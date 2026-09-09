#!/usr/bin/env python3
"""Validate an English issue title and the shared Markdown issue template."""

import argparse
import json
from pathlib import Path
import re
import sys

from validate_pr import has_disallowed_non_ascii


HEADINGS = (
    "Issue Type", "Summary", "Tasks", "Acceptance Criteria", "Bug Details",
    "Android Environment", "References and Notes",
)
ISSUE_TYPES = ("Feature", "Bug", "Improvement", "Task")
BUG_FIELDS = ("Steps to reproduce", "Expected behavior", "Actual behavior")
ENV_FIELDS = ("Device / emulator", "Android version / API level", "App version / build variant")
PLACEHOLDER = re.compile(
    r"<(?:Title|Task|Expected outcome|Setup or starting screen|Action that triggers the problem|"
    r"What should happen|What happens instead)>|"
    r"^[ \t]*(?:- \[[ xX]\][ \t]*|[-*][ \t]+|\d+[.)][ \t]+)?(?:TODO|TBD)[.!]?[ \t]*$",
    re.IGNORECASE | re.MULTILINE,
)
CHECKBOX = re.compile(r"- \[([ xX])\](?:[ \t]+(.*))?")


def has_detail(text):
    text = text.strip().strip("*_`").strip()
    return (
        bool(re.search(r"[A-Za-z0-9]", text))
        and text.lower() not in {"n/a", "none", "not applicable"}
        and not PLACEHOLDER.search(text)
    )


def markdown_lines(body, errors):
    """Yield visible lines and whether they are code, preserving fenced log text."""
    fence = None
    in_comment = False
    for raw in body.replace("\r\n", "\n").splitlines():
        line = raw
        if not fence:
            visible = ""
            while line:
                if in_comment:
                    before, end, line = line.partition("-->")
                    if not end:
                        break
                    in_comment = False
                else:
                    before, start, line = line.partition("<!--")
                    visible += before
                    if not start:
                        break
                    in_comment = True
            line = visible
            if "-->" in line:
                errors.append("Close HTML comments correctly.")
        marker = re.match(r"^ {0,3}(`{3,}|~{3,})(.*)$", line)
        if fence:
            if marker and marker[1][0] == fence[0] and len(marker[1]) >= len(fence) and not marker[2].strip():
                fence = None
            else:
                yield line, True
        elif marker:
            fence = marker[1]
        else:
            yield line, False
    if fence:
        errors.append("Close Markdown code fences correctly.")
    if in_comment:
        errors.append("Close HTML comments correctly.")


def validate(title, body):
    errors = []
    if not isinstance(title, str) or not isinstance(body, str):
        return ["Issue title and body must be strings."]
    if not has_detail(title) or "\n" in title or "\r" in title:
        errors.append("Title: write a short, descriptive title and replace placeholders.")
    if has_disallowed_non_ascii(title + body):
        errors.append("Write in English; non-ASCII characters are allowed only for emoji and supported typographic punctuation.")

    sections = []
    for line, is_code in markdown_lines(body, errors):
        if not is_code and line.startswith("## "):
            sections.append((line[3:].rstrip(), []))
        elif sections:
            sections[-1][1].append((line, is_code))
        elif line.strip():
            errors.append("Place all content under the template headings.")
    if tuple(name for name, _ in sections) != HEADINGS:
        errors.append("Keep these headings exactly once and in order: " + ", ".join(HEADINGS) + ".")
        return list(dict.fromkeys(errors))
    sections = dict(sections)
    contents = {name: "\n".join(line for line, _ in lines).strip() for name, lines in sections.items()}
    for name, content in contents.items():
        if not content:
            errors.append(f"{name}: add content; use N/A only in optional sections.")
        if PLACEHOLDER.search(content):
            errors.append(f"{name}: replace template placeholders, TODO, and TBD with actual details.")

    type_lines = [line.strip() for line, code in sections["Issue Type"] if line.strip() and not code]
    choices = [CHECKBOX.fullmatch(line) for line in type_lines]
    selected = [match[2] for match in choices if match and match[1].lower() == "x"]
    if (
        len(choices) != len(ISSUE_TYPES)
        or any(match is None for match in choices)
        or tuple(match[2] for match in choices if match) != ISSUE_TYPES
        or len(selected) != 1
        or any(code for _, code in sections["Issue Type"])
    ):
        errors.append("Issue Type: keep the four template options and select exactly one using [x].")

    if not has_detail(contents["Summary"]):
        errors.append("Summary: describe the problem or goal and why it matters.")
    for name in ("Tasks", "Acceptance Criteria"):
        items = [CHECKBOX.fullmatch(line.strip()) for line, code in sections[name] if not code and re.match(r"\s*- \[", line)]
        if not items or any(not item or not has_detail(item[2] or "") for item in items):
            errors.append(f"{name}: write at least one non-empty '- [ ] ...' or '- [x] ...' item; fill every checkbox item.")

    is_bug = selected == ["Bug"]
    if is_bug or contents["Bug Details"] != "N/A":
        details = []
        for line, code in sections["Bug Details"]:
            if not code and line.strip() in {f"**{field}**" for field in BUG_FIELDS}:
                details.append((line.strip()[2:-2], []))
            elif details:
                details[-1][1].append((line, code))
        if tuple(name for name, _ in details) != BUG_FIELDS:
            errors.append("Bug Details: fill Steps to reproduce, Expected behavior, and Actual behavior using the template labels.")
        else:
            for name, lines in details:
                if not has_detail("\n".join(line for line, _ in lines)):
                    errors.append(f"Bug Details / {name}: provide actual details.")
            steps = [
                match[1] for line, code in details[0][1]
                if not code and (match := re.match(r"^\s*\d+[.)](?:[ \t]+(.*))?$", line))
            ]
            if not steps or any(not has_detail(step or "") for step in steps):
                errors.append("Bug Details / Steps to reproduce: include numbered reproduction steps and fill every step.")

    if is_bug or contents["Android Environment"] != "N/A":
        for field in ENV_FIELDS:
            values = [line.strip()[len(field) + 4:] for line, code in sections["Android Environment"] if not code and line.strip().startswith(f"- {field}: ")]
            valid = len(values) == 1 and has_detail(values[0])
            if valid and values[0].lower().startswith("unknown"):
                prefix, separator, reason = values[0].partition(":")
                valid = prefix.lower() == "unknown" and bool(separator) and has_detail(reason) and reason.strip().lower() != "reason"
            if not valid:
                errors.append(f"Android Environment / {field}: fill this field; use 'Unknown: reason' if unavailable.")
    if contents["References and Notes"] != "N/A" and not has_detail(contents["References and Notes"]):
        errors.append("References and Notes: add context or use N/A.")
    return list(dict.fromkeys(errors))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--title")
    parser.add_argument("--body-file", type=Path)
    parser.add_argument("--event-path", type=Path)
    args = parser.parse_args()
    if args.event_path:
        if args.title is not None or args.body_file is not None:
            parser.error("Use --event-path alone, or --title with --body-file.")
    elif args.title is None or args.body_file is None:
        parser.error("Provide --title and --body-file, or --event-path.")
    try:
        if args.event_path:
            issue = json.loads(args.event_path.read_text(encoding="utf-8"))["issue"]
            if "pull_request" in issue:
                raise ValueError("Use validate_pr.py for pull requests.")
            title, body = issue["title"], issue["body"]
            if body is None:
                body = ""
            if not isinstance(title, str) or not isinstance(body, str):
                raise ValueError("Issue title and body must be strings.")
        else:
            title, body = args.title, args.body_file.read_text(encoding="utf-8")
    except (OSError, UnicodeError, ValueError, KeyError, TypeError) as error:
        print(f"issue-check: Cannot read issue input: {error}", file=sys.stderr)
        return 2
    errors = validate(title, body)
    for error in errors:
        print(f"issue-check: {error}", file=sys.stderr)
    if errors:
        return 1
    print("issue-check: Issue title and body are valid.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

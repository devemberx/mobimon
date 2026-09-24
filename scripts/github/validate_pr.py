#!/usr/bin/env python3
"""Validate a PR title and Markdown body using only the Python standard library."""

import argparse
import json
from pathlib import Path
import re
import sys


HEADINGS = ["Summary", "Changes", "Verification", "Demo", "Shortcuts and Risks"]
TITLE_PATTERN = re.compile(
    r"(feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)"
    r"\([a-z][a-z0-9]*(-[a-z0-9]+)*\)!?: [!-~][ -~]*"
)
PLACEHOLDER = re.compile(
    r"<(?:reason|Why the change is needed|What changed)>|^\s*(?:- )?(?:TODO|TBD)\s*$",
    re.IGNORECASE | re.MULTILINE,
)
NON_ASCII_RE = re.compile(r"[^\x00-\x7F]")
TYPOGRAPHIC_RE = re.compile(
    "[\u2013\u2014\u2018\u2019\u201c\u201d\u2022\u2026"
    "\u2190\u2192\u2194\u21d2\u2260\u2264\u2265\u00b1\u00b7\u00d7\u00f7]"
)
EMOJI_RE = re.compile(
    "[\U0001f000-\U0001faff\u2600-\u27bf\u2b00-\u2bff\ufe00-\ufe0f\u200d]"
)


def has_disallowed_non_ascii(text):
    # Match the Polarion hook's character filter, not a language detector.
    stripped = TYPOGRAPHIC_RE.sub("", EMOJI_RE.sub("", text))
    return bool(NON_ASCII_RE.search(stripped))


def has_content(text):
    return bool(re.sub(r"[\s#*`~>\[\]().:_-]", "", text))


def extract_sections(body):
    body = re.sub(r"<!--.*?-->", "", body.replace("\r\n", "\n"), flags=re.DOTALL)
    sections = []
    preamble = []
    fence = None
    for line in body.split("\n"):
        marker = re.match(r"^ {0,3}(`{3,}|~{3,})(.*)$", line)
        if fence:
            if marker and marker[1][0] == fence[0] and len(marker[1]) >= len(fence) and not marker[2].strip():
                fence = None
        elif marker:
            fence = marker[1]
        elif line.startswith("## "):
            sections.append((line[3:].rstrip(), []))
            continue
        (sections[-1][1] if sections else preamble).append(line)
    return preamble, sections, bool(fence)


def validate(title, body):
    errors = []
    if not TITLE_PATTERN.fullmatch(title) or len(title) > 50 or title.endswith("."):
        errors.append("Title: use type(scope): summary, printable ASCII, max 50 characters, no final period.")
    if has_disallowed_non_ascii(body):
        errors.append("Body: write in English; non-ASCII characters are allowed only for emoji and typographic punctuation.")

    normalized_body = re.sub(r"<!--.*?-->", "", body.replace("\r\n", "\n"), flags=re.DOTALL)
    if "<!--" in normalized_body or "-->" in normalized_body:
        errors.append("Close HTML comments correctly.")
    preamble, sections, fence_open = extract_sections(normalized_body)

    if fence_open:
        errors.append("Close Markdown code fences correctly.")
    if any(line.strip() for line in preamble):
        errors.append("Place content under the five required headings.")
    if [name for name, _ in sections] != HEADINGS:
        errors.append("Keep these headings exactly once and in order: " + ", ".join(HEADINGS) + ".")
        return errors

    for name, lines in sections:
        content = "\n".join(lines).strip()
        if not has_content(content):
            errors.append(f"{name}: add content; comments and empty bullets do not count.")
        if PLACEHOLDER.search(content):
            errors.append(f"{name}: replace placeholders with actual details.")
        if content.lower() in {"none", "n/a", "not run"} and not (name == "Shortcuts and Risks" and content == "None"):
            errors.append(f"{name}: explain the change or give a reason for omitting checks/demo.")
        for prefix, allowed_section in (("Not run:", "Verification"), ("N/A:", "Demo")):
            if content.lower().startswith(prefix.lower()):
                if name != allowed_section or not has_content(content[len(prefix):]):
                    errors.append(f"{name}: use '{prefix} <reason>' only in {allowed_section}, with a real reason.")
        if name == "Changes":
            bullets = [line.rstrip(" ") for line in lines if line.strip()]
            if len(bullets) != 2 or any(
                not re.fullmatch(r"- [!-~][ -~]*", line) or not has_content(line[2:])
                for line in bullets
            ):
                errors.append("Changes: write exactly two single-line ASCII '- ' bullets, why then what; no other text.")
            if any(len(line) > 120 for line in bullets):
                errors.append("Changes: each bullet must be at most 120 characters, including '- '.")
    return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--title", help="PR title")
    parser.add_argument("--body-file", type=Path, help="UTF-8 Markdown PR body")
    parser.add_argument("--event-path", type=Path, help="GitHub pull_request event JSON")
    args = parser.parse_args()
    if args.event_path:
        if args.title is not None or args.body_file is not None:
            parser.error("Use --event-path alone, or --title with --body-file.")
    elif args.title is None or args.body_file is None:
        parser.error("Provide --title and --body-file, or --event-path.")
    try:
        if args.event_path:
            event = json.loads(args.event_path.read_text(encoding="utf-8"))
            pr = event["pull_request"]
            title, body = pr["title"], pr["body"]
            if body is None:
                body = ""
            if not isinstance(title, str) or not isinstance(body, str):
                raise ValueError("PR title and body must be strings.")
        else:
            title, body = args.title, args.body_file.read_text(encoding="utf-8")
    except (OSError, UnicodeError, ValueError, KeyError, TypeError) as error:
        print(f"pr-check: Cannot read PR input: {error}", file=sys.stderr)
        return 2
    errors = validate(title, body)
    for error in errors:
        print(f"pr-check: {error}", file=sys.stderr)
    if errors:
        return 1
    print("pr-check: PR title and body are valid.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

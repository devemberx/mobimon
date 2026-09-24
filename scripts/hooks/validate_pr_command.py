#!/usr/bin/env python3
"""Check gh PR content commands before an agent sends them to GitHub."""

import json
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts/github"))

import validate_pr

try:
    from . import validate_merge
except ImportError:  # Executed directly as a hook script.
    import validate_merge


CONTENT = {
    "--title": "title",
    "-t": "title",
    "--body": "body",
    "-b": "body",
    "--body-file": "body_file",
    "-F": "body_file",
}
VALUES = {
    "--repo",
    "-R",
    "--base",
    "-B",
    "--head",
    "-H",
    "--assignee",
    "-a",
    "--reviewer",
    "-r",
    "--label",
    "-l",
    "--milestone",
    "-m",
    "--project",
    "-p",
    "--add-assignee",
    "--remove-assignee",
    "--add-reviewer",
    "--remove-reviewer",
    "--add-label",
    "--remove-label",
    "--add-project",
    "--remove-project",
}
GENERATED = {
    "--fill",
    "--fill-first",
    "--fill-verbose",
    "--template",
    "-T",
    "--web",
    "-w",
}


def parse_arguments(arguments):
    values = {}
    identifiers = []
    generated = False
    index = 0
    while index < len(arguments):
        argument = arguments[index]
        option, separator, inline = argument.partition("=")
        if option in CONTENT or option in VALUES or option in {"--template", "-T"}:
            if separator:
                value = inline
                index += 1
            elif index + 1 < len(arguments):
                value = arguments[index + 1]
                index += 2
            else:
                return None, [f"{option} requires a value."]
            if not value:
                return None, [f"{option} requires a value."]
            if option in CONTENT:
                key = CONTENT[option]
                if key in values:
                    return None, [f"Specify {option} only once."]
                values[key] = value
            elif option in {"--repo", "-R"}:
                values["repository"] = value
            elif option in {"--template", "-T"}:
                generated = True
        elif option in GENERATED:
            generated = True
            index += 1
        elif argument.startswith("-"):
            index += 1
        else:
            identifiers.append(argument)
            index += 1
    values["identifiers"] = identifiers
    values["generated"] = generated
    return values, []


def load_existing_pr(identifier, repository, cwd):
    command = ["gh", "pr", "view"]
    if identifier:
        command.append(identifier)
    command.extend(["--json", "title,body"])
    if repository:
        command.extend(["--repo", repository])
    result = subprocess.run(
        command,
        cwd=cwd,
        capture_output=True,
        text=True,
        timeout=15,
        check=False,
    )
    if result.returncode:
        detail = (result.stderr or result.stdout).strip().split("\n", 1)[0]
        raise RuntimeError(detail[:300] or f"gh exited with status {result.returncode}")
    metadata = json.loads(result.stdout)
    if (
        not isinstance(metadata, dict)
        or not isinstance(metadata.get("title"), str)
        or not isinstance(metadata.get("body"), str)
    ):
        raise ValueError("gh returned incomplete PR content")
    return metadata


def validate_command(command, cwd, metadata_loader=None):
    command = validate_merge._remove_line_continuations(command)
    try:
        tokens = validate_merge._shell_tokens(command)
    except ValueError:
        if "gh" in command and "pr" in command:
            return ["Run gh pr create or gh pr edit as a standalone command."]
        return []
    located = None
    action = None
    for candidate in ("create", "edit"):
        located = validate_merge._locate_command(tokens, ("pr", candidate))
        if located is not None:
            action = candidate
            break
    if located is None:
        return []
    start, end, repositories = located
    if (
        "\n" in command
        or start != 0
        or validate_merge._has_command_substitution(command)
        or any(token in validate_merge.CONTROL_TOKENS for token in tokens)
    ):
        return [
            "Run gh pr create or gh pr edit as a standalone command with literal PR content."
        ]
    values, errors = parse_arguments(tokens[end:])
    if errors:
        return errors
    if repositories:
        if len(repositories) > 1 or "repository" in values:
            return ["Specify --repo only once."]
        values["repository"] = repositories[0]

    changing_content = any(key in values for key in ("title", "body", "body_file"))
    if action == "edit" and not changing_content and not values["generated"]:
        return []
    if action == "create" and values["identifiers"]:
        return ["gh pr create does not accept a PR identifier."]
    if action == "edit" and len(values["identifiers"]) > 1:
        return ["Specify at most one PR identifier."]
    if values["generated"]:
        return [
            "Provide an explicit --title and --body-file or --body; generated PR content cannot be checked."
        ]
    if "body" in values and "body_file" in values:
        return ["Provide either --body or --body-file, not both."]

    if action == "edit" and ("title" not in values or not ("body" in values or "body_file" in values)):
        loader = metadata_loader or load_existing_pr
        try:
            identifier = values["identifiers"][0] if values["identifiers"] else None
            metadata = loader(identifier, values.get("repository"), Path(cwd))
        except (OSError, ValueError, RuntimeError, subprocess.TimeoutExpired) as error:
            return [f"Cannot inspect the current PR before editing it: {error}."]
        title = values.get("title", metadata["title"])
        body = values.get("body", metadata["body"])
    else:
        if "title" not in values or not ("body" in values or "body_file" in values):
            return [
                "Provide an explicit --title and --body-file or --body so the PR template can be checked."
            ]
        title = values["title"]
        body = values.get("body")

    if "body_file" in values:
        if values["body_file"] == "-":
            return ["Use a readable --body-file path instead of stdin."]
        body_path = Path(values["body_file"])
        if not body_path.is_absolute():
            body_path = Path(cwd) / body_path
        try:
            body = body_path.read_text(encoding="utf-8")
        except (OSError, UnicodeError) as error:
            return [f"Cannot read PR body file: {error}."]
    return validate_pr.validate(title, body)


def main():
    try:
        event = json.load(sys.stdin)
        command = event["tool_input"]["command"]
        cwd = event["cwd"]
        if not isinstance(command, str) or not isinstance(cwd, str):
            raise ValueError("command and cwd must be strings")
    except (OSError, UnicodeError, ValueError, KeyError, TypeError) as error:
        print(f"pr-hook: Cannot inspect hook input: {error}", file=sys.stderr)
        return 2
    try:
        errors = validate_command(command, Path(cwd))
    except Exception as error:
        errors = [f"Cannot inspect the PR command: {error}."]
    for error in errors:
        print(f"pr-hook: {error}", file=sys.stderr)
    return 2 if errors else 0


if __name__ == "__main__":
    sys.exit(main())

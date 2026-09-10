#!/usr/bin/env python3
"""Block pull-request merges that do not match repository policy."""

from pathlib import Path
import json
import re
import shlex
import subprocess
import sys

try:
    from . import validate_pr
except ImportError:  # Executed directly from the scripts directory.
    import validate_pr


CONTROL_TOKENS = {";", "&&", "||", "|", "&", "(", ")", "`"}
VALUE_OPTIONS = {
    "--subject": "subject",
    "-t": "subject",
    "--body-file": "body_file",
    "-F": "body_file",
    "--match-head-commit": "head_sha",
    "--repo": "repository",
    "-R": "repository",
}
FLAG_OPTIONS = {
    "--squash": "squash",
    "-s": "squash",
    "--delete-branch": "delete_branch",
    "-d": "delete_branch",
}
FORBIDDEN_OPTIONS = {
    "--admin",
    "--auto",
    "--author-email",
    "-A",
    "--merge",
    "-m",
    "--rebase",
    "-r",
}
PR_URL_PATTERN = re.compile(r"https://github\.com/[^/\s]+/[^/\s]+/pull/[1-9][0-9]*/?")


def _shell_tokens(command):
    lexer = shlex.shlex(command, posix=True, punctuation_chars=";&|()`")
    lexer.commenters = ""
    lexer.whitespace_split = True
    return list(lexer)


def _remove_line_continuations(command):
    result = []
    quote = None
    index = 0
    while index < len(command):
        character = command[index]
        newline_width = 0
        if command[index:index + 2] == "\\\n":
            newline_width = 2
        elif command[index:index + 3] == "\\\r\n":
            newline_width = 3
        if newline_width and quote != "'":
            index += newline_width
            continue
        if character == "\\" and quote != "'" and index + 1 < len(command):
            result.extend((character, command[index + 1]))
            index += 2
            continue
        if character == "'":
            if quote is None:
                quote = "'"
            elif quote == "'":
                quote = None
        elif character == '"':
            if quote is None:
                quote = '"'
            elif quote == '"':
                quote = None
        result.append(character)
        index += 1
    return "".join(result)


def _has_command_substitution(command):
    quote = None
    escaped = False
    index = 0
    while index < len(command):
        character = command[index]
        if escaped:
            escaped = False
        elif character == "\\" and quote != "'":
            escaped = True
        elif character == "'":
            if quote is None:
                quote = "'"
            elif quote == "'":
                quote = None
        elif character == '"':
            if quote is None:
                quote = '"'
            elif quote == '"':
                quote = None
        elif quote != "'" and character == "`":
            return True
        elif quote != "'" and command[index:index + 2] == "$(":
            return True
        index += 1
    return False


def _is_gh(token):
    return Path(token).name == "gh"


def _consume_repo_options(tokens, index):
    repositories = []
    while index < len(tokens):
        if len(tokens[index]) > 2 and tokens[index].startswith("-R") and not tokens[index].startswith("-R="):
            repository = tokens[index][2:]
            if not repository:
                break
            repositories.append(repository)
            index += 1
            continue
        option, separator, inline_value = tokens[index].partition("=")
        if option not in {"--repo", "-R"}:
            break
        if separator:
            if not inline_value:
                return index, repositories
            repositories.append(inline_value)
            index += 1
        elif index + 1 < len(tokens):
            repositories.append(tokens[index + 1])
            index += 2
        else:
            return index, repositories
    return index, repositories


def _locate_command(tokens, components):
    for start, token in enumerate(tokens):
        if not _is_gh(token):
            continue
        index = start + 1
        repositories = []
        for component in components:
            index, found_repositories = _consume_repo_options(tokens, index)
            repositories.extend(found_repositories)
            if index >= len(tokens) or tokens[index] != component:
                break
            index += 1
        else:
            return start, index, repositories
    return None


def _file_contains_merge_mutation(path, cwd, json_input=False):
    if path in {"", "-"}:
        return True
    input_path = Path(path)
    if not input_path.is_absolute():
        input_path = cwd / input_path
    try:
        content = input_path.read_bytes().decode("utf-8")
    except (OSError, UnicodeError):
        return True
    if "mergePullRequest" in content or "enablePullRequestAutoMerge" in content:
        return True
    if not json_input:
        return False
    try:
        payload = json.loads(content)
    except (json.JSONDecodeError, TypeError):
        return True
    if not isinstance(payload, dict) or not isinstance(payload.get("query"), str):
        return True
    query = payload["query"]
    return "mergePullRequest" in query or "enablePullRequestAutoMerge" in query


def _known_api_merge(tokens, cwd):
    located = _locate_command(tokens, ("api",))
    if located is None:
        return False
    _start, end, _repositories = located
    arguments = tokens[end:]
    for argument in arguments:
        endpoint = re.split(r"[?#]", argument, maxsplit=1)[0]
        if re.fullmatch(
            r"/?repos/[^/\s]+/[^/\s]+/pulls/[1-9][0-9]*/merge", endpoint
        ):
            return True

    joined = " ".join(arguments)
    if "mergePullRequest" in joined or "enablePullRequestAutoMerge" in joined:
        return True
    if "graphql" not in arguments:
        return False

    index = 0
    while index < len(arguments):
        argument = arguments[index]
        option, separator, inline_value = argument.partition("=")
        if option == "--input":
            if separator:
                if _file_contains_merge_mutation(inline_value, cwd, json_input=True):
                    return True
            elif index + 1 >= len(arguments):
                return True
            else:
                if _file_contains_merge_mutation(
                    arguments[index + 1], cwd, json_input=True
                ):
                    return True
                index += 1
        if option in {"--field", "-F"}:
            value = inline_value if separator else (
                arguments[index + 1] if index + 1 < len(arguments) else ""
            )
            key, equals, field_value = value.partition("=")
            if key == "query" and equals and field_value.startswith("@"):
                if _file_contains_merge_mutation(field_value[1:], cwd):
                    return True
            if not separator:
                index += 1
        index += 1
    return False


def _parse_merge_arguments(arguments):
    values = {}
    flags = set()
    errors = []
    identifier = None
    index = 0
    while index < len(arguments):
        argument = arguments[index]
        if len(argument) > 2 and argument.startswith("-R") and not argument.startswith("-R="):
            repository = argument[2:]
            if not repository:
                errors.append("-R requires a value.")
            elif "repository" in values:
                errors.append("Specify -R exactly once.")
            else:
                values["repository"] = repository
            index += 1
            continue
        option, separator, inline_value = argument.partition("=")
        if option in FORBIDDEN_OPTIONS:
            errors.append(f"Do not use {option} for a squash merge.")
            index += 2 if option in {"--author-email", "-A"} and not separator else 1
        elif option in VALUE_OPTIONS:
            key = VALUE_OPTIONS[option]
            if key in values:
                errors.append(f"Specify {option} exactly once.")
                index += 1
            elif separator:
                if inline_value:
                    values[key] = inline_value
                else:
                    errors.append(f"{option} requires a value.")
                index += 1
            elif index + 1 >= len(arguments):
                errors.append(f"{option} requires a value.")
                index += 1
            else:
                values[key] = arguments[index + 1]
                index += 2
        elif argument in FLAG_OPTIONS:
            key = FLAG_OPTIONS[argument]
            if key in flags:
                errors.append(f"Specify {argument} exactly once.")
            flags.add(key)
            index += 1
        elif argument.startswith("-"):
            errors.append(f"Unsupported merge option: {argument}.")
            index += 1
        elif identifier is None:
            identifier = argument
            index += 1
        else:
            errors.append(f"Unexpected merge argument: {argument}.")
            index += 1

    valid_identifier = identifier is not None and (
        identifier.isdigit() and int(identifier) > 0
        or PR_URL_PATTERN.fullmatch(identifier)
    )
    if not valid_identifier:
        errors.append("Provide an explicit PR number or URL.")
    if "squash" not in flags:
        errors.append("Use --squash for every PR merge.")
    for key, option in (
        ("subject", "--subject"),
        ("body_file", "--body-file"),
        ("head_sha", "--match-head-commit"),
    ):
        if not values.get(key):
            errors.append(f"Provide {option} explicitly.")
    return identifier, values, errors


def load_pr_metadata(identifier, repository, cwd):
    command = [
        "gh",
        "pr",
        "view",
        identifier,
        "--json",
        "title,body,headRefOid,state,isDraft",
    ]
    if repository:
        command.extend(["--repo", repository])
    try:
        result = subprocess.run(
            command,
            cwd=cwd,
            capture_output=True,
            text=True,
            timeout=15,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired) as error:
        raise RuntimeError(str(error)) from error
    if result.returncode:
        detail = (result.stderr or result.stdout).strip().split("\n", 1)[0]
        raise RuntimeError(detail[:300] or f"gh exited with status {result.returncode}")
    try:
        metadata = json.loads(result.stdout)
    except (json.JSONDecodeError, TypeError) as error:
        raise RuntimeError("gh returned invalid JSON") from error
    if not isinstance(metadata, dict):
        raise RuntimeError("gh returned an invalid PR record")
    return metadata


def _expected_changes(body):
    _preamble, sections, _fence_open = validate_pr.extract_sections(body)
    for name, lines in sections:
        if name == "Changes":
            return "\n".join(line for line in lines if line.strip())
    raise ValueError("the Changes section is missing")


def _validate_metadata(metadata, values):
    required_types = {
        "title": str,
        "body": str,
        "headRefOid": str,
        "state": str,
        "isDraft": bool,
    }
    if not isinstance(metadata, dict) or any(
        not isinstance(metadata.get(key), expected_type)
        for key, expected_type in required_types.items()
    ):
        return ["GitHub returned incomplete PR metadata."]
    if metadata["state"] != "OPEN" or metadata["isDraft"]:
        return ["The pull request must be open, non-draft, and ready for review."]

    pr_errors = validate_pr.validate(metadata["title"], metadata["body"])
    if pr_errors:
        return [f"PR format is invalid: {pr_errors[0]}"]

    errors = []
    if values["subject"] != metadata["title"]:
        errors.append("The merge subject must exactly match the current PR title.")
    if values["head_sha"] != metadata["headRefOid"]:
        errors.append("--match-head-commit must exactly match the current PR head SHA.")

    body_path = Path(values["body_file"])
    if not body_path.is_absolute():
        body_path = values["cwd"] / body_path
    try:
        supplied_body = body_path.read_bytes().decode("utf-8")
    except (OSError, UnicodeError) as error:
        errors.append(f"Cannot read the merge body file: {error}.")
    else:
        if supplied_body.endswith("\n"):
            supplied_body = supplied_body[:-1]
        if supplied_body != _expected_changes(metadata["body"]):
            errors.append("The merge body file must contain exactly the current Changes bullets.")
    return errors


def validate_event(event, metadata_loader=None):
    if not isinstance(event, dict):
        return ["Cannot inspect the shell command because the hook input is invalid."]
    tool_input = event.get("tool_input")
    command = tool_input.get("command") if isinstance(tool_input, dict) else None
    cwd = event.get("cwd")
    if not isinstance(command, str) or not isinstance(cwd, str):
        return ["Cannot inspect the shell command because the hook input is incomplete."]
    return validate_command(command, Path(cwd), metadata_loader)


def validate_command(command, cwd, metadata_loader=None):
    if not isinstance(command, str):
        return []
    command = _remove_line_continuations(command)
    possible_merge = bool(
        re.search(r"\bgh\b[^\n]*\bpr\b[^\n]*\bmerge\b|\bgh\b[^\n]*\bapi\b", command)
    )
    try:
        tokens = _shell_tokens(command)
    except ValueError:
        return ["Run the canonical gh pr merge command as a standalone command."] if possible_merge else []

    if _known_api_merge(tokens, Path(cwd)):
        return ["Use the canonical gh pr merge command instead of a direct API merge."]

    located = _locate_command(tokens, ("pr", "merge"))
    if located is None:
        return []
    merge_start, arguments_start, prefix_repositories = located
    if (
        "\n" in command
        or merge_start != 0
        or _has_command_substitution(command)
        or any(token in CONTROL_TOKENS for token in tokens)
    ):
        return ["Run the canonical gh pr merge command as a standalone command."]

    identifier, values, errors = _parse_merge_arguments(tokens[arguments_start:])
    if prefix_repositories:
        if len(prefix_repositories) > 1 or "repository" in values:
            errors.append("Specify --repo exactly once.")
        else:
            values["repository"] = prefix_repositories[0]
    if errors:
        return errors

    loader = metadata_loader or load_pr_metadata
    try:
        metadata = loader(identifier, values.get("repository"), Path(cwd))
    except Exception as error:
        return [f"Cannot verify the pull request: {error}"]
    values["cwd"] = Path(cwd)
    return _validate_metadata(metadata, values)


def main():
    try:
        event = json.load(sys.stdin)
    except (OSError, UnicodeError, json.JSONDecodeError, TypeError, ValueError):
        print("merge-check: Cannot inspect invalid hook JSON.", file=sys.stderr)
        return 2
    try:
        errors = validate_event(event)
    except Exception as error:
        errors = [f"Cannot inspect the merge command: {error}"]
    for error in errors:
        print(f"merge-check: {error}", file=sys.stderr)
    return 2 if errors else 0


if __name__ == "__main__":
    sys.exit(main())

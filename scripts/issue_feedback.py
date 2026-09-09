#!/usr/bin/env python3
"""Validate a current GitHub issue and maintain its format feedback comment/label."""

import argparse
import json
import os
import re
import sys
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

from validate_issue import validate


MARKER = "<!-- rivo-issue-format:v1 -->"
LABEL = "needs-info"


class APIError(RuntimeError):
    def __init__(self, method, path, status):
        self.status = status
        super().__init__(f"GitHub API {method} {path} failed (HTTP {status}).")


class GitHubAPI:
    def __init__(self, token):
        self.token = token

    def request(self, method, path, data=None, missing_ok=False):
        request = Request(
            "https://api.github.com" + path,
            data=json.dumps(data).encode("utf-8") if data is not None else None,
            method=method,
            headers={
                "Authorization": f"Bearer {self.token}",
                "Accept": "application/vnd.github+json",
                "Content-Type": "application/json",
                "User-Agent": "RIVO-issue-format",
            },
        )
        try:
            with urlopen(request, timeout=30) as response:
                content = response.read()
                return json.loads(content) if content else None
        except HTTPError as error:
            if missing_ok and error.code == 404:
                return None
            raise APIError(method, path, error.code) from error


def sync_feedback(api, repository, number):
    if not re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", repository) or number < 1:
        raise ValueError("Provide an owner/repository and a positive issue number.")
    root = f"/repos/{repository}"
    issue_path = f"{root}/issues/{number}"
    # Read current content rather than an event snapshot that may already be stale.
    issue = api.request("GET", issue_path)
    if "pull_request" in issue or issue["state"] != "open":
        return []
    errors = validate(issue["title"], issue["body"] or "")
    comment = None
    page = 1
    while True:
        comments = api.request("GET", f"{issue_path}/comments?per_page=100&page={page}")
        comment = next((entry for entry in comments if (
            entry.get("user", {}).get("login") == "github-actions[bot]"
            and entry.get("user", {}).get("type") == "Bot"
            and (entry.get("body") or "").startswith(MARKER + "\n")
        )), None)
        if comment or len(comments) < 100:
            break
        page += 1

    template = f"https://github.com/{repository}/blob/HEAD/.github/ISSUE_TEMPLATE.md"
    if errors:
        body = (
            f"{MARKER}\n### Issue format: changes needed\n\n"
            "Please edit the issue description to address the following items:\n\n"
            + "\n".join(f"- {error}" for error in errors)
            + f"\n\nUse the [issue template]({template}). "
            "The check runs again when you edit the issue. This comment will be updated in place."
        )
    else:
        body = f"{MARKER}\n### Issue format: passed\n\nThe current title and description match the [issue template]({template})."
    if comment:
        if comment["body"] != body:
            api.request("PATCH", f"{root}/issues/comments/{comment['id']}", {"body": body})
    elif errors:
        api.request("POST", f"{issue_path}/comments", {"body": body})

    has_label = any(label["name"].lower() == LABEL for label in issue["labels"])
    if errors and not has_label:
        if api.request("GET", f"{root}/labels/{LABEL}", missing_ok=True) is None:
            try:
                api.request("POST", f"{root}/labels", {
                    "name": LABEL, "color": "FBCA04", "description": "Issue format needs additional information",
                })
            except APIError as error:
                # Two different issues may create the repository label concurrently.
                if error.status != 422 or api.request("GET", f"{root}/labels/{LABEL}", missing_ok=True) is None:
                    raise
        api.request("POST", f"{issue_path}/labels", {"labels": [LABEL]})
    elif not errors and has_label and comment:
        # Do not remove a manually applied label on an issue we have never flagged.
        api.request("DELETE", f"{issue_path}/labels/{LABEL}", missing_ok=True)
    return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repository", required=True)
    parser.add_argument("--issue-number", required=True, type=int)
    args = parser.parse_args()
    token = os.environ.get("GH_TOKEN")
    if not token:
        parser.error("Set GH_TOKEN to a token with issues: write permission.")
    try:
        errors = sync_feedback(GitHubAPI(token), args.repository, args.issue_number)
    except (RuntimeError, OSError, URLError, ValueError, KeyError, TypeError) as error:
        print(f"issue-check: Cannot synchronize feedback: {error}", file=sys.stderr)
        return 2
    for error in errors:
        print(f"issue-check: {error}", file=sys.stderr)
    if errors:
        return 1
    print("issue-check: Feedback synchronized; issue is valid or no longer open.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

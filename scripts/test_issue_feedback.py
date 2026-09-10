#!/usr/bin/env python3
"""Regression tests for issue-format feedback synchronization."""

import unittest
from unittest.mock import patch

import issue_feedback


REPOSITORY = "monsters/MobiMon"
ISSUE_PATH = "/repos/monsters/MobiMon/issues/7"
LEGACY_MARKER = "<!-- rivo-issue-format:v1 -->"


class FakeGitHubAPI:
    def __init__(self, issue, comments):
        self.issue = issue
        self.comments = comments
        self.calls = []

    def request(self, method, path, data=None, missing_ok=False):
        self.calls.append((method, path, data, missing_ok))
        if method == "GET" and path == ISSUE_PATH:
            return self.issue
        if method == "GET" and path == f"{ISSUE_PATH}/comments?per_page=100&page=1":
            return self.comments
        return None


def open_issue():
    return {
        "title": "Issue title",
        "body": "Issue body",
        "state": "open",
        "labels": [{"name": "needs-info"}],
    }


def legacy_bot_comment():
    return {
        "id": 42,
        "body": f"{LEGACY_MARKER}\nold feedback",
        "user": {"login": "github-actions[bot]", "type": "Bot"},
    }


class SyncFeedbackTest(unittest.TestCase):
    @patch.object(issue_feedback, "validate", return_value=["Missing required content"])
    def test_invalid_issue_updates_legacy_comment_without_creating_duplicate(self, _validate):
        api = FakeGitHubAPI(open_issue(), [legacy_bot_comment()])

        issue_feedback.sync_feedback(api, REPOSITORY, 7)

        patches = [call for call in api.calls if call[0] == "PATCH"]
        comment_posts = [call for call in api.calls if call[0] == "POST" and call[1].endswith("/comments")]
        self.assertEqual(1, len(patches))
        self.assertEqual(f"{ISSUE_PATH.rsplit('/', 2)[0]}/issues/comments/42", patches[0][1])
        self.assertTrue(patches[0][2]["body"].startswith(issue_feedback.MARKER + "\n"))
        self.assertEqual([], comment_posts)

    @patch.object(issue_feedback, "validate", return_value=[])
    def test_valid_issue_updates_legacy_comment_and_removes_managed_label(self, _validate):
        api = FakeGitHubAPI(open_issue(), [legacy_bot_comment()])

        issue_feedback.sync_feedback(api, REPOSITORY, 7)

        patches = [call for call in api.calls if call[0] == "PATCH"]
        deletes = [call for call in api.calls if call[0] == "DELETE"]
        self.assertEqual(1, len(patches))
        self.assertEqual(
            [("DELETE", f"{ISSUE_PATH}/labels/{issue_feedback.LABEL}", None, True)],
            deletes,
        )


if __name__ == "__main__":
    unittest.main()

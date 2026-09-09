# Contributing to RIVO

## Setup

Enable the commit message hook and template once after cloning:

```bash
bash scripts/setup-hooks.sh
```

On Windows, run this in Git Bash.

## Commit messages

Use `type(scope): summary`, followed by a blank line and two bullets:

```text
feat(auth): add automatic login

- Reduce repeated sign-ins after restarting the app
- Store tokens and restore the login state
```

- Use ASCII characters throughout the commit message.
- Keep the entire subject within **50 characters**, with no trailing period.
- Include **exactly two bullets**, each within **120 characters** including `- `: why, then what changed.
- **Type:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`.
- **Scope:** a lowercase area or module name, such as `app`, `ui`, `auth`, `data`, `deps`, or `feature-login`.

The hook checks each commit and explains how to fix invalid messages.

## Issues

Start from [the Markdown issue template](ISSUE_TEMPLATE.md). Write a short,
descriptive English title; issue titles do not need the commit subject format.
Keep all seven headings exactly once and in their original order.

- Keep all four issue type options and select exactly one with `[x]`.
- Fill `Summary`, `Tasks`, and `Acceptance Criteria` with actual details.
  Both checklist sections need at least one non-empty item; `[ ]` and `[x]` are accepted.
- Replace template placeholders, `TODO`, and `TBD`; comments do not count as content.
- For bugs, fill the three `Bug Details` fields, including numbered reproduction
  steps, and all three `Android Environment` fields. Use `Unknown: reason` if an
  environment value is unavailable. `N/A` is not allowed for these bug fields.
- For other issue types, use `N/A` for `Bug Details` and `Android Environment`,
  or fill their template fields. Use `N/A` for `References and Notes` if unused.
- Titles and bodies use the same English character filter as PR bodies, including
  the emoji and typographic punctuation exceptions. This checks characters and
  structure; it does not verify English grammar or whether the described facts are true.

Validate before creating or editing an issue, including when using an AI or CLI:

```bash
python3 scripts/validate_issue.py --title 'Restore the session on startup' --body-file /tmp/issue.md
```

The checker needs Python 3.9+ and the companion `scripts/validate_pr.py`; neither
requires third-party packages. Exit codes are `0` for valid content, `1` for format
errors, and `2` for invalid input or an operational error.

The `Issue check` workflow rechecks the current issue on creation, edits, and
reopening. It maintains one GitHub Actions bot comment and adds `needs-info` on
failure. Once corrected, it updates that comment to passed and removes the label.
Initially valid issues receive no bot comment. The workflow creates the repository
label if missing and preserves other labels, user comments, and the issue body.
Reserve `needs-info` for this checker on issues it has flagged.

This is feedback after submission: it does not block issue creation, close issues,
rewrite descriptions, or block PR merges. Do not start implementation until the
linked issue passes the format check. The workflow and its scripts must be on the
default branch, Actions must be enabled, and the workflow needs `issues: write`
permission. No personal access token or AI API key is required.

For an existing issue, or an issue created by automation using `GITHUB_TOKEN`
(which normally does not trigger another workflow), use **Actions > Issue check >
Run workflow** and provide its issue number. Closed issues and PRs are skipped.

## Pull requests

Use the commit subject format for the PR title and fill all five template sections.
`Changes` must contain exactly two single-line ASCII bullets: why, then what changed,
each at most 120 characters including `- `. Replace placeholders; comments do not count as content.
Use `Not run: reason` for skipped verification, `N/A: reason` for no demo impact, and `None` for no risks.
Write in English. The body rejects non-ASCII characters except emoji and supported typographic punctuation,
including in comments and code blocks. Title and `Changes` remain ASCII-only.
This is a character filter, not a vocabulary or grammar check.

Check locally with Python 3.9+ (no dependencies):

```bash
python3 scripts/validate_pr.py --title 'feat(auth): restore login' --body-file /tmp/pr-body.md
```

The `PR format` GitHub Actions check runs on PR creation, edits, and updates.
To block merging on failure, make `PR format` a required status check in the repository's branch rules.
Local Git hooks cannot validate PR edits made on GitHub.

For squash merges, use the PR title and only the two `Changes` bullets as the commit message.
This check validates the PR; it does not generate or validate GitHub's final squash message.

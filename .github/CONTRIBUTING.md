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

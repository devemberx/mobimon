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

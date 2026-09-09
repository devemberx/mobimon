#!/usr/bin/env bash
# Run with: bash scripts/setup-hooks.sh
set -euo pipefail

project_root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
cd "$project_root"
if [ "$(git rev-parse --show-toplevel 2>/dev/null)" != "$project_root" ]; then
    printf 'setup-hooks: Place this script in the scripts directory of a Git repository root.\n' >&2
    exit 1
fi
for required in .githooks/commit-msg .githooks/pre-push .gitmessage; do
    if [ ! -f "$required" ] || [ ! -r "$required" ]; then
        printf 'setup-hooks: Required file is missing or unreadable: %s\n' "$required" >&2
        exit 1
    fi
done

existing_hooks=$(git config --path --get core.hooksPath || true)
if [ -n "$existing_hooks" ] && [ "$existing_hooks" != .githooks ] &&
   [ "$existing_hooks" != "$project_root/.githooks" ]; then
    printf 'setup-hooks: An existing core.hooksPath is configured: %s\n' "$existing_hooks" >&2
    printf 'Decide how to integrate the existing hooks, then follow .github/CONTRIBUTING.md.\n' >&2
    exit 1
fi

chmod +x .githooks/commit-msg .githooks/pre-push
git config --local core.hooksPath .githooks
git config --local commit.template "$project_root/.gitmessage"
# The shared template uses '#'. Pin both settings for old and new Git versions,
# including when a global setting uses 'auto' or a different comment prefix.
git config --local core.commentChar '#'
git config --local core.commentString '#'
printf 'Installed the commit message hook, push hook, and commit template for this repository.\n'
printf 'Set this repository\047s Git comment prefix to # to match the template.\n'

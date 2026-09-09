# Shared AI instructions

- Before development or working on issues, pull requests, or commits, read and follow `.github/CONTRIBUTING.md`.
- For PR merges, follow the [squash merge instructions](.github/CONTRIBUTING.md#squash-merges): use the latest PR title as the commit subject and copy only the two `Changes` bullets verbatim into the body.
- Preserve the bullets' wording and order; omit the section heading, comments, other PR sections, and intermediate commit messages. Correct the PR first if its message needs changing.
- Before merging, verify the required checks for the current PR revision. When using GitHub CLI, explicitly supply `--squash`, `--subject`, `--body-file`, and `--match-head-commit`; do not rely on the generated message or bypass required checks.
- For UI design or changes, also read and follow `docs/DESIGN.md`.

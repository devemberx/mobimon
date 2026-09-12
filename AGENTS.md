# Repository Guidelines

## Workflow

- Before development or Git/GitHub operations, read and follow [CONTRIBUTING.md](.github/CONTRIBUTING.md). It is the source of truth for environment setup, dependency locks, branches, commits, issues, pull requests, required checks, and squash merges.
- Prefer `gh pr merge --squash` when merging and follow the [pull request and merge rules](.github/CONTRIBUTING.md#pull-requests-and-merges).
- Inspect source and build configuration before assuming that a planned module, dependency, test task, or integration exists. Report only verification actually performed.
- Read the relevant sections of [ARCHITECTURE.md](docs/ARCHITECTURE.md) before changing feature behavior or module boundaries.
- Follow [DESIGN.md](docs/DESIGN.md) for UI changes and [TESTING.md](docs/TESTING.md) for behavior changes or tests.
- Before using `ui-ux-pro-max`, read its [project integration rules](.agents/skills/README.md#uiux-design-guidance).
- Mirror subject modules/packages for focused tests; do not require one test file per source file. Update the current requirement map in `TESTING.md` for changed critical behavior.

## Code Quality

- Prefer clear names and structure over explanatory comments.
- Do not add comments that merely restate what the code does.
- Add a comment only when it preserves non-obvious intent, a constraint, a safety condition, or the reason for a workaround. Keep it concise and update or remove it when the code changes.
- Keep required API documentation and externally meaningful contract documentation accurate; the comment rule does not replace those requirements.

## Architecture and Safety

- Preserve the [module boundaries](docs/ARCHITECTURE.md#target-modules-and-dependencies): keep `core-domain` independent of Android, keep feature modules independent of each other and concrete data implementations, and assemble bindings in `app`.
- Keep simulated providers in Debug/demo source sets with explicit labels and separate application IDs, profiles, and databases. Release must report unavailable vehicle data until a verified real adapter is connected; unknown driving state cannot authorize quest commands.
- Route quest rewards through the repository's atomic Room transaction. Preserve evidence validation, ownership/revision checks, and completion uniqueness per reward occurrence; UI code must not award XP or points directly.
- Keep character artwork replaceable through [PetAvatar](core/core-ui/src/main/java/com/monsters/mobimon/core/ui/PetAvatar.kt). Keep rewards, ownership, equipment and interaction state outside the renderer, and limit placeholder artwork work while separate character assets are being prepared.

## Documentation

- Keep one authoritative home per topic: `CONTRIBUTING.md` for workflow and required checks; `ARCHITECTURE.md` for structure and contracts; `DESIGN.md` for UI behavior; `TESTING.md` for test strategy and coverage; `AGENTS.md` for essential agent rules.
- Link to that section or source/configuration instead of repeating commands, versions, settings, test inventories or general tutorials. Repeat only brief safety reminders.
- Revise existing sections instead of appending overlapping explanations. Keep planned contracts separate from current implementation; put task breakdowns, ownership, schedules and run logs in issues/PRs.
- Update the current implementation sections in `ARCHITECTURE.md` and `TESTING.md` when adding modules or integrations.
- Update `CONTRIBUTING.md` and CI together when build or verification commands change.
- Before committing documentation, remove stale or duplicate guidance and verify relative links, including heading anchors. Preserve safety contracts and verification limits when shortening.
- Local plans under `docs/superpowers/` remain ignored and must not be committed.
- When updating shared skills, edit `.agents/skills/`, then copy the entire affected skill folder, including references and licenses, to `.claude/skills/`. Keep both copies identical in the same commit.

## Verification

Run the [canonical checks](.github/CONTRIBUTING.md#verification) for the change type, including device checks when available. Report only the layers actually executed; APK assembly and Robolectric do not establish device-test execution.

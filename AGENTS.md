# Repository Guidelines

## Workflow

- Before development or Git/GitHub operations, read and follow [CONTRIBUTING.md](.github/CONTRIBUTING.md). It is the source of truth for environment setup, dependency locks, branches, commits, issues, pull requests, required checks, and squash merges.
- Inspect source and build configuration before assuming that a planned module, dependency, test task, or integration exists. Report only verification actually performed.
- Read the relevant sections of [ARCHITECTURE.md](docs/ARCHITECTURE.md) before changing feature behavior or module boundaries.
- Follow [DESIGN.md](docs/DESIGN.md) for UI changes and [TESTING.md](docs/TESTING.md) for behavior changes or tests.
- Mirror subject modules/packages for focused tests; do not require one test file per source file. Update the current requirement map in `TESTING.md` for changed critical behavior.

## Code Quality

- Prefer clear names and structure over explanatory comments.
- Do not add comments that merely restate what the code does.
- Add a comment only when it preserves non-obvious intent, a constraint, a safety condition, or the reason for a workaround. Keep it concise and update or remove it when the code changes.
- Keep required API documentation and externally meaningful contract documentation accurate; the comment rule does not replace those requirements.

## Architecture and Safety

- Preserve the [module boundaries](docs/ARCHITECTURE.md#target-modules-and-dependencies): keep `core-domain` independent of Android, keep feature modules independent of each other and concrete data implementations, and assemble bindings in `app`.
- Keep simulated providers in Debug/demo source sets with explicit labels and separate application IDs, profiles, and databases. Release must report unavailable vehicle data until a verified real adapter is connected; unknown driving state cannot authorize quest commands.
- Route quest rewards through the repository's atomic Room transaction. Preserve evidence validation, ownership/revision checks, and completion uniqueness when extending progression; UI code must not award XP directly.
- Keep character artwork replaceable through [PetAvatar](core/core-ui/src/main/java/com/monsters/mobimon/core/ui/PetAvatar.kt). Keep progression and interaction state outside the renderer, and limit placeholder artwork work while separate character assets are being prepared.

## Documentation

- Update the current implementation sections in `ARCHITECTURE.md` and `TESTING.md` when adding modules or integrations.
- Update `CONTRIBUTING.md` and CI together when build or verification commands change.
- Local plans under `docs/superpowers/` remain ignored and must not be committed.
- When updating shared skills, edit `.agents/skills/`, then copy the entire affected skill folder, including references and licenses, to `.claude/skills/`. Keep both copies identical in the same commit.

## Verification

For code or build changes, run the canonical checks from the repository root:

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck lintDebug testDebugUnitTest :core:core-domain:test :core:core-vss:test :app:assembleDebug :app:assembleDebugAndroidTest :core:core-database:assembleDebugAndroidTest
git diff --check
```

On Windows, use `gradlew.bat` instead of `./gradlew`. For documentation-only changes, verify the content and relative links, then run `git diff --check`; an Android build is unnecessary. See [CONTRIBUTING.md](.github/CONTRIBUTING.md) for setup, dependency-lock updates, device checks, and PR requirements.

With a compatible connected device, run `./gradlew :core:core-database:connectedDebugAndroidTest :app:connectedDebugAndroidTest`. CI requires both local and AAOS API 34-ext9 device jobs through `Android checks`. Report device tests as unrun when only APK assembly or Robolectric ran.

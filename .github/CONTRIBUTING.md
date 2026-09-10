# Contributing to MobiMon

This guide covers the shortest path from a fresh checkout to a verified change.
Use the linked architecture, design, and testing documents when your change needs
more context.

## Quick start

Install Android Studio with JDK 17, Android SDK Platform 34, and SDK Build Tools
34.0.0. Use the repository's Gradle Wrapper; do not install a separate Gradle.

1. Point Android Studio's **Gradle JDK** and your terminal's `JAVA_HOME` to JDK 17.
2. Set the SDK path in the ignored `local.properties` file or with `ANDROID_HOME`.
3. Install the repository hooks:

   ```bash
   bash scripts/setup-hooks.sh
   ```

4. Confirm the environment and build the Debug APK:

   ```bash
   ./gradlew --version
   ./gradlew :app:assembleDebug
   ```

On Windows, use `gradlew.bat` instead of `./gradlew`. The Debug APK is written to
`app/build/outputs/apk/debug/` and installs as the separate simulated `.demo`
application.

## Before changing code

- Create or use an issue based on [ISSUE_TEMPLATE.md](ISSUE_TEMPLATE.md). Do not
  start implementation until the linked issue passes its format check.
- Read the relevant parts of [ARCHITECTURE.md](../docs/ARCHITECTURE.md),
  [DESIGN.md](../docs/DESIGN.md), and [TESTING.md](../docs/TESTING.md).
- Confirm the module, dependency, task, or integration in source and build files;
  design documents may describe work that is not implemented yet.

Validate an issue locally with Python 3.9 or newer:

```bash
python3 scripts/validate_issue.py --title 'Restore the session on startup' --body-file /tmp/issue.md
```

## Verification

For code or build changes, format first and review the resulting diff:

```bash
./gradlew ktlintFormat
./gradlew ktlintCheck lintDebug testDebugUnitTest :core:core-domain:test :core:core-vss:test :app:assembleDebug
git diff --check
```

Add or update tests for changed behavior. Follow [TESTING.md](../docs/TESTING.md)
for test placement and Fakes. Check affected flows on a device or emulator when
changing UI, permissions, or platform integrations.

For documentation-only changes, verify the content and relative links and run
`git diff --check`; an Android build is unnecessary. Record every check performed,
including skipped device or integration checks and their reasons, in the PR.

## Dependency changes

- Declare exact library and plugin versions in `gradle/libs.versions.toml`.
- Use version-catalog aliases from module build files.
- Keep dependency locking enabled and commit every affected `gradle.lockfile` and
  `settings-gradle.lockfile` change with the build change.

Regenerate and inspect lock state before running the normal verification commands:

```bash
./gradlew :app:dependencies :core:core-domain:dependencies :core:core-database:dependencies :core:core-vss:dependencies :core:core-ui:dependencies :feature:feature-pet:dependencies :feature:feature-quest:dependencies :feature:feature-vehicle-info:dependencies --write-locks
```

Add the corresponding `:<module>:dependencies` task when introducing a module.
Never run normal CI verification with `--write-locks`.

## Branches and commits

Create one branch per topic from an up-to-date `main`. Use
`<type>/<short-kebab-summary>`, keep the full name within 50 characters, and use
lowercase ASCII letters, digits, and single hyphens. Valid types are `feat`, `fix`,
`docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, and `revert`.

Commit messages use an ASCII subject of at most 50 characters followed by exactly
two ASCII bullets: why the change is needed, then what changed. Each bullet must
be at most 120 characters including `- `.

```text
feat(auth): restore login

- Reduce repeated sign-ins after restarting the app
- Store tokens and restore the login state
```

The hooks enforce branch and commit formats and block direct pushes to `main`.

## Pull requests and merges

Use [PULL_REQUEST_TEMPLATE.md](PULL_REQUEST_TEMPLATE.md) without removing its
sections. Write the title and body in English. The PR title follows the commit
subject format, and `Changes` contains exactly two single-line ASCII bullets:
why, then what changed.

Validate the PR before creating or updating it:

```bash
python3 scripts/validate_pr.py --title 'feat(auth): restore login' --body-file /tmp/pr-body.md
```

Before merging, confirm `PR format` and `Android checks` passed for the current
revision. Use squash merge. Copy the PR title exactly as the squash commit subject
and copy only the two `Changes` bullets as its body; do not add generated text or
rewrite them during the merge.

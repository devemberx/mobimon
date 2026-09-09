# Contributing to RIVO

## Development environment

| Component | Pinned version |
| --- | --- |
| Build JDK / JVM target | 17 |
| Gradle Wrapper | 8.7 |
| Android Gradle Plugin | 8.5.2 |
| Kotlin / Compose Compiler | 2.0.21 |
| compileSdk / targetSdk / initial minSdk | 34 / 34 / 34 |
| Compose BOM | 2024.06.00 |
| ktlint Gradle plugin / engine | 12.1.2 / 1.3.1 |
| Kover | 0.8.3 |

Install Android Studio with support for AGP 8.5, Temurin JDK 17, Android SDK Platform 34, and SDK Build Tools 34.0.0. Point Android Studio's **Gradle JDK** and your terminal's `JAVA_HOME` at the same JDK 17 installation. Android Studio itself can use its bundled runtime. Local SDK paths belong in the ignored `local.properties` (`sdk.dir=/your/sdk/path`) or the `ANDROID_HOME` environment variable.

Use `./gradlew --version` to confirm the build JDK and Wrapper version. Do not install a separate system Gradle. On Windows, use `gradlew.bat` in place of `./gradlew`. Run `bash scripts/setup-hooks.sh` once, in Git Bash on Windows, to install the repository's existing commit checks.

The API 34 minimum is an initial assumption for the supplied Android 14 environment. Confirm this, vehicle permissions, signing requirements, and manifest configuration against the provided AAOS sample before device integration. This launcher does not yet access vehicle APIs or declare driving-state behavior.

## Before opening or updating a PR

For code or build changes, run these commands from the repository root before
pushing. On Windows, replace `./gradlew` with `gradlew.bat`.

1. Apply the shared Kotlin formatting rules and review the resulting diff:

   ```bash
   ./gradlew ktlintFormat
   ```

2. Run the same checks as Android CI:

   ```bash
   ./gradlew ktlintCheck :app:lintDebug :app:testDebugUnitTest :app:assembleDebug
   ```

3. Inspect formatting, Lint, and test reports under the corresponding
   `build/reports/` directories. Fix failures and review warnings that affect the
   change. The Debug APK is under `app/build/outputs/apk/debug/`.
4. Add or update tests for changed behavior. JUnit 4 and `kotlinx-coroutines-test`
   are configured for `app/src/test/`. No behavior tests exist yet; `NO-SOURCE`
   means no tests ran. Prioritize reward calculations, state transitions,
   asynchronous responses, and persistence behavior as those features are added.
5. Check affected user flows on a device or emulator when changing UI,
   permissions, or platform integrations. Vehicle APIs require the supplied AAOS
   environment. Debug CI does not verify device behavior or a Release/R8 build;
   validate the actual submission artifact before the demo.
6. Review `git diff --check` and the changed files. Record checks performed and
   any device or integration checks not run, with reasons, in the PR's
   `Verification` section. Validate the PR format as described below.

For dependency changes, follow the lockfile procedure below before running the
checks. For documentation-only changes, check content, relative links, and
`git diff --check`; a local Android build is unnecessary. GitHub still runs the
required PR checks.

Coverage reports are optional locally and are generated on main and manual CI
runs:

```bash
./gradlew :app:koverHtmlReportDebug :app:koverXmlReportDebug
```

Kover measures local JVM tests, not on-device coverage. Reports are informational;
there is no minimum percentage gate. CI also omits detekt, a blanket
warnings-as-errors setting, and emulator tests for this initial development
phase. Android Lint errors fail CI; warnings remain visible in the report.

## Dependency management

- Declare library and plugin versions in `gradle/libs.versions.toml`; reference the aliases from module build files.
- `app/gradle.lockfile` records the resolved application/test compile and runtime dependencies, including transitive dependencies for Debug and Release variants. Locking is strict: missing or inconsistent lock state fails dependency resolution.
- `settings-gradle.lockfile` is Gradle's generated lock state for importing the local version catalog. Keep it with the application lock when regenerating dependencies.
- Gradle/plugin tool classpaths are outside this application lock. Plugin versions are pinned in the catalog, ktlint's engine version is pinned explicitly, and the Wrapper distribution includes a SHA-256 checksum. This is not a claim that the application lock covers every build tool.
- Compose versions are aligned with a pinned BOM. New dependencies must support compileSdk 34 and the selected Kotlin version. Add Room, KSP, and service SDKs when their features need them, after checking compatibility with the supplied environment.

To change dependencies:

1. Edit the version catalog and module declarations. Use exact versions, not `+` or snapshots.
2. Generate the lock state locally:

   ```bash
   ./gradlew :app:dependencies --write-locks
   ```

3. Inspect the lock diff for unexpected transitive upgrades. Run the required checks **without** `--write-locks`.
4. Commit the catalog/build changes and `app/gradle.lockfile` together. For new modules, enable locking and generate their own lock files.

Normal CI consumes committed locks; it does not update them or run `clean` on every build. The Gradle action owns its dependency/build cache.

## CI and merge policy

During initial development, merging requires successful `PR format` and
`Android checks` statuses, with **zero required review approvals**.

- PR creation and code updates run formatting, Android Lint, local unit-test
  tasks, and Debug APK assembly.
- PR title/body edits rerun the PR-format workflow.
- Pushes to main and manual Android CI runs also retain the Debug APK for 14 days
  and generate coverage reports.
- CI preserves available reports even after a failure, for 7 days. Superseded
  Android CI runs are cancelled. Android checks use one Ubuntu job.

CI does not call real AI or vehicle services. Branch protection is configured in
GitHub separately from the workflow YAML; keep the required status names stable.

## Documentation

Keep README focused on what users can do with the Android app and its current
availability. Put developer setup, dependency management, and checks to run after
code changes in this guide. Update the guide when commands or workflows change.
Local agent planning and design documents under `docs/superpowers/` are ignored
and must not be committed.

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

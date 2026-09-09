# RIVO

Android foundation for the AI DrivePet hackathon. The app currently contains only a minimal Compose launcher showing the project name. The [interactive concept](docs/ui/home.html) and [design overview](docs/DESIGN.md) describe the planned product.

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

## Build and checks

```bash
# Apply the shared Kotlin formatting rules locally.
./gradlew ktlintFormat

# The required Android CI checks.
./gradlew ktlintCheck :app:lintDebug :app:testDebugUnitTest :app:assembleDebug

# Optional local coverage reports, also generated on main and manual CI runs.
./gradlew :app:koverHtmlReportDebug :app:koverXmlReportDebug
```

The APK is under `app/build/outputs/apk/debug/`. Lint, formatting, test, and coverage reports are under the corresponding `build/reports/` directories. CI preserves reports after failures and retains successful main/manual APKs for 14 days.

JUnit 4 and `kotlinx-coroutines-test` are configured for `app/src/test/`. **No behavior tests exist yet:** the unit-test task may report `NO-SOURCE`, and initial coverage is not evidence of tested behavior. Add tests alongside quest rewards, vehicle state interpretation, asynchronous AI responses, and memory handling as those features are implemented. Use actual Room integration tests when persistence is introduced. Device/AAOS execution requires separate verification.

There is no detekt, blanket warnings-as-errors setting, coverage percentage gate, or emulator matrix. Android Lint errors fail CI; warnings remain visible in its report. Kover measures local JVM tests, not on-device test coverage. CI never calls a real AI or vehicle service.

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

## Collaboration and CI

The initial merge policy requires the `PR format` and `Android checks` statuses and **zero review approvals**. PRs remain the place to record changes and verification. See [CONTRIBUTING](.github/CONTRIBUTING.md) for issue, PR, and commit conventions.

- PR creation/code updates: formatting, Android Lint, local unit tests, and Debug APK assembly.
- PR title/body edits: only the existing PR-format workflow reruns.
- Pushes to main: Android checks plus APK retention and coverage reports.
- Manual Android CI runs: the same checks and artifacts for the selected branch.
- Superseded Android CI runs are cancelled. There is one Ubuntu job and no required reviewer.

Branch protection is a GitHub repository setting, separate from the YAML. Keep the required status names stable. If a submission uses a Release/R8 build, validate and install that artifact early; Debug CI alone does not verify it.

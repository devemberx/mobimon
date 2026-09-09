# Android dependency and CI foundation

The user approved the dependency and CI design in the conversation and requested implementation with CI-only merge requirements. Review approvals are not required during initial development.

## Scope

Create one buildable `:app` Android application with a minimal Compose entry point so that the agreed CI tasks run against a real APK. Product screens, vehicle integration, AI, persistence behavior, and invented sample business logic are outside this change.

Use JDK 17, Gradle 8.7, AGP 8.5.2, and Kotlin / Compose Compiler 2.0.21. Assume compileSdk, targetSdk, and the initial supported device baseline are API 34 until the supplied AAOS sample establishes additional requirements. Centralize dependency and plugin versions in `gradle/libs.versions.toml`, commit the official Gradle Wrapper and a generated dependency lock file, and reject missing lock state for the application and test compile/runtime classpaths.

Use ktlint 1.3.1 through its Gradle plugin 12.1.2, Android Lint, JUnit 4.13.2, kotlinx-coroutines-test 1.8.1, and Kover 0.8.3. Compose dependencies use BOM 2024.06.00, Core 1.13.1, and Activity Compose 1.9.0 to retain compileSdk 34 compatibility. Do not introduce detekt, blanket warnings-as-errors, a coverage threshold, an emulator matrix, or vacuous tests just to make test counts nonzero.

## CI and merge policy

Keep the existing PR-format workflow. Add an Android workflow on PR code events, pushes to main, and manual dispatch. Run formatting checks, Android Lint, local unit tests, and Debug APK assembly in one Ubuntu job. Preserve reports even on failure. On main/manual runs also retain the APK and produce coverage reports without enforcing a percentage. Cancel superseded runs and use the Gradle action's built-in cache.

Require `PR format` and `Android checks` for main, with zero mandatory review approvals. Inspect existing repository protection first and preserve unrelated settings. Publish a reviewable branch/PR and verify its CI before enabling a new required check. Do not merge the implementation automatically.

## Verification

Run the exact CI Gradle tasks and Debug Kover report tasks. Verify style rejection with a temporary formatting violation and lock enforcement with a temporary dependency version mismatch, restoring both afterward. Validate the workflow using actionlint and confirm an actual GitHub Actions run. A buildable skeleton has no behavior tests yet; document this explicitly rather than reporting empty test tasks as passing tests. Device execution and AAOS integration remain unverified.

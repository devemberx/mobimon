# Android CI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Deliver reproducible Android builds and CI-only merge requirements for the four-person hackathon team.

**Architecture:** A single app module owns the application and test classpaths. Root Gradle scripts centralize plugin declarations and Kotlin formatting; the app module owns Android Lint and coverage. One Android CI job complements the existing PR-format workflow.

**Tech Stack:** JDK 17, Gradle 8.7, AGP 8.5.2, Kotlin 2.0.21, Compose, ktlint, JUnit, Kover, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-09-android-ci-design.md`

## Global Constraints

- compileSdk = 34; targetSdk = 34; initial minSdk = 34.
- Gradle 8.7; AGP 8.5.2; Kotlin and Compose Compiler 2.0.21; JDK 17.
- No detekt, coverage percentage gate, mandatory reviews, or fabricated tests.
- Preserve existing issue/PR validators and the HTML prototype.
- No automatic merge to main.

## Task 1: Reproducible Android build and dependency checks

**Files:** Create `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, the official Wrapper files, `.editorconfig`, `app/build.gradle.kts`, `app/gradle.lockfile`, `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/devemberx/rivo/MainActivity.kt`, and `app/src/main/res/values/strings.xml`. Update `.gitignore` and `.gitattributes` for local build output and Wrapper line endings.

**Interface:** Expose `ktlintCheck`, `ktlintFormat`, `:app:lintDebug`, `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:koverHtmlReportDebug`, and `:app:koverXmlReportDebug`.

- [ ] Install a temporary official JDK/SDK/Gradle toolchain and generate the official Gradle 8.7 Wrapper with its distribution SHA-256.
- [ ] Create the single-module build using the exact versions in the spec and the minimal launcher activity.
- [ ] Activate dependency locking for app/test compile and runtime configurations with strict missing-state enforcement; generate with `./gradlew :app:dependencies --write-locks` and commit the resulting real lock state.
- [ ] Run `./gradlew ktlintCheck :app:lintDebug :app:testDebugUnitTest :app:assembleDebug :app:koverHtmlReportDebug :app:koverXmlReportDebug`.
- [ ] Temporarily break formatting and request a conflicting locked dependency; verify failures and restore the files. Do not add permanent tests that just mirror build configuration.

## Task 2: CI, onboarding, and merge requirements

**Files:** Create `.github/workflows/android-ci.yml` and `README.md`; update `.github/CONTRIBUTING.md` with commands and the approved merge policy.

**Interface:** Stable GitHub check name `Android checks`; existing `PR format`; main requires both and no review approval.

- [ ] Configure PR/main/manual triggers, a single JDK 17 / SDK 34 job, built-in Gradle caching, cancellation, report uploads, main/manual APK retention, and coverage reporting.
- [ ] Document dependency updates, lock regeneration, IDE/CLI JDK alignment, the absence of initial behavior tests, and AAOS sample validation still required.
- [ ] Validate workflow syntax with actionlint and rerun the exact Gradle checks after changes.
- [ ] Request an independent code review while completing local verification and fix material findings.
- [ ] Create a compliant commit and draft PR, check GitHub CI, and configure main to require `Android checks` and `PR format` with zero mandatory approvals.
- [ ] Report actual verification results, the PR URL, and any remaining environment limitations.

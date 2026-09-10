# Testing strategy

Use this guide to choose tests and understand what they establish. Environment
setup, required checks, dependency changes and PR reporting belong in
[CONTRIBUTING.md](../.github/CONTRIBUTING.md#verification). Library versions are
defined in the [version catalog](../gradle/libs.versions.toml).

## Current setup and test locations

The project uses JUnit 4, coroutines-test, Robolectric, Compose Testing, Hilt
integration tests and Room device tests. Kover reports local JVM coverage with
no percentage gate. Screenshot and system-UI automation are not configured.

| Location | Scope and host |
| --- | --- |
| `core/core-domain/src/test/kotlin` | Growth and evidence rules; plain JVM |
| `core/core-vss/src/test/kotlin` | Unavailable real vehicle contract; plain JVM |
| `core/core-database/src/test/java` | Native SQLite constraints, transactions, concurrency, file reopening and DataStore; Robolectric |
| `core/core-database/src/androidTest/java` | SQLite transaction and file-reopening counterparts; device |
| Feature `src/test/java` | Constructor-injected ViewModels and Compose state/callback contracts; JVM or Robolectric |
| `app/src/test/java`, `src/testDebug/java` | Runtime, shell, Q01 repository integration, branding and demo provider behavior |
| `app/src/journeyTest/java` | Shared Q01 UI journeys through MainActivity, Hilt and Room; Robolectric and device |

The shared journey directory is explicitly included in both test source sets by
[app/build.gradle.kts](../app/build.gradle.kts). The instrumentation runner lives
in `app/src/androidTest/java`; local host settings live in
`app/src/testDebug/resources/robolectric.properties`. Other test directories are
not automatically shared across source sets or modules.

## Writing tests

- Put focused tests in the subject's module and package, using `SubjectTest`;
  name integration tests after the behavior. Do not require one test file per
  source file or tests that merely repeat implementation details.
- Keep helpers module-local until sharing is needed. Use fresh, controllable
  Fakes for external dependencies; production and demo APKs must not depend on
  test helpers. Shared `core-testing` is not implemented or required.
- Construct ordinary unit-test subjects directly. Reserve Hilt for integration
  tests. A small Fake is preferred to a mocking framework when it can represent
  success, failure, pending work and obsolete results.
- Use `runTest`, one shared test scheduler and injected clocks. Install/reset
  Main for local `viewModelScope` tests; retain real Main in instrumented tests.
  Virtual coroutine time does not advance a separate clock. Avoid real sleeps.
- Observe pending states before advancing time; collect subscription-driven
  flows before emitting. Propagate cancellation and test late replies where
  identity checks protect state. Close databases, cancel jobs and restore
  dispatchers after each test.
- Prove SQL constraints and rollback with real Room. Use disposable file-backed
  databases for reopening and exported schemas for migration tests. Do not use
  destructive migration for user progress.
- Test Compose screens through state and callbacks using semantics; use
  `testTag` when semantics are insufficient. Cover meaningful back, focus and
  enabled-action behavior. Add screenshot baselines when visuals stabilize.

See the official [coroutine testing](https://developer.android.com/kotlin/coroutines/test),
[Room testing](https://developer.android.com/training/data-storage/room/testing-db),
[Compose testing](https://developer.android.com/develop/ui/compose/testing) and
[Hilt testing](https://developer.android.com/training/dependency-injection/hilt-testing)
guides for API setup.

## Current requirement map

Keep changed critical behavior mapped to an implemented suite. These links
identify coverage; execution reports establish whether it passed at a revision.

| Requirement | Test suite | Scope |
| --- | --- | --- |
| Fixed XP growth boundaries | [RewardCalculatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/RewardCalculatorTest.kt) | JVM |
| Concurrent completion awards once; failures roll back all reward writes | [RoomCompanionRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/RoomCompanionRepositoryTest.kt), [device counterpart](../core/core-database/src/androidTest/java/com/monsters/mobimon/core/database/RoomCompanionRepositoryAndroidTest.kt) | Local SQLite and device |
| Committed appearance, XP and completion survive file reopening | Same Room suites | File persistence |
| Old, invalid, wrong-source or wrong-epoch evidence is rejected | [QuestEvaluatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/QuestEvaluatorTest.kt), [QuestViewModelTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestViewModelTest.kt) | JVM and ViewModel |
| UI acknowledgment awards 80 XP once and retains it after Activity recreation | [Q01AppJourneyTest](../app/src/journeyTest/java/com/monsters/mobimon/Q01AppJourneyTest.kt) | Hilt, MainActivity and Room; local and device |
| Unavailable, unknown or moving state blocks starting; parked recovery enables it | Same app journey suite | Local and device |
| Failed settings/appearance saves preserve committed state; cancellation propagates | [PetViewModelTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/PetViewModelTest.kt), [DataStoreSettingsRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/DataStoreSettingsRepositoryTest.kt) | Local |
| Detail back returns to menu; close dismisses the drawer | [MobiMonContentTest](../app/src/test/java/com/monsters/mobimon/ui/MobiMonContentTest.kt) | Compose and Robolectric |
| Repeated foreground notifications do not duplicate a vehicle connection | [CompanionRuntimeTest](../app/src/test/java/com/monsters/mobimon/runtime/CompanionRuntimeTest.kt) | Runtime unit test |
| Real progression rejects simulated evidence; real adapter reports unavailable | Room local suite, [UnavailableVehicleRepositoryTest](../core/core-vss/src/test/kotlin/com/monsters/mobimon/core/vss/UnavailableVehicleRepositoryTest.kt) | JVM and local SQLite |
| Debug application ID and launcher label match MobiMon | [BrandingTest](../app/src/testDebug/java/com/monsters/mobimon/BrandingTest.kt) | Debug and Robolectric |

## Integration boundaries

The app journeys use `HiltTestApplication`. Their
[JourneyTestModule](../app/src/journeyTest/java/com/monsters/mobimon/testing/JourneyTestModule.kt)
replaces platform and vehicle providers with a fixed clock, controllable
simulated vehicle, in-memory Room and isolated DataStore. MainActivity, feature
ViewModels, repository bindings and reward transactions remain real.

Activity recreation, file-backed database reopening and process restart prove
different guarantees. The journeys cover recreation; the Room suites cover file
persistence. Production Application lifecycle, process restart, Release behavior,
custom CSTD services and real vehicle/AI integration still require separate
acceptance. APK assembly or a task with `NO-SOURCE` does not prove test execution.

Define acceptance cases for future features in their implementation issues using
the [planned design contracts](ARCHITECTURE.md#planned-features). Extend the map
above when those tests exist. Device acceptance records should identify the
image, signal source, scenario and outcome without credentials or private logs.

## Focused commands and reports

Run from the repository root; on Windows use `gradlew.bat`. The full required
check sequence and connected-test command are in
[CONTRIBUTING.md](../.github/CONTRIBUTING.md#verification).

```bash
# Pure JVM modules.
./gradlew :core:core-domain:test :core:core-vss:test

# A feature's local tests.
./gradlew :feature:feature-quest:testDebugUnitTest

# Shared Q01 journeys on Robolectric.
./gradlew :app:testDebugUnitTest --tests com.monsters.mobimon.Q01AppJourneyTest

# Informational local coverage.
./gradlew :app:koverHtmlReportDebug :app:koverXmlReportDebug
```

An app test task does not run its library dependencies' tests. When adding
modules, include their local tests, Lint and meaningful coverage in the build/CI.

[Android CI](../.github/workflows/android-ci.yml) uploads `local-reports-*` and
`device-reports-*`, including on failure. Device artifacts include host properties,
features and display metrics under `build/reports/aaos/`. Kover reports are
generated on main pushes and cover local JVM tests only.

### CI AAOS environment

The workflow selects the official Google APIs image
`system-images;android-34-ext9;android-automotive;x86_64`. Display and heap settings
come from [cstd.ini](../.github/avd/cstd.ini); CPU, RAM and data partition settings
are in the workflow. The required [host check](../scripts/check-aaos-environment.sh)
verifies automotive support, API, ABI, resolution and density before device tests.

The supplied local `setting/cstd/x86_64/` metadata identifies the same API,
extension and ABI at image revision 5. Its companion
`setting/CSTDe_API_34.avd/config.ini` supplies the reference display/hardware
settings. These files are not CI inputs. CI uses a disposable 6 GB data partition
instead of the supplied 30 GB, headless software rendering, a fresh AVD and
disabled animations. The SDK resolves the available image revision; matching
metadata does not establish image binary or custom CSTD service equivalence.

To reproduce locally, install the image above with `sdkmanager`, use emulator
35.1.9 or newer, and create an Automotive AVD with the settings from the workflow
and `cstd.ini`. Boot the supplied AVD for CSTD-specific acceptance. With exactly
the intended emulator connected, run `bash scripts/check-aaos-environment.sh`,
then the connected-test command in CONTRIBUTING. On Windows, use Git Bash for
the host check. Passing it identifies the host; connected-test results establish
that the Room and app journeys ran.

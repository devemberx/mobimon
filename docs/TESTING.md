# Testing strategy

This document owns test strategy, coverage mapping and verification limits.
Use [CONTRIBUTING.md](../.github/CONTRIBUTING.md#verification) for setup and required
checks, [DESIGN.md](DESIGN.md) for UX, and [ARCHITECTURE.md](ARCHITECTURE.md) for
technical contracts. Record test executions in the implementation issue or PR.

## Current setup and test locations

The project uses JUnit 4, coroutines-test, Robolectric, Compose Testing, Hilt
integration tests and Room device tests. Kover reports local JVM coverage with
no percentage gate. Screenshot and system-UI automation are not configured.

| Location | Scope and host |
| --- | --- |
| `core/core-domain/src/test/kotlin` | Legacy reward rules, evidence and per-signal freshness; plain JVM |
| `core/core-vss/src/test/kotlin` | Unavailable real vehicle contract; plain JVM |
| `core/core-database/src/test/java` | Native SQLite constraints, transactions, concurrency, file reopening and DataStore; Robolectric |
| `core/core-database/src/androidTest/java` | SQLite transaction and file-reopening counterparts; device |
| Feature `src/test/java` | Constructor-injected ViewModels and Compose state/callback contracts; JVM or Robolectric |
| `app/src/test/java`, `src/testDebug/java` | Runtime, shell, Q01 repository integration, branding and demo provider behavior |
| `app/src/journeyTest/java` | Shared Q01 UI journeys through MainActivity, Hilt and Room; Robolectric and device |

The journey source sets and instrumentation runner are configured in
[app/build.gradle.kts](../app/build.gradle.kts); local host settings are in
[robolectric.properties](../app/src/testDebug/resources/robolectric.properties).
Other test directories are not automatically shared across source sets or modules.

## Writing tests

- Place focused `SubjectTest` suites in the subject's module and package; name
  integration tests after behavior. Test contracts, not one file per source file.
- Construct unit-test subjects directly and reserve Hilt for integration tests.
  Use fresh, controllable Fakes for external dependencies. Keep helpers
  module-local and out of production/demo APKs; `core-testing` is not implemented.
- Use `runTest`, a shared scheduler and injected clocks rather than real sleeps.
  Virtual coroutine time does not advance a separate clock. Install/reset Main
  for local `viewModelScope` tests; retain real Main on devices.
- Observe pending states and start collectors before emitting. Cover cancellation
  and late results, then close databases, cancel jobs and restore dispatchers.
- Verify constraints and rollback with real Room, reopening with disposable
  file-backed databases, and migrations with exported schemas. Do not replace
  migration tests with destructive reset.
- Exercise Compose state/callback contracts through semantics, including back,
  focus and enabled actions. Use `testTag` when semantics are insufficient; add
  screenshot baselines when visuals stabilize.

## Current requirement map

Map changed critical behavior to implemented suites. The point economy and
full-screen shell now have tests; the production quest and paid-item catalogs
remain undefined. Future cases follow [DESIGN.md](DESIGN.md) and the
[planned contracts](ARCHITECTURE.md#planned-features). Suite links identify
coverage, not a passing result at a particular revision.

| Requirement | Test suite | Scope |
| --- | --- | --- |
| Fixed XP growth boundaries | [RewardCalculatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/RewardCalculatorTest.kt) | JVM |
| Concurrent completion awards once; failures roll back all reward writes | [RoomCompanionRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/RoomCompanionRepositoryTest.kt), [device counterpart](../core/core-database/src/androidTest/java/com/monsters/mobimon/core/database/RoomCompanionRepositoryAndroidTest.kt) | Local SQLite and device |
| Committed appearance, XP and completion survive file reopening | Same Room suites | File persistence |
| Old, invalid, wrong-source or wrong-epoch evidence is rejected | [QuestEvaluatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/QuestEvaluatorTest.kt), [QuestViewModelTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestViewModelTest.kt) | JVM and ViewModel |
| Superseded parking or AAOS app-use restriction rejects queued reward writes | [Q01JourneyTest](../app/src/test/java/com/monsters/mobimon/Q01JourneyTest.kt) | Local Room transaction |
| Parking and battery age are evaluated independently | [VehicleFreshnessPolicyTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/VehicleFreshnessPolicyTest.kt), [VehicleInfoScreenTest](../feature/feature-vehicle-info/src/test/java/com/monsters/mobimon/feature/vehicle/VehicleInfoScreenTest.kt) | Domain and Compose |
| Point migration preserves XP evidence without minting points | [PointEconomyMigrationTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/PointEconomyMigrationTest.kt) | File-backed v1 to v2 Room |
| Purchases charge once, equipment is separate, and one-time/daily credits have unique occurrences | [PointEconomyRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/PointEconomyRepositoryTest.kt) | Local SQLite |
| A loading or failed point balance is distinct from saved zero | [PointBalanceViewModelTest](../app/src/test/java/com/monsters/mobimon/ui/PointBalanceViewModelTest.kt) | ViewModel |
| Failed inventory observation can be retried without losing a saved selection | [CosmeticInventoryViewModelTest](../app/src/test/java/com/monsters/mobimon/ui/CosmeticInventoryViewModelTest.kt), [CustomizationScreenTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/CustomizationScreenTest.kt) | ViewModel and Compose |
| Previewing Luna does not apply her until the user confirms | [CustomizationScreenTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/CustomizationScreenTest.kt) | Compose |
| UI acknowledgment awards 80 XP once and retains it after Activity recreation | [Q01AppJourneyTest](../app/src/journeyTest/java/com/monsters/mobimon/Q01AppJourneyTest.kt) | Hilt, MainActivity and Room; local and device |
| Quest screen offers Q01 without advertising legacy Q02/Q03 | [QuestScreenTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestScreenTest.kt) | Compose and Robolectric |
| Settings screen does not promise personal memory management | [PetPreferencesScreenTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/PetPreferencesScreenTest.kt) | Compose and Robolectric |
| Unavailable, unknown or moving state blocks starting; parked recovery enables it | Same app journey suite | Local and device |
| Failed settings/appearance saves preserve committed state; cancellation propagates | [PetViewModelTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/PetViewModelTest.kt), [DataStoreSettingsRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/DataStoreSettingsRepositoryTest.kt) | Local |
| Selecting a menu destination closes the overlay and uses a full-screen route; back returns home | [MobiMonContentTest](../app/src/test/java/com/monsters/mobimon/ui/MobiMonContentTest.kt), [ShellStateTest](../app/src/test/java/com/monsters/mobimon/ui/ShellStateTest.kt) | Compose and JVM |
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

Define future acceptance cases and record executions in their implementation
issues/PRs; extend the map above when suites exist. Reports must identify the
revision, executed layers, skipped checks and reasons. Device records also need
the image, signal source, scenario and outcome, without credentials or private logs.
Before enabling launcher character support, verify that new and existing installs
start with launcher visibility off even when the in-app preview preference is on.
The current DataStore test covers the separate off-by-default preference; it does
not verify a launcher surface. `CarAppUseMonitor` uses the current display's UX
restrictions, but its callbacks, system blocking behavior and reconnection must
be tested on the target AAOS image. No Copilot SDK/CLI runtime or launcher OEM
contract is connected, so these experiences remain unavailable and have no
end-to-end acceptance result.

## Focused commands and reports

Run from the repository root; on Windows use `gradlew.bat`. The full required
check sequence and connected-test command are in
[CONTRIBUTING.md](../.github/CONTRIBUTING.md#verification).

```bash
# Shared Q01 journeys on Robolectric.
./gradlew :app:testDebugUnitTest --tests com.monsters.mobimon.Q01AppJourneyTest

# Informational local coverage.
./gradlew :app:koverHtmlReportDebug :app:koverXmlReportDebug
```

The filtered journey command does not run device tests. App test tasks do not
run their library dependencies' tests or replace the required check sequence.

[Android CI](../.github/workflows/android-ci.yml) defines report paths, upload
conditions and retention for `local-reports-*` and `device-reports-*`. Device
artifacts include host properties, features and display metrics. CI generates
Kover reports on main pushes; they cover local JVM tests only.

### CI AAOS environment

The [workflow](../.github/workflows/android-ci.yml) defines the official Google
APIs image, API extension, ABI and emulator resources; [cstd.ini](../.github/avd/cstd.ini)
defines display and heap settings. The required
[host check](../scripts/check-aaos-environment.sh) verifies automotive support,
API, ABI, resolution and density before device tests.

CI uses a fresh AVD with a disposable data partition, headless software rendering
and disabled animations. Supplied local CSTD images and AVDs are separate inputs,
not used by CI. Matching metadata does not establish identical image binaries or
custom CSTD services; the SDK resolves the available image revision.

For local reproduction, install the workflow's image with `sdkmanager`, use
emulator 35.1.9 or newer, and configure the AVD from the workflow and `cstd.ini`.
Use the supplied AVD for CSTD-specific acceptance. With only the intended emulator
connected, run `bash scripts/check-aaos-environment.sh` (Git Bash on Windows),
then the connected-test command in CONTRIBUTING. Passing the host check identifies
the environment; connected-test results establish that the Room and app journeys ran.

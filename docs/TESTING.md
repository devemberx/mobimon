# Testing strategy

Design baseline: 2026-09-10. This document accompanies
[the target architecture](ARCHITECTURE.md). Test locations and dependencies
marked planned must be implemented before their commands are usable.

## Current setup

| Area | Observed repository state |
| --- | --- |
| Modules and UI | Eight implemented modules; two native home surfaces, drawer, settings and Q01 |
| Local tests | JUnit 4.13.2 and coroutines-test 1.8.1; domain, ViewModel, runtime, Compose and storage behavior cases |
| Coverage | Kover 0.8.3; informational local-JVM reports, no percentage gate |
| CI | Parallel local and API 34 device jobs; both must succeed for `Android checks`; aggregated app Kover on main pushes |
| DI, Room and platform adapters | Hilt 2.52, Room 2.6.1, KSP 2.0.21-1.0.28, DataStore 1.1.1; debug demo and release unavailable vehicle providers |
| Compose behavior test harness, Robolectric, instrumentation runner | Compose Testing 1.6.8 via BOM, Robolectric 4.13/API 34; Hilt 2.52 app journey harness and Room device tests |
| Screenshot or system-UI test harness | Not configured |

Keep JUnit 4, coroutines-test, and Kover. Do not add a second coverage system or a
mocking framework by default. Preserve the pinned JDK/SDK/Kotlin/Compose stack
in [CONTRIBUTING](../.github/CONTRIBUTING.md#development-environment). Dependency
additions need compatible versions in the catalog and reviewed lockfile updates.

## Implemented test locations

- `core/core-domain/src/test/kotlin`: reward boundaries and evidence rejection.
- `core/core-vss/src/test/kotlin`: unavailable real vehicle contract.
- `core/core-database/src/test/java`: Robolectric native-SQLite constraints,
  transactions, concurrent rewards, observation consistency and file reopening;
  DataStore persistence/error/cancellation behavior.
- `core/core-database/src/androidTest/java`: device SQLite transaction/reopen
  counterparts. Compiling these tests does not establish that they ran on a device.
- Feature `src/test/java`: plain constructor-injected ViewModels and Compose
  state/callback/availability contracts on Robolectric.
- `app/src/test/java`: runtime, shell navigation, Compose drawer integration,
  and the Q01 journey through feature ViewModels and a real local Room database.
  `app/src/testDebug/java` checks the debug-only simulated provider lifecycle.
- `app/src/journeyTest/java`: the same MainActivity/Hilt/Room UI journeys run in
  Debug local tests and app instrumentation. This directory is explicitly added
  to those two test source sets in `app/build.gradle.kts`, never to production.
  `app/src/androidTest/java` contains the Hilt instrumentation runner;
  `app/src/testDebug/resources/robolectric.properties` selects the local host.

Helpers are currently module-local. A production APK does not depend on test
Fakes. Debug/demo classes live in `app/src/debug`; Release has its own unavailable
provider binding. Neither automatic tests nor the demo provider prove real AAOS
or AI integration. Full conversation, memory and Q03 cases below are still target
coverage for subsequent features.

## Source and test correspondence

Put a test in the same module and package as its subject. Use the subject name
plus `Test` for focused tests; name integration tests after the behavior they
prove. Current examples:

```text
core/core-domain/
  src/main/kotlin/com/monsters/mobimon/core/domain/RewardCalculator.kt
  src/test/kotlin/com/monsters/mobimon/core/domain/RewardCalculatorTest.kt

feature/feature-quest/
  src/main/java/com/monsters/mobimon/feature/quest/QuestViewModel.kt
  src/test/java/com/monsters/mobimon/feature/quest/QuestViewModelTest.kt
  src/main/java/com/monsters/mobimon/feature/quest/QuestScreen.kt
  src/test/java/com/monsters/mobimon/feature/quest/QuestScreenTest.kt

core/core-database/
  src/main/java/com/monsters/mobimon/core/database/RoomCompanionRepository.kt
  src/androidTest/java/com/monsters/mobimon/core/database/RoomCompanionRepositoryAndroidTest.kt

app/
  src/test/java/com/monsters/mobimon/ui/MobiMonContentTest.kt
```

`QuestScreenTest` is a local Compose test with Robolectric, not a
plain JVM test that can run without a configured Android UI host. Instrumented
Compose/system integration tests go in `androidTest`. Neither source set is
automatically shared with the other or with another module.

Test business behavior, async transitions, persistence guarantees, and meaningful
UI contracts. Do not mechanically pair every data class, Activity, composable,
getter, or DI registration with a unit-test file. A test should fail when a
requirement is violated, rather than merely repeat the implementation formula.

Path correspondence helps discover tests; it is not a file-count or method-count
gate. Every changed critical behavior must have an identifiable test. Update the
current requirement map below when changing that behavior. An integration test
may cover multiple subjects, and a subject may have both local and device tests.

## Current requirement map

These links identify implemented tests, not evidence of a particular run passing.
Use the local/device CI reports to establish execution for the current revision.
IDs below are local documentation identifiers, not Polarion work-item bindings.

| ID / requirement | Implemented test and representative method | Layer |
| --- | --- | --- |
| GROW-01: fixed XP growth boundaries | [RewardCalculatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/RewardCalculatorTest.kt), `stage changes at the fixed XP boundaries` | JVM |
| REWARD-01: concurrent completion awards once | [RoomCompanionRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/RoomCompanionRepositoryTest.kt), `duplicate concurrent completion grants exactly one reward`; [device counterpart](../core/core-database/src/androidTest/java/com/monsters/mobimon/core/database/RoomCompanionRepositoryAndroidTest.kt), `uniqueSqlConstraintsAndConcurrentRewardAllowOneCompletion` | Local SQLite + device |
| REWARD-02: storage failure rolls back all reward writes | Same Room suites: `sqlite failure during xp update rolls back completion and run status` / `sqliteAbortRollsBackEveryRewardWrite` | Local SQLite + device |
| STORE-01: committed profile and reward survive DB reopening | Same Room suites: `file database reopen restores appearance xp and completion` / `fileDatabaseReopenRestoresCommittedProfileAndReward` | File-backed DB; not process restart |
| SIGNAL-01: old, invalid, wrong-source or wrong-epoch evidence is rejected | [QuestEvaluatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/QuestEvaluatorTest.kt); [QuestViewModelTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestViewModelTest.kt), `snapshotFreshnessExpiresWithoutAnotherVehicleEmission` | JVM / ViewModel |
| JOURNEY-01: UI acknowledgment commits one 80 XP reward and Activity recreation retains it | [Q01AppJourneyTest](../app/src/journeyTest/java/com/monsters/mobimon/Q01AppJourneyTest.kt), `q01AwardsOnceThroughUiAndSurvivesActivityRecreation` | Hilt + MainActivity + Room, local and device |
| JOURNEY-02: unavailable/unknown/moving vehicle state blocks starting; parked recovery enables it | Same app suite: `unavailableVehicleBlocksStartUntilParked`, `unknownDrivingStateBlocksStartUntilParked`, `movingVehicleBlocksStartUntilParked` | Hilt + MainActivity + Room, local and device |
| SETTINGS-01: failed saves preserve committed state; cancellation propagates | [PetViewModelTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/PetViewModelTest.kt), `failedAppearanceSavePreservesCommittedSelectionAndExposesFailure`; [DataStoreSettingsRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/DataStoreSettingsRepositoryTest.kt) | Local |
| SHELL-01: detail back returns to menu; close dismisses drawer | [MobiMonContentTest](../app/src/test/java/com/monsters/mobimon/ui/MobiMonContentTest.kt), `detailBackReturnsToMenuAndCloseDismissesTheWholeDrawer` | Compose + Robolectric |
| RUNTIME-01: repeated foreground notifications do not duplicate vehicle connection | [CompanionRuntimeTest](../app/src/test/java/com/monsters/mobimon/runtime/CompanionRuntimeTest.kt), `repeatedForegroundNotificationsDoNotDuplicateConnection` | JVM; not Application lifecycle integration |
| SOURCE-01: real progression rejects simulated evidence; real adapter reports unavailable | Room local suite, `real repository rejects simulated start and completion evidence`; [UnavailableVehicleRepositoryTest](../core/core-vss/src/test/kotlin/com/monsters/mobimon/core/vss/UnavailableVehicleRepositoryTest.kt) | JVM / local SQLite |
| BRAND-01: Debug application ID and launcher label match MobiMon | [BrandingTest](../app/src/testDebug/java/com/monsters/mobimon/BrandingTest.kt) | Debug + Robolectric |

The app journeys use `HiltTestApplication`, not `MobiMonApplication`. They verify
the production MainActivity, ViewModels, AppModule repository bindings and Room
transactions. `JourneyTestModule` replaces only PlatformModule/VehicleProviderModule
with a fixed clock, controllable simulated vehicle, in-memory Room and disposable
DataStore. Each test gets new Hilt singletons; Activity closes before DataStore
jobs are cancelled/joined, Room closes and the temporary preferences directory is
removed. The fixed clock keeps these UI journeys independent of execution speed;
freshness/time-boundary behavior remains covered by the dedicated rule/ViewModel tests.

Activity recreation is not process death or file persistence. App startup/runtime
integration, a process-restart journey, Release artifact checks and real AAOS
acceptance remain separate work. Screenshot tests remain deferred until visuals
stabilize; neither a screenshot suite nor `core-testing` is required to add a feature.

## Reusable setup and test doubles

Use explicit Kotlin builders, fresh Fake instances, and JUnit `@Before`,
`@After`, and `@Rule`. There is no pytest-style directory-discovered fixture
injection. The [referenced Python conftest](https://github.com/devemberx/mcp-server-polarion/blob/main/tests/mcp_server_polarion/tools/conftest.py)
provides a useful model for injecting external clients and clearing shared
state; preserve those purposes through explicit setup and cleanup.

Start module-local helpers in that module's `test` or `androidTest` package.
When multiple source sets/modules need the same JVM-only helper, extract it to
the planned `:core:core-testing` Kotlin/JVM library:

```text
core/core-testing/src/main/kotlin/com/monsters/mobimon/core/testing/
  builders/       # vehicleSnapshot(), questRun(), petProfile()
  fake/           # FakeRewardRepository, FakeVehicleRepository, FakeAiGateway
                  # FakeMemoryRepository, FakeClock, deterministic IDs
  rule/           # MainDispatcherRule for local tests
```

Consumers declare `testImplementation` or `androidTestImplementation` on this
module. Its helpers use `src/main` to be publishable to test consumers; the
module must never be a production `implementation` or `api` dependency. Keep it
free of Android, Room, feature modules, and actual credentials. Android database
builders and runner helpers remain in their instrumented test source set until
sharing them is actually necessary. Do not wire another module's `src/test`
directory into a production source set.

Test Fakes and demo providers serve different purposes. Interactive simulated
vehicle/AI providers belong in the appropriate debug/demo source set with
explicit simulated labels. A production or demo APK must not depend on
`core-testing`; simulated evidence/progression must not become real results.

Fakes must make scenarios controllable: hold a request pending, release success,
return a storage/network failure, emit missing/stale vehicle data, and complete
an obsolete request. Keep the contract's duplicate handling and error semantics
consistent with the real implementation. Use outcome assertions and recorded
request arguments; require exact call counts only when they establish behavior
such as duplicate subscription prevention.

Create new repositories, scopes, clocks, and sessions per test. Close databases,
cancel collectors, unregister listeners, and restore overridden dispatchers in
cleanup. Do not let singleton state or caches leak between tests. Prefer a Fake
over a mock where an interface can represent the dependency. Use a mocking
library only for a concrete need that a small Fake cannot cover. This follows
[Android's test-double guidance](https://developer.android.com/training/testing/fundamentals/test-doubles).

## Deterministic coroutine and time tests

- Construct a ViewModel directly with `FakeRewardRepository` or its use case
  wired to that Fake. Local ViewModel tests do not require an emulator or Hilt.
- Use `runTest`. Share one `TestCoroutineScheduler` across test dispatchers,
  injected scopes, and time-dependent Fakes; avoid hidden production IO scopes.
- For JVM tests using `viewModelScope`, install a test Main dispatcher with a
  `MainDispatcherRule` before constructing the subject and reset Main afterward.
  Do not replace Android's real Main dispatcher in instrumented UI tests.
- Use an explicit pending gate such as a controllable deferred result. Start the
  request, use `runCurrent()` to observe loading, then complete/fail it and
  inspect the new state. Advancing straight to idle can skip the loading phase.
- Test timeouts and freshness with controlled clocks, `advanceTimeBy`, and
  `runCurrent`, not `Thread.sleep` or real network delays. Coroutine virtual time
  does not automatically advance a separately implemented wall/monotonic clock.
- When `stateIn(WhileSubscribed(...))` is under test, establish a collector before
  emitting data. Use the test background scope or explicit cancellation for
  long-lived flows. Do not expect StateFlow to retain every intermediate value.
- Propagate coroutine cancellation; do not turn `CancellationException` into an
  ordinary retryable error. Also test a provider reply arriving after local
  cancellation, because request identity must guard acceptance independently.

See [Android coroutine testing](https://developer.android.com/kotlin/coroutines/test)
for scheduler and Main-dispatcher setup. Reserve Hilt's test
runner/rules for graph and integration tests; ordinary constructor-injected
unit tests remain unchanged. Follow the [Hilt testing guide](https://developer.android.com/training/dependency-injection/hilt-testing)
when configuring the test Application, runner, and rule ordering.

## Test layers and acceptance matrix

| Layer / planned location | Prove | Do not substitute |
| --- | --- | --- |
| Pure Kotlin, `core-domain/src/test` | Growth boundaries, evidence evaluation, condition precedence, invalid inputs | Device or DB setup for arithmetic/rules |
| ViewModel/use-case tests, feature `src/test` | Loading/success/failure, cancellation, stale responses, observed state | A live AI/vehicle connection |
| Compose + Robolectric, feature `src/test` | State rendering, callbacks, focus, enabled actions, drawer/back behavior | Screenshots for assertions available through semantics |
| Room integration, `core-database/src/test` and `src/androidTest` | Native SQLite constraints, rollback, concurrent completion, persistence; device counterparts | Fake repositories as proof of SQL behavior, local runs as proof of device execution |
| App integration, `app/src/test` and shared `src/journeyTest` | Drawer behavior and Q01 UI journey through Hilt/MainActivity/Room; shared journeys run locally and on device | Process restart, Application runtime lifecycle, or real vehicle/AI support |
| Supplied AAOS environment | Overlay/permission behavior, parked restrictions, real signals, real AI, restart/submission artifact | A standard emulator or passing JVM tests |

The roadmap below includes future features and stronger acceptance targets. It is
not a list of currently passing tests; the current requirement map above identifies
implemented coverage. Prioritize each case with the feature introducing it:

| Case | Expected result | Owner / strongest test |
| --- | --- | --- |
| Growth at 0, 79, 80, 239, 240 XP | Correct stage on both sides of each boundary | C / `RewardCalculatorTest` |
| Same quest completed five times, including concurrent requests | One completion and one 80 XP award per profile/type | C / Room integration |
| Cancel races with completion; a new run reuses the same type | Revalidate active run/revision; no second type reward | C / Room integration |
| Failure between completion insertion and XP update | No partial completion, run change, or XP increment | C / real transaction with controlled storage failure |
| Reopen persistent DB and relaunch app | XP, completions and approved memories survive | C+D / file-backed DB reopen plus device journey |
| Old, out-of-order, pre-start, missing, or simulated signal | No false real completion; unavailable stays explicit | B+C / rules and tracker tests |
| Disconnect/process gap during charging | Unverified care progress; no inferred completion | B+C / tracker and restart tests |
| Charging stops or cable disconnects without completion evidence | Q03 remains uncompleted | B+C / evaluator tests |
| Vehicle warning plus low energy, or worsening condition | Correct condition priority; saved growth unchanged | B+C / resolver and composition tests |
| Open/close/reenter screens repeatedly | One active vehicle connection and tracker | A+B / runtime integration |
| AI pending, timeout, retry, close, late reply | Correct phase and input preservation; obsolete reply ignored | D / session and ViewModel tests |
| Memory save fails, or user rejects proposal | No stored memory and no claim that it was remembered | D / use-case and UI tests |
| Memory changed/deleted during a request | New revision and cleared context; old reply discarded; future payload excludes deleted memory | D / session and Room integration |
| Memory deletion after a reward | Completion remains; no duplicate XP; another profile unaffected | C+D / Room integration |
| Detail back, close, two-home switch, reduced motion | UI design behavior and same saved selection/progress | A / Compose and app integration |
| Overlay service outlives/restarts independently of Activity | UI reflects actual service state; explicit permission failure | A / supplied AAOS environment |

Use a fresh in-memory Room database for isolation/transaction tests. For reopen
tests use a disposable file-backed database, close it, recreate it, and read
through new repositories; an in-memory DB cannot establish restart persistence.
Test migrations against exported schemas when the schema changes. Use no
destructive-migration fallback for user progress. See
[Room's testing guidance](https://developer.android.com/training/data-storage/room/testing-db).

## UI harness and visual checks

Compose Testing APIs and Robolectric are configured with the necessary test
host/resources. Test plain
`*Screen(state, callbacks)` composables without a whole application graph.
Find controls by text, content description, role, or state; use `testTag` only
where stable semantics are insufficient. Exercise text input and focus/back
behavior as well as touch. Use state-restoration tests only for state intended
to be restored; conversation text intentionally does not survive process death.
See [Compose testing](https://developer.android.com/develop/ui/compose/testing).

Introduce a small screenshot suite when the pet and drawer visuals stabilize.
Pin UI state, locale, font scale, density, assets, and animation time. Cover the
supported AAOS viewport first, then compact/medium/expanded layouts and larger
text. Screenshot tooling and baselines are not currently configured; verify
compatibility with the pinned stack before choosing/installing a tool. Record
the actual baseline path and task names here in the same change. Do not update
reference images automatically just to make a failed comparison pass.

Use device tests for system overlays and permission flows. Add UI Automator
only when automating interaction outside the app is needed. Keep actual
AI/vehicle acceptance separate from hermetic CI: record device/image, signal
source, scenario, and observed outcome without committing credentials or raw
private logs. A file-backed reopen test, Activity recreation, and actual process
restart verify different guarantees and must not be reported interchangeably.

## Commands and CI

Currently configured commands, from the repository root:

```bash
./gradlew ktlintCheck lintDebug testDebugUnitTest :core:core-domain:test :core:core-vss:test :app:assembleDebug :app:assembleDebugAndroidTest :core:core-database:assembleDebugAndroidTest
./gradlew :app:koverHtmlReportDebug :app:koverXmlReportDebug
```

Behavior cases exist in the locations above. A successful task with `NO-SOURCE`
is not evidence that behavior passed. Kover reports measure local JVM tests only and
have no minimum-percentage gate. The contributor guide remains authoritative
for required checks and lockfile updates.

The following focused commands are available for implemented modules; `core-testing`
is still planned. Connected tests require a compatible connected device:

```bash
# Pure JVM rules and the unavailable vehicle adapter.
./gradlew :core:core-domain:test :core:core-vss:test

# Local feature tests, including Robolectric UI tests.
./gradlew :feature:feature-quest:testDebugUnitTest

# Requires a compatible connected device or emulator and configured runners.
./gradlew :core:core-database:connectedDebugAndroidTest :app:connectedDebugAndroidTest

# The same Q01 app journeys on the local Robolectric host.
./gradlew :app:testDebugUnitTest --tests com.monsters.mobimon.Q01AppJourneyTest
```

When splitting modules, update CI to run local tests and Lint in every applicable
module, including pure JVM tests separately. Merely depending on a library does
not make `:app:testDebugUnitTest` execute that library's tests. Retain the required
`Android checks` status, which now requires both parallel jobs to succeed. The
device job runs Room and app journeys on an API 34 Google APIs x86_64 emulator;
this is not an AAOS environment. Local results are saved as `local-reports-*`,
device reports as `device-reports-*`, including on failure. Kover still measures
only local tests. Extend Kover configuration/reports to
new modules when they acquire meaningful tests; do not assume app coverage
automatically includes them.

Each behavior PR includes the relevant owner-written tests and a small
integration check when crossing a boundary. Report exactly which layers ran,
their outcomes, and which were not run with reasons. Documentation-only changes
need content/link checks and `git diff --check`, not an Android build.

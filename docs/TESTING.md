# Testing strategy

This document owns test strategy, coverage mapping and verification limits.
Use [CONTRIBUTING.md](../.github/CONTRIBUTING.md#verification) for setup and required
checks, [DESIGN.md](DESIGN.md) for UX, and [ARCHITECTURE.md](ARCHITECTURE.md) for
technical contracts. Record test executions in the implementation issue or PR.

## Current setup and test locations

The project uses JUnit 4, coroutines-test, Robolectric, Compose Testing, Hilt
integration tests and Room device tests. Kover reports local JVM coverage with
no percentage gate. Copilot UI tests generate native Robolectric review images;
golden-image comparison and system-UI automation are not configured. Shared
component readiness and full-screen migration are separate acceptance layers.
Owners complete their feature-specific screen verification after consuming the
shared library; missing future screens do not invalidate a tested primitive.

| Location | Scope and host |
| --- | --- |
| `core/core-domain/src/test/kotlin` | Legacy reward rules, evidence and per-signal freshness; plain JVM |
| `core/core-navigation/src/test/java` | Complete, unique route registration; JVM |
| `core/core-presentation/src/test/java` | Wallet failure/loading and vehicle display freshness; JVM |
| `core/core-ui/src/test/java` | Shared component actions and touch targets; Robolectric |
| `core/core-vss/src/test/kotlin` | Unavailable real vehicle contract; plain JVM |
| `core/core-database/src/test/java` | Native SQLite constraints, transactions, concurrency, file reopening and DataStore; Robolectric |
| `core/core-database/src/androidTest/java` | SQLite transaction and file-reopening counterparts; device |
| Feature `src/test/java` | Constructor-injected ViewModels and Compose state/callback contracts; JVM or Robolectric |
| `app/src/test/java`, `src/testDebug/java` | Runtime, shell, Q01 repository integration, branding and demo provider behavior |
| `app/src/journeyTest/java` | MainActivity quest/connection journeys with Hilt and Room, plus isolated CopilotPreviewActivity journeys; Robolectric and device |

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

## Final Figma visual acceptance

After changing a shared component or completing a feature screen, compare the
affected v4 states referenced by
[DESIGN.md](DESIGN.md) with its full-resolution Figma export. Match the app content
area, display scale, font scale and displayed data before comparing side by side
or with an overlay. Check original artwork, font family and weight, text size and
baselines, wrapping, colors, panel proportions, spacing, alignment, corners, icons
and button bounds. Fix discrepancies and repeat the comparison after the final
code change; inspect compact and enlarged-text layouts separately for usability.
Exclude the SVG's drawn system bars and any Debug review controls from the
reference comparison. Inspect the actual device app window separately: its
compatibility density and available height can select a different layout from a
native Robolectric reference render. A rehearsal toolbar must not hide controls
or inadvertently force the compact layout at the reference app window.

Record the references, rendered images and remaining differences in the
implementation issue or PR. Distinguish font antialiasing differences from layout
or styling errors. Passing builds, behavior tests or generating review images
does not establish visual parity. Do not report an exact match while any screen
lacks a full-resolution reference or still has an unresolved visual difference.

## Current requirement map

Map changed critical behavior to implemented suites. The point economy and
full-screen shell now have tests; the production quest and paid-item catalogs
remain undefined. Future cases follow [DESIGN.md](DESIGN.md) and the
[planned contracts](ARCHITECTURE.md#planned-features). Suite links identify
coverage, not a passing result at a particular revision.

| Requirement | Test suite | Scope |
| --- | --- | --- |
| Declared feature project dependencies stay within allowed core modules; selected domain platform imports are rejected | `verifyModuleBoundaries` in [root build](../build.gradle.kts) | Configured project declarations and main Kotlin imports; [audit limits](ARCHITECTURE.md#state-and-lifecycle) apply |
| Missing/duplicate routes cannot silently render an arbitrary feature | [FeatureRegistryTest](../core/core-navigation/src/test/java/com/monsters/mobimon/core/navigation/FeatureRegistryTest.kt) | JVM |
| AI observes committed companion context independently; failed observation retains it and supports retry without duplicate observers; cancellation propagates | [AiCompanionViewModelTest](../feature/feature-auth/src/test/java/com/monsters/mobimon/feature/auth/AiCompanionViewModelTest.kt) | ViewModel |
| AI route exposes initial and later observation failures, retains the displayed companion across revisits, and retries to current equipment without duplicate observers or authorizing unknown parking | [AiFeatureTest](../feature/feature-auth/src/test/java/com/monsters/mobimon/feature/auth/AiFeatureTest.kt) | Production feature entry, real ViewModels and fake repositories on Robolectric; includes enlarged-text and keyboard retry |
| Shared actions and selection controls retain 76dp targets; disabled callbacks cannot dispatch; selection stays caller-owned; account content is vertically centered; tabs and rows reflow at enlarged text | [MobiMonComponentsTest](../core/core-ui/src/test/java/com/monsters/mobimon/core/ui/MobiMonComponentsTest.kt), [MobiMonV4ComponentsTest](../core/core-ui/src/test/java/com/monsters/mobimon/core/ui/MobiMonV4ComponentsTest.kt) | Compose semantics, layout bounds and Robolectric; not Figma pixel parity |
| Quest-owned acknowledgment blocks moving/stale/unknown state and forwards the displayed snapshot ID | [QuestVehicleCardTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestVehicleCardTest.kt), shared Q01 journey | Compose and app integration |
| Historical XP compatibility boundaries; not a product progression feature | [RewardCalculatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/RewardCalculatorTest.kt) | JVM |
| Concurrent completion awards once; failures roll back all reward writes | [RoomCompanionRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/RoomCompanionRepositoryTest.kt), [device counterpart](../core/core-database/src/androidTest/java/com/monsters/mobimon/core/database/RoomCompanionRepositoryAndroidTest.kt) | Local SQLite and device |
| Committed appearance, XP and completion survive file reopening | Same Room suites | File persistence |
| Old, invalid, wrong-source or wrong-epoch evidence is rejected | [QuestEvaluatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/QuestEvaluatorTest.kt), [QuestViewModelTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestViewModelTest.kt) | JVM and ViewModel |
| Superseded parking or AAOS app-use restriction rejects queued reward writes | [Q01JourneyTest](../app/src/test/java/com/monsters/mobimon/Q01JourneyTest.kt) | Local Room transaction |
| Parking and battery age are evaluated independently | [VehicleFreshnessPolicyTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/VehicleFreshnessPolicyTest.kt), [VehicleInfoScreenTest](../feature/feature-vehicle-info/src/test/java/com/monsters/mobimon/feature/vehicle/VehicleInfoScreenTest.kt) | Domain and Compose |
| Point migration preserves XP evidence without minting points | [PointEconomyMigrationTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/PointEconomyMigrationTest.kt) | Hand-built, file-backed v1 fixture to current v3 through both migrations |
| Purchases charge once, equipment is separate, and one-time/daily credits have unique occurrences | [PointEconomyRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/PointEconomyRepositoryTest.kt) | Local SQLite |
| A loading or failed point balance is distinct from saved zero | [PointBalanceViewModelTest](../core/core-presentation/src/test/java/com/monsters/mobimon/core/presentation/PointBalanceViewModelTest.kt) | ViewModel |
| Home preserves point loading/failure, independent vehicle freshness, warning severity and history, and simulated labels when vehicle data is lost or recovered; conversation stays disabled without a connection destination | [PetHomeScreenTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/PetHomeScreenTest.kt) | Compose and Robolectric |
| Fresh readings arriving between timer ticks stay valid, genuine future timestamps remain unavailable, and readings still expire | [VehicleStateViewModelTest](../core/core-presentation/src/test/java/com/monsters/mobimon/core/presentation/VehicleStateViewModelTest.kt) | Coroutine virtual time and controllable clock |
| Home forwards navigation and retry callbacks, distinguishes profile loading/failure and loading/empty/failed inventory, keeps primary targets reachable with enlarged text and keyboard focus, and keeps controls reachable at head-unit and tall windows, unclipped parking text at 1.2x scale, and an accessible companion without a visible name caption | Same Home suite | Compose, semantics, text layout and layout/touch target bounds |
| Failed inventory observation can be retried without losing a saved selection | [CosmeticInventoryViewModelTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/CosmeticInventoryViewModelTest.kt), [CustomizationScreenTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/CustomizationScreenTest.kt) | ViewModel and Compose |
| Previewing Luna does not apply her until the user confirms | [CustomizationScreenTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/CustomizationScreenTest.kt) | Compose |
| Debug legacy Q01 acknowledgment retains its historical 80 XP transaction once after Activity recreation; this is not a v4 product reward specification | [Q01AppJourneyTest](../app/src/journeyTest/java/com/monsters/mobimon/Q01AppJourneyTest.kt) | Hilt, MainActivity and Room; local and device |
| Quest screen offers Q01 without advertising legacy Q02/Q03 | [QuestScreenTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestScreenTest.kt) | Compose and Robolectric |
| Settings retains committed preferences during independent saves/failures, blocks unknown parking and unavailable services, supports retry and keyboard navigation, and keeps shared rows scrollable and 76dp actions reachable at the head-unit app window | [PetPreferencesScreenTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/PetPreferencesScreenTest.kt) | Compose and Robolectric; responsive controls and keyboard input |
| Unavailable, unknown or moving state blocks starting; parked recovery enables it | Same app journey suite | Local and device |
| Failed settings/appearance saves preserve committed state; cancellation propagates | [PetViewModelTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/PetViewModelTest.kt), [DataStoreSettingsRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/DataStoreSettingsRepositoryTest.kt) | Local |
| Menu stays within its window and preserves 76dp targets, takes initial focus, and closes after navigation; destination back restores Home and AAOS restrictions preserve the route | [MobiMonContentTest](../app/src/test/java/com/monsters/mobimon/ui/MobiMonContentTest.kt), [ShellStateTest](../app/src/test/java/com/monsters/mobimon/ui/ShellStateTest.kt) | Compose and JVM |
| Settings and Home open the connection introduction, restore its origin and unavailable feedback across Activity recreation, and disable connection after parking becomes unknown | Shell suite, [CopilotConnectionJourneyTest](../app/src/journeyTest/java/com/monsters/mobimon/CopilotConnectionJourneyTest.kt) | JVM and shared MainActivity journeys on Robolectric/device |
| Copilot states expose approval/help/recheck/cancel and disconnect actions, hide expired codes, disable repeated pending actions, distinguish access failures from account approval, and keep large-text controls keyboard-operable | [CopilotConnectionScreenTest](../feature/feature-auth/src/test/java/com/monsters/mobimon/feature/auth/CopilotConnectionScreenTest.kt) | Compose and Robolectric |
| The eight Copilot reference states retain panel proportions, expose working action callbacks and render 2560×1268 review images in `feature/feature-auth/build/reports/copilot-ui` | Same connection suite | Native Robolectric graphics; review artifacts, not golden baselines |
| Panel transitions suppress outgoing actions, preserve countdown content identity and remove expired content immediately; reduced motion bypasses fades | [CopilotPanelTransitionTest](../feature/feature-auth/src/test/java/com/monsters/mobimon/feature/auth/CopilotPanelTransitionTest.kt) | Compose with controlled animation clock |
| The isolated Debug rehearsal connects approval help, recreation, success, destination placeholders, disconnect, expiry, reconnect and access review | [CopilotPreviewJourneyTest](../app/src/journeyTest/java/com/monsters/mobimon/preview/CopilotPreviewJourneyTest.kt) | Shared preview Activity journeys on Robolectric/device |
| Repeated foreground notifications do not duplicate a vehicle connection | [CompanionRuntimeTest](../app/src/test/java/com/monsters/mobimon/runtime/CompanionRuntimeTest.kt) | Runtime unit test |
| Real reward transactions reject simulated evidence; real adapter reports unavailable | Room local suite, [UnavailableVehicleRepositoryTest](../core/core-vss/src/test/kotlin/com/monsters/mobimon/core/vss/UnavailableVehicleRepositoryTest.kt) | JVM and local SQLite |
| Debug application ID and launcher label match MobiMon | [BrandingTest](../app/src/testDebug/java/com/monsters/mobimon/BrandingTest.kt) | Debug and Robolectric |

## Integration boundaries

`PointEconomyMigrationTest` builds its v1 fixture directly with SQL; it does not
load exported schemas with `MigrationTestHelper`. It exercises `MIGRATION_1_2`
and `MIGRATION_2_3` together, not a separate populated v2 fixture or an earlier
development v3 database. Schema exports and successful fresh creation do not
prove those historical upgrade paths. Add fixtures for supported versions when
implementing the required versioned migration; preserve historical records.

The MainActivity journeys use `HiltTestApplication`. Their
[JourneyTestModule](../app/src/journeyTest/java/com/monsters/mobimon/testing/JourneyTestModule.kt)
replaces platform, vehicle and AAOS use-state providers with a fixed clock, controllable
simulated vehicle, an allowed app-use state, in-memory Room and isolated DataStore. MainActivity, feature
ViewModels, repository bindings and reward transactions remain real. They now
exercise Hilt feature registrations, the vehicle contribution slot and saved
route state end to end; shell-only tests use minimal entries to isolate navigation.

`CopilotPreviewJourneyTest` launches the plain Debug preview Activity without
Hilt injection or Room interaction. It verifies presentation navigation and
Activity recreation with sample data; it does not exercise the production
connection host, vehicle authorization or an authentication provider.

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
Copilot connection UI tests use explicit display states and Debug-only examples.
They do not execute GitHub OAuth, QR approval, provider polling, token storage,
Copilot readiness checks or real disconnect. The production route shows only the
introduction and unavailable feedback until those integrations are implemented.
The separate Debug launcher **Copilot UI 체험** is a manual UI rehearsal, with
explicitly simulated accounts, a fixed timer and no network or credential storage.
Its scenario controls and destination placeholders are outside the Figma design.
Release manifest/resource inspection is evidence of packaging boundaries;
it does not establish live authentication or Release runtime behavior.

## Focused commands and reports

Run from the repository root; on Windows use `gradlew.bat`. The full required
check sequence and connected-test command are in
[CONTRIBUTING.md](../.github/CONTRIBUTING.md#verification).

```bash
# Shared Q01 journeys on Robolectric.
./gradlew :app:testDebugUnitTest --tests com.monsters.mobimon.Q01AppJourneyTest

# Isolated Copilot rehearsal journeys on Robolectric.
./gradlew :app:testDebugUnitTest --tests com.monsters.mobimon.preview.CopilotPreviewJourneyTest

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

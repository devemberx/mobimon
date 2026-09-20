# Testing strategy

This document owns test strategy, coverage mapping and verification limits.
Use [CONTRIBUTING.md](../.github/CONTRIBUTING.md#verification) for setup and required
checks, [DESIGN.md](DESIGN.md) for UX, and [ARCHITECTURE.md](ARCHITECTURE.md) for
technical contracts. Record test executions in the implementation issue or PR.

## Current setup and test locations

The project uses JUnit 4, coroutines-test, Robolectric, Compose Testing, Hilt and
Room device tests. Kover is informational, with no percentage gate. Review-image
generation exists; golden comparisons and system-UI automation are not configured.

| Location | Scope and host |
| --- | --- |
| `core/core-domain/src/test/kotlin` | Driving/legacy reward rules, evidence and freshness; plain JVM |
| `core/core-navigation/src/test/java` | Complete, unique route registration; JVM |
| `core/core-presentation/src/test/java` | Wallet failure/loading and vehicle display freshness; JVM |
| `core/core-ui/src/test/java` | Shared component actions and touch targets; Robolectric |
| `core/core-vss/src/test/kotlin` | Unavailable real vehicle contract; plain JVM |
| `core/core-database/src/test/java`, `src/testDebug/java` | Native SQLite constraints, transactions, concurrency, file reopening, DataStore and Debug-only point transactions; Robolectric |
| `core/core-database/src/androidTest/java` | SQLite transaction and file-reopening counterparts; device |
| `core/core-database/src/migrationTest/java` | Shared v3 upgrade and persistence contracts; local and device wrappers in the database module |
| Feature `src/test/java` | Constructor-injected ViewModels and Compose state/callback contracts; JVM or Robolectric |
| `app/src/test/java`, `src/testDebug/java` | Runtime, shell, Q01 repository integration, branding and demo provider behavior |
| `app/src/journeyTest/java` | MainActivity connection journeys with Hilt and Room, plus isolated CopilotPreviewActivity journeys; Robolectric and device |

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

After the final UI code change, compare affected states with the full-resolution
[v5 exports](ui/README.md). Match data, window size, display/font scale and the
app content area; exclude drawn system bars and Debug review controls. Inspect
artwork, typography/baselines, wrapping, colors, proportions, spacing, corners,
icons and touch bounds. Check compact/enlarged-text layouts and the actual AAOS
app window separately; device density/insets can select another layout.

Include menu safe insets, shared navigation/parking controls, vehicle expression
and visible scale, aligned status banners/badges, and centered quest empty states.
Verify keyboard/dialog focus, pending/recovery actions and interruption during
motion. Static SVGs do not verify animations, persistence or provider integration.

Record reference paths, review images and unresolved differences in the PR.
Missing reference states remain explicit in the export index. Builds, behavioral
tests and generated images alone do not establish visual parity. Source-only SVG
renames require XML/render checks and byte-preservation checks, not an Android
visual acceptance claim.

## Current requirement map

These links describe existing coverage, not results at a particular revision.
Update the map when critical behavior changes. Current implementation gaps are
owned by [ARCHITECTURE.md](ARCHITECTURE.md#planned-features).

| Requirement | Suite | Scope |
| --- | --- | --- |
| Five background periods, hour boundaries, aliases and matching image dimensions | [CompanionBackgroundTest](../core/core-ui/src/test/java/com/monsters/mobimon/core/ui/CompanionBackgroundTest.kt), [DebugVssStateInterpretationTest](../app/src/testDebug/java/com/monsters/mobimon/debug/DebugVssStateInterpretationTest.kt) | Shared mapping/resource decoding and Debug agreement |
| Background follows local wall clock and time-zone changes without modifying vehicle evidence; supplied time overrides the clock | [VehicleStateViewModelTest](../core/core-presentation/src/test/java/com/monsters/mobimon/core/presentation/VehicleStateViewModelTest.kt) | Injected clocks, time zone and virtual ticker |
| Declared module dependencies and selected domain imports | `verifyModuleBoundaries` in [root build](../build.gradle.kts) | Project declarations, production external dependencies/JVM graphs and selected production imports; [audit limits](ARCHITECTURE.md#state-and-lifecycle) apply |
| Complete, unique destination registration | [FeatureRegistryTest](../core/core-navigation/src/test/java/com/monsters/mobimon/core/navigation/FeatureRegistryTest.kt) | JVM |
| Committed AI context, observation retry and cancellation | [AiCompanionViewModelTest](../feature/feature-auth/src/test/java/com/monsters/mobimon/feature/auth/AiCompanionViewModelTest.kt) | ViewModel |
| AI context failure/retry across route revisits and unknown parking | [AiFeatureTest](../feature/feature-auth/src/test/java/com/monsters/mobimon/feature/auth/AiFeatureTest.kt) | Production entry with fake repositories; Robolectric |
| 76dp controls, disabled callbacks, selection and enlarged-text reflow | [MobiMonComponentsTest](../core/core-ui/src/test/java/com/monsters/mobimon/core/ui/MobiMonComponentsTest.kt), [MobiMonThemeComponentsTest](../core/core-ui/src/test/java/com/monsters/mobimon/core/ui/MobiMonThemeComponentsTest.kt) | Compose semantics/bounds; no pixel parity |
| Artwork, accessory fallbacks and Mobi/Luna animation asset loading | [PetAvatarTest](../core/core-ui/src/test/java/com/monsters/mobimon/core/ui/PetAvatarTest.kt) | Native Robolectric pixel signatures; no v5 expression coverage |
| Historical XP calculation compatibility | [RewardCalculatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/RewardCalculatorTest.kt) | JVM |
| Atomic legacy completion, concurrency, rollback and file persistence | [RoomCompanionRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/RoomCompanionRepositoryTest.kt), [device counterpart](../core/core-database/src/androidTest/java/com/monsters/mobimon/core/database/RoomCompanionRepositoryAndroidTest.kt) | Local SQLite and device |
| Legacy evidence freshness, source, epoch and ownership | [QuestEvaluatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/QuestEvaluatorTest.kt), [LegacyQuestControllerTest](../app/src/test/java/com/monsters/mobimon/testing/legacy/LegacyQuestControllerTest.kt) | JVM and test-only compatibility controller |
| Transaction recheck after parking/AAOS restrictions change | [Q01JourneyTest](../app/src/test/java/com/monsters/mobimon/Q01JourneyTest.kt) | Local Room transaction |
| Independent parking/battery freshness | [VehicleFreshnessPolicyTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/VehicleFreshnessPolicyTest.kt), [VehicleInfoScreenTest](../feature/feature-vehicle-info/src/test/java/com/monsters/mobimon/feature/vehicle/VehicleInfoScreenTest.kt) | Domain and Compose |
| V1 migration preserves XP without creating point credits | [PointEconomyMigrationTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/PointEconomyMigrationTest.kt) | File-backed v1 → v4 chain |
| Original/expanded v3 upgrade preserves records and survives reopening | [LevelingMigrationContract](../core/core-database/src/migrationTest/java/com/monsters/mobimon/core/database/LevelingMigrationContract.kt), [local wrapper](../core/core-database/src/test/java/com/monsters/mobimon/core/database/LevelingMigrationTest.kt), [device wrapper](../core/core-database/src/androidTest/java/com/monsters/mobimon/core/database/LevelingMigrationAndroidTest.kt) | Shared populated schema fixtures; local/device wrappers |
| Atomic purchases/equipment, occurrence keys and persisted point claims | [PointEconomyRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/PointEconomyRepositoryTest.kt) | Local SQLite |
| Wallet loading/failure is distinct from committed zero | [PointBalanceViewModelTest](../core/core-presentation/src/test/java/com/monsters/mobimon/core/presentation/PointBalanceViewModelTest.kt) | ViewModel |
| Home wallet/parking/appearance, one animated line below the title, navigation callbacks and accessible responsive layout | [PetHomeScreenTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/PetHomeScreenTest.kt) | Compose/Robolectric |
| Home bubble entrance, reentry, stable data updates and reduced-motion interruption | [HomeSpeechBubbleTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/HomeSpeechBubbleTest.kt) | Native controlled-frame images; device navigation remains separate |
| Home background crop/tint and two-line speech-bubble proportions at reference, smaller landscape and enlarged-text compact sizes | [CompanionReviewTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/CompanionReviewTest.kt) | Native pixel samples, text layout and bubble bounds |
| Initial freshness, clock sampling, expiry and raw/display evidence pairing | [VehicleStateViewModelTest](../core/core-presentation/src/test/java/com/monsters/mobimon/core/presentation/VehicleStateViewModelTest.kt), [QuestFeatureTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestFeatureTest.kt) | Virtual time, injected clock and production quest route |
| Debug nonmoving Park authorization and serialized epoch sequences | [DemoVehicleRepositoryTest](../app/src/testDebug/java/com/monsters/mobimon/vehicle/DemoVehicleRepositoryTest.kt) | Debug provider and virtual time |
| Debug account/ledger atomicity, authorization and reopening | [DebugPointRepositoryTest](../core/core-database/src/testDebug/java/com/monsters/mobimon/core/database/DebugPointRepositoryTest.kt) | Debug native SQLite and rollback |
| Independent catalog/inventory retries and wallet recovery preserve committed selection | [CosmeticInventoryViewModelTest](../feature/feature-customization/src/test/java/com/monsters/mobimon/feature/customization/CosmeticInventoryViewModelTest.kt), [CustomizationScreenTest](../feature/feature-customization/src/test/java/com/monsters/mobimon/feature/customization/CustomizationScreenTest.kt) | ViewModel and Compose |
| Shared category mapping, compatible previews and catalog reward amounts | [CustomizationCatalogTest](../feature/feature-customization/src/test/java/com/monsters/mobimon/feature/customization/CustomizationCatalogTest.kt), [QuestCatalogTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestCatalogTest.kt) | Pure presentation rules |
| Store category semantics and uncommitted preview reference renders | [StoreReferenceScreenTest](../feature/feature-customization/src/test/java/com/monsters/mobimon/feature/customization/StoreReferenceScreenTest.kt) | Native Robolectric review images; no golden |
| Committed appearance propagation, retained values and subscription retry | [CompanionAppearanceViewModelTest](../core/core-presentation/src/test/java/com/monsters/mobimon/core/presentation/CompanionAppearanceViewModelTest.kt) | ViewModel; Room suites own persistence |
| Friend preview/apply separation, isolated artwork and per-friend equipment | [CustomizationScreenTest](../feature/feature-customization/src/test/java/com/monsters/mobimon/feature/customization/CustomizationScreenTest.kt), [PetAvatarTest](../core/core-ui/src/test/java/com/monsters/mobimon/core/ui/PetAvatarTest.kt), [PointEconomyRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/PointEconomyRepositoryTest.kt) | Compose and Room |
| Driving quest list/detail Back and hidden quest dialogs/claim callbacks | [QuestScreenTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestScreenTest.kt) | Compose and Robolectric |
| Point claim pending/duplicate/failure/cancellation, committed amount, already-awarded reconciliation and completion resets after early or delayed observation | [QuestViewModelTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestViewModelTest.kt), [QuestScreenTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestScreenTest.kt) | JVM and Compose; no real driving evidence claim |
| Settings save without profile initialization; independent pending writes and retry | [SettingsViewModelTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/SettingsViewModelTest.kt) | ViewModel |
| Appearance failure/retry, retained look, missing/partial auxiliary readings | [VehicleFeatureTest](../feature/feature-vehicle-info/src/test/java/com/monsters/mobimon/feature/vehicle/VehicleFeatureTest.kt), vehicle screen suite | Production route and Compose |
| Home, Settings and vehicle reference/compact review renders; vehicle values, badges and independent ages avoid clipping | [CompanionReviewTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/CompanionReviewTest.kt), [VehicleReviewTest](../feature/feature-vehicle-info/src/test/java/com/monsters/mobimon/feature/vehicle/VehicleReviewTest.kt) | Native images, text layout and visible bounds at enlarged text; no golden |
| Decorative animation obeys reduced motion, frame time and parameter changes | [DecorativeMotionTest](../core/core-ui/src/test/java/com/monsters/mobimon/core/ui/DecorativeMotionTest.kt), [MotionPreferencesViewModelTest](../app/src/test/java/com/monsters/mobimon/ui/MotionPreferencesViewModelTest.kt) | Native controlled-frame images and ViewModel; device lifecycle remains separate |
| Settings independent saves, restrictions, unavailable launcher/voice rows, removed Do Not Disturb, Debug visibility and accessible controls | [PetPreferencesScreenTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/PetPreferencesScreenTest.kt) | Compose/Robolectric |
| Profile initialization failure and independent preference storage keys | [PetViewModelTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/PetViewModelTest.kt), [DataStoreSettingsRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/DataStoreSettingsRepositoryTest.kt) | Local; preference pending/cancellation belongs to SettingsViewModel |
| Menu reference/compact renders, safe bounds, focus/dismissal, navigation state and outgoing/restricted input removal | [CompanionMenuReviewTest](../app/src/test/java/com/monsters/mobimon/ui/CompanionMenuReviewTest.kt), [MobiMonContentTest](../app/src/test/java/com/monsters/mobimon/ui/MobiMonContentTest.kt), [ShellStateTest](../app/src/test/java/com/monsters/mobimon/ui/ShellStateTest.kt) | JVM/Compose with controlled animation clock |
| Connection origin/recreation and unavailable or unknown-parking guards | Shell suite, [AiFeatureTest](../feature/feature-auth/src/test/java/com/monsters/mobimon/feature/auth/AiFeatureTest.kt), [CopilotConnectionJourneyTest](../app/src/journeyTest/java/com/monsters/mobimon/CopilotConnectionJourneyTest.kt) | Shell, feature and shared connection journeys |
| Approval/help/expiry/access/disconnect controls and enlarged-text keyboard use | [CopilotConnectionScreenTest](../feature/feature-auth/src/test/java/com/monsters/mobimon/feature/auth/CopilotConnectionScreenTest.kt) | Compose/Robolectric |
| Connection panel bounds and reference/compact review renders | Same connection suite | Native Robolectric images; no golden |
| Panel identity, outgoing input removal and immediate restriction/expiry handling | [CopilotPanelTransitionTest](../feature/feature-auth/src/test/java/com/monsters/mobimon/feature/auth/CopilotPanelTransitionTest.kt) | Controlled Compose animation clock |
| Isolated Debug rehearsal navigation and recreation | [CopilotPreviewJourneyTest](../app/src/journeyTest/java/com/monsters/mobimon/preview/CopilotPreviewJourneyTest.kt) | Shared Debug Activity journey; local/device |
| Single provider connection across repeated foreground notifications | [CompanionRuntimeTest](../app/src/test/java/com/monsters/mobimon/runtime/CompanionRuntimeTest.kt) | Runtime unit test |
| Real-source rejection of simulated evidence and unavailable real adapter | Room local suite, [UnavailableVehicleRepositoryTest](../core/core-vss/src/test/kotlin/com/monsters/mobimon/core/vss/UnavailableVehicleRepositoryTest.kt) | JVM and local SQLite |
| Debug identity, launch/Home background agreement and adaptive artwork clearance | [BrandingTest](../app/src/testDebug/java/com/monsters/mobimon/BrandingTest.kt) | Native Robolectric; 48/64/96px color/themed review images in `app/build/reports/branding`, no golden; native splash and launcher need device review |
| Fourteen driving conditions, weather calculation and seventeen catalog definitions | [DrivingQuestEvaluatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/DrivingQuestEvaluatorTest.kt) | Plain JVM; does not verify real signals or transactional driving evidence |

## Integration boundaries

- Migration tests use real file-backed Room upgrades/reopening. The v1 fixture
  exercises the full v1-to-v4 chain. The shared v3 contract uses the
  [original fixture](../core/core-database/src/migrationTest/assets/legacy-schema-3.json)
  from `ef2fa07` and the [expanded v3 schema](../core/core-database/schemas/com.monsters.mobimon.core.database.AppDatabase/3.json).
  They preserve populated records/identity hashes; they do not use
  `MigrationTestHelper` or a separately populated v2 fixture.
- [JourneyTestModule](../app/src/journeyTest/java/com/monsters/mobimon/testing/JourneyTestModule.kt)
  uses `HiltTestApplication`, fake platform/vehicle/app-use providers, an injected
  clock, in-memory Room and isolated DataStore. MainActivity, feature ViewModels
  and repository bindings remain real. Current shared MainActivity journeys cover
  connection, not the retired Q01 UI journey.
- `CopilotPreviewJourneyTest` uses a plain Debug Activity and sample data without
  Hilt/Room or authentication. Presentation success does not verify OAuth, QR
  approval, polling, credentials, Copilot readiness or disconnect.
- Recreation, file reopening and process restart are distinct. Existing journeys
  cover recreation and Room suites cover persistence; neither proves real
  providers, process restart, OEM launcher support or Release runtime behavior.
  APK assembly, packaging inspection and `NO-SOURCE` tasks are not device execution.
- Driving evaluator tests verify formulas and supplied data. Point repository
  tests verify parked authorization, keys and transactions; they do not establish
  trusted driving evidence, real per-drive identity, repeat eligibility or
  evaluator-to-award amount agreement. V5 hungry/sick rendering still lacks
  acceptance; decorative motion has controlled-frame coverage, not an on-device
  lifecycle claim. See the architecture gaps before reporting them as working
  features.
- AAOS restriction callbacks, Car-service reconnection and system blocking need
  the target image. The dormant launcher preference test covers its default only,
  not a launcher surface. Demo evaluation/vehicle state is intentionally transient.

Record executions, revision, skipped layers/reasons and device image/signal source
in the issue/PR, without credentials or private logs. Future test plans belong
there until implemented suites can be linked here.

## Focused commands and reports

Use [CONTRIBUTING.md](../.github/CONTRIBUTING.md#verification) for required checks.
A filtered module test task does not run dependency modules or device tests.
For local focus, select the suite with `--tests <qualified-name>` on its module's
`testDebugUnitTest`; pure Kotlin modules use `test`.

Informational coverage: `./gradlew :app:koverHtmlReportDebug :app:koverXmlReportDebug`.
[Android CI](../.github/workflows/android-ci.yml) owns report paths, retention and
upload conditions; Kover covers local JVM tests only. Device reports include host
properties, features and display metrics.

### CI AAOS environment

The [workflow](../.github/workflows/android-ci.yml) owns the official Google APIs
image, API extension, ABI and emulator resources; [cstd.ini](../.github/avd/cstd.ini)
owns display/heap settings. The required [host check](../scripts/check-aaos-environment.sh)
verifies automotive support, API, ABI, resolution and density before device tests.

CI uses a fresh disposable AVD, headless software rendering and disabled
animations. Local CSTD images are separate inputs: matching metadata does not
prove identical binaries or custom services. For local reproduction, use the
workflow's image/configuration and emulator 35.1.9 or newer. With only the intended
emulator connected, run the host check, then the canonical connected tests. A
passing host check identifies the environment; test results establish execution.

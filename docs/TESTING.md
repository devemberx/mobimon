# Testing strategy

[CONTRIBUTING.md](../.github/CONTRIBUTING.md#verification) owns required commands.

## Current setup and test locations

Use JUnit 4, coroutines-test, Robolectric/Compose Testing and Hilt/Room device tests.
Review images exist; golden comparisons and system-UI automation are not configured.

Tests live in the subject module's `src/test`; device tests use `src/androidTest`.
Debug-only behavior uses `src/testDebug`. Two shared source sets need explicit wiring:

- `app/src/journeyTest`: connection journeys run on Robolectric and devices via
  [app/build.gradle.kts](../app/build.gradle.kts).
- `core-database/src/migrationTest`: populated V3 migration contracts use local and
  device wrappers. Other test directories are not automatically shared.

## Writing tests

- Test behavior in the owning module/package. Construct subjects directly; use
  Hilt for integration and fresh, controllable fakes for external dependencies.
  Keep helpers out of production APKs.
- Use `runTest`, a shared scheduler and injected clocks. Virtual time does not
  advance a separate clock. Install/reset Main locally; retain real Main on devices.
- Observe pending states and cover cancellation/late results. Close databases,
  cancel jobs and restore dispatchers after tests.
- Verify transactions, constraints, concurrency and rollback with real Room.
  Test persistence by reopening files and migrations with populated schemas;
  destructive reset is not migration coverage.
- Exercise Compose callbacks, Back, focus and enabled state through semantics.
  Use screenshots for layout, not as proof of persistence or provider behavior.

## Final Figma visual acceptance

After the final UI change, compare affected states with full-resolution
[v5 exports](ui/README.md), matching data, window size and display/font scale.
Compare app content without exported system bars or Debug controls. Inspect artwork,
typography/wrapping, geometry, colors, icons, insets and touch bounds. Also check
compact/enlarged text and the actual AAOS window, including focus, recovery and
interrupted motion.

Record references, review images and unresolved differences in the PR. Missing
references remain explicit. Builds, behavioral tests and generated images do not
establish visual parity. SVG-only renames require XML/render and byte-preservation checks.

## Current requirement map

These are existing suites, not execution results. Update critical mappings when
behavior changes. [Architecture](ARCHITECTURE.md#planned-features) owns remaining gaps.
Related suites share the linked module/package.

### Boundaries, vehicle evidence and persistence

| Contract | Coverage |
| --- | --- |
| Module isolation and unique route registration | `verifyModuleBoundaries` in [root build](../build.gradle.kts); [FeatureRegistryTest](../core/core-navigation/src/test/java/com/monsters/mobimon/core/navigation/FeatureRegistryTest.kt) |
| Parking freshness, independent signals, original/display evidence and decorative clock | [VehicleFreshnessPolicyTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/VehicleFreshnessPolicyTest.kt), [VehicleStateViewModelTest](../core/core-presentation/src/test/java/com/monsters/mobimon/core/presentation/VehicleStateViewModelTest.kt) |
| Debug Park interpretation, one foreground connection and unavailable real adapter | [DemoVehicleRepositoryTest](../app/src/testDebug/java/com/monsters/mobimon/vehicle/DemoVehicleRepositoryTest.kt), [CompanionRuntimeTest](../app/src/test/java/com/monsters/mobimon/runtime/CompanionRuntimeTest.kt), [UnavailableVehicleRepositoryTest](../core/core-vss/src/test/kotlin/com/monsters/mobimon/core/vss/UnavailableVehicleRepositoryTest.kt) |
| Legacy ownership/revision, later evidence, atomic completion and reopening | [QuestEvaluatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/QuestEvaluatorTest.kt), [RoomCompanionRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/RoomCompanionRepositoryTest.kt) and its device counterpart |
| Point uniqueness, concurrent purchase/equip, rollback and authorization recheck | [PointEconomyRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/PointEconomyRepositoryTest.kt), [Q01JourneyTest](../app/src/test/java/com/monsters/mobimon/Q01JourneyTest.kt), [DebugPointRepositoryTest](../core/core-database/src/testDebug/java/com/monsters/mobimon/core/database/DebugPointRepositoryTest.kt) |
| V1→V4 and both V3 shapes preserve records/identity | [PointEconomyMigrationTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/PointEconomyMigrationTest.kt), [LevelingMigrationContract](../core/core-database/src/migrationTest/java/com/monsters/mobimon/core/database/LevelingMigrationContract.kt) with local/device wrappers |
| Supplied driving conditions, weather and catalog rules | [DrivingQuestEvaluatorTest](../core/core-domain/src/test/kotlin/com/monsters/mobimon/core/domain/DrivingQuestEvaluatorTest.kt); no trusted driving-evidence claim |

### Authentication

| Contract | Coverage |
| --- | --- |
| OAuth request/response validation, HTTP errors and redirects | [OkHttpGitHubApiTest](../core/core-auth/src/test/java/com/monsters/mobimon/core/auth/OkHttpGitHubApiTest.kt); MockWebServer |
| Poll intervals, slowdown, expiry, cancellation, persistence, refresh and revocation | [PersistentGitHubAuthenticationTest](../core/core-auth/src/test/java/com/monsters/mobimon/core/auth/PersistentGitHubAuthenticationTest.kt); fake provider/store |
| Keystore encryption, reopening, tamper rejection and deletion | [EncryptedCredentialStoreTest](../core/core-auth/src/androidTest/java/com/monsters/mobimon/core/auth/EncryptedCredentialStoreTest.kt); device |
| Authentication guards/recovery, readiness separation, QR decoding and success/disconnect actions | [Authentication feature suites](../feature/feature-auth/src/test/java/com/monsters/mobimon/feature/auth); ViewModel and Robolectric |

### Presentation and navigation

| Contract | Coverage |
| --- | --- |
| Loading/failure differs from committed values; retries retain data | [Shared presentation suites](../core/core-presentation/src/test/java/com/monsters/mobimon/core/presentation) and owning feature tests |
| Claim pending/duplicate/cancellation, committed amounts and reset reconciliation | [QuestViewModelTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestViewModelTest.kt), [QuestScreenTest](../feature/feature-quest/src/test/java/com/monsters/mobimon/feature/quest/QuestScreenTest.kt) |
| Independent catalog/inventory retry, preview isolation and friend-specific equipment | [Customization suites](../feature/feature-customization/src/test/java/com/monsters/mobimon/feature/customization), point repository suite |
| Independent settings writes, failure/retry and DataStore keys | [SettingsViewModelTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/SettingsViewModelTest.kt), [DataStoreSettingsRepositoryTest](../core/core-database/src/test/java/com/monsters/mobimon/core/database/DataStoreSettingsRepositoryTest.kt) |
| Artwork, background periods/dimensions, reduced motion and shared control bounds | [Core UI suites](../core/core-ui/src/test/java/com/monsters/mobimon/core/ui); native Robolectric images |
| Home/Settings, vehicle, store and quest layouts, focus and recovery | Owning feature `src/test` suites, including `CompanionReviewTest`, `VehicleReviewTest` and `StoreReferenceScreenTest` |
| Menu reference/AAOS-density/compact bounds, focus, authenticated chat routing, connection origin after authentication loss, recreation and restricted/outgoing input | [Shell suites](../app/src/test/java/com/monsters/mobimon/ui), [CopilotConnectionJourneyTest](../app/src/journeyTest/java/com/monsters/mobimon/CopilotConnectionJourneyTest.kt) |
| Conversation reveal/return, stationary Home, visible touch bounds, interruption, reduced motion and scrolled action bounds | [ConversationRevealTest](../app/src/test/java/com/monsters/mobimon/ui/ConversationRevealTest.kt), [PetHomeScreenTest](../feature/feature-pet/src/test/java/com/monsters/mobimon/feature/pet/PetHomeScreenTest.kt); native Robolectric frames and pointer input |
| Chat draft/composition lifetime, ownership clearing, input guards/actions and responsive layouts | [Conversation and feature suites](../feature/feature-auth/src/test/java/com/monsters/mobimon/feature/auth); native review images |
| Native keyboard resizing and Back/draft retention | [ConversationKeyboardDeviceTest](../app/src/androidTest/java/com/monsters/mobimon/preview/ConversationKeyboardDeviceTest.kt); AAOS device |
| Isolated Debug rehearsal and branding | [CopilotPreviewJourneyTest](../app/src/journeyTest/java/com/monsters/mobimon/preview/CopilotPreviewJourneyTest.kt), [BrandingTest](../app/src/testDebug/java/com/monsters/mobimon/BrandingTest.kt) |

## Integration boundaries

- Room migration fixtures cover populated V1 and original/expanded V3 upgrades and
  reopening, not a separately populated V2 fixture or `MigrationTestHelper`.
- [JourneyTestModule](../app/src/journeyTest/java/com/monsters/mobimon/testing/JourneyTestModule.kt)
  keeps MainActivity, feature ViewModels and Room repositories real, with in-memory
  Room, isolated DataStore and fake platform/vehicle/AAOS/authentication providers.
  It never contacts GitHub or accesses user credentials. Debug preview journeys
  use sample data and establish no authentication or vehicle verification.
- Recreation, file reopening and process restart are distinct. Local tests, APK
  assembly and `NO-SOURCE` tasks do not prove device execution, live providers,
  Release behavior or launcher support. Real OAuth approval/restart/revocation and
  AAOS restriction/reconnection behavior need separate target-device verification.
- Driving tests cover supplied formulas and transaction invariants, not trusted
  driving evidence, real occurrence identity or evaluator-to-award agreement.
  Hungry/sick rendering and on-device decorative lifecycle still lack acceptance.

Record revision, executed checks, skipped layers/reasons and device image/signal
source in the issue/PR. Keep credentials and private logs out of reports.

## Focused commands and reports

Use `--tests <qualified-name>` with the owning module's `testDebugUnitTest`, or
`test` for plain Kotlin. Filtered tasks do not execute dependency suites or devices.
Kover: `./gradlew :app:koverHtmlReportDebug :app:koverXmlReportDebug` (local JVM only; no percentage gate).
[CI](../.github/workflows/android-ci.yml) owns artifact paths and retention.

### CI AAOS environment

The [workflow](../.github/workflows/android-ci.yml) and [cstd.ini](../.github/avd/cstd.ini)
define the image, extension, ABI and display. Run the required
[host check](../scripts/check-aaos-environment.sh) with only the intended emulator
connected, then canonical device tests. Use emulator 35.1.9 or newer.

CI uses a fresh, headless AVD with software rendering and disabled animations.
Local CSTD images are separate inputs; matching metadata does not establish
identical binaries/services. The host check identifies the environment, not test execution.

# Application architecture

This document owns module boundaries, state, storage and platform contracts.
[DESIGN.md](DESIGN.md) owns UX, [UI exports](ui/README.md) own the visual inventory,
[TESTING.md](TESTING.md) owns coverage, and
[CONTRIBUTING.md](../.github/CONTRIBUTING.md) owns workflow.

## Current foundation

The app has an in-app Home, menu, Settings, customization, Vehicle and Quest
routes, plus Copilot presentation. The v5 exports are implementation references;
importing them does not migrate runtime screens or enable integrations.

| Area | Implemented support and limits |
| --- | --- |
| Shell | Saved routes/origin, transient menu, independent Hilt feature entries and global AAOS restriction gate |
| Storage | Room schema 4 for profiles, legacy XP/runs/completions, point wallet/ledger/occurrences, cosmetics/equipment and leveling records; DataStore preferences |
| Quests | Driving and hidden quest UI with point claims; legacy Q01 repository compatibility remains, but Q01 is no longer a current list entry |
| Catalog | `DefaultPointQuestCatalog` defines driving/hidden rewards; profile initialization seeds free Mobi/Luna, friend-specific accessories and backgrounds |
| Driving evaluation | In-memory `DriveEvaluationData`, currently populated by Debug controls; no verified real driving collector |
| Character | External assets through `PetAvatar`, including default Mobi breathing frames; reduced-motion wiring and v5 condition expressions remain incomplete |
| AI | Production introduction/unavailable state and isolated Debug connection rehearsal; no live provider, credential store, conversation or voice runtime |
| Vehicle/launcher | Debug simulated vehicle and Release unavailable adapter; no background tracker, overlay service or launcher renderer |

Source catalogs, not example SVG prices, define current data:
[quest definitions](../core/core-domain/src/main/kotlin/com/monsters/mobimon/core/domain/PointQuestModels.kt),
[cosmetic initialization](../core/core-database/src/main/java/com/monsters/mobimon/core/database/RoomCompanionRepository.kt).
Implemented catalog entries do not establish validated real-vehicle reward flows;
see the [remaining economy work](#points-cosmetics-and-quest-occurrences).

## Scope and decisions

Use MVVM and unidirectional data flow with constructor-injected repositories.
Keep rules in plain Kotlin; add use cases for coordination, not pass-through
wrappers. Records are local: there is no MobiMon backend, account service,
synchronization or guaranteed reinstall recovery. Simulated adapters do not
establish platform support.

## Target modules and dependencies

The modules below exist in [settings.gradle.kts](../settings.gradle.kts). Add a
planned module only with working behavior and tests; use `com.monsters.mobimon`
as the package root.

| Module | Responsibility | Allowed internal dependencies |
| --- | --- | --- |
| `app` | Entry points, shell, runtime and [Hilt registrations](../app/src/main/java/com/monsters/mobimon/di/features) | Features and core implementations |
| `core-domain` | Models, repository contracts and rules | None |
| `core-database` | Room, DataStore, migrations and transactions | `core-domain` |
| `core-vss` | Unavailable real vehicle adapter | `core-domain` |
| `core-ui` | Stateless components, theme, fonts and artwork | None |
| `core-navigation` | Typed routes, entries and callbacks | None |
| `core-presentation` | Shared wallet, vehicle and companion read state | `core-domain` |
| `feature-pet` | Home, customization, Settings; [entry](../feature/feature-pet/src/main/java/com/monsters/mobimon/feature/pet/PetFeature.kt) | Domain, UI, navigation, presentation |
| `feature-quest` | Quest progress and commands; [entry](../feature/feature-quest/src/main/java/com/monsters/mobimon/feature/quest/QuestFeature.kt) | Same four core modules |
| `feature-vehicle-info` | Readings and availability; [entry](../feature/feature-vehicle-info/src/main/java/com/monsters/mobimon/feature/vehicle/VehicleFeature.kt) | Same four core modules |
| `feature-auth` | Copilot presentation and AI context; [entry](../feature/feature-auth/src/main/java/com/monsters/mobimon/feature/auth/AiFeature.kt) | Same four core modules |

Features never import each other or concrete data implementations. `core-domain`
has no Android, Compose, Room, Hilt or SDK DTO dependency. Map transport/storage
models at implementation boundaries and assemble bindings in `app`. Keep helpers
internal until shared; test helpers must not become production dependencies.
There is no `core-ai`, overlay or `core-testing` module.

## State and lifecycle

Routes obtain feature ViewModels and collect read-only `StateFlow` with
`collectAsStateWithLifecycle()`. Screen composables accept state, callbacks and
`Modifier`; they do not open databases or call SDKs. Shared UI APIs stay stateless.

`MobiMonApp` owns route/menu state and cross-feature navigation. `FeatureRegistry`
rejects missing/duplicate destinations and saved names. Route ViewModels use the
Activity store and local UI state uses a saveable-state holder. The current
single-level shell preserves parent/origin behavior; it is not Navigation 3 or a
multiple-back-stack implementation.

`verifyModuleBoundaries` checks project dependencies in all configurations and
selected platform imports/plugins in domain main sources. Transitive libraries,
generated code, other source sets, fully qualified types and SDK DTOs still need
review; the task is a guardrail, not a complete dependency audit.

| State | Owner/lifetime |
| --- | --- |
| Profiles, rewards, wallet, catalog, ownership and equipment | Room repositories; durable |
| Reduced motion, Debug mode and dormant launcher preference | DataStore; independent keys, Debug/launcher defaults off |
| Vehicle connection and AAOS restriction listener | `CompanionRuntime`; process foreground lifecycle |
| Driving evaluation input | `PointEconomyRepository` memory; Debug-fed, not durable evidence |
| Route and connection origin | Saved shell state; open menu is transient |
| Preview, geometry and animation progress | Feature/renderer; not committed equipment |

Shared presentation observes committed wallet, vehicle and appearance state.
Store preview stays local until equip commits. Friend-specific equipment uses
scoped slot keys; switching friends preserves their separate selections. Failed
observations retain committed data and expose retry without duplicate collectors.
Quest owns its commands; Vehicle does not host quest reward actions.

[PetAvatar](../core/core-ui/src/main/java/com/monsters/mobimon/core/ui/PetAvatar.kt)
and [CharacterArtwork](../core/core-ui/src/main/java/com/monsters/mobimon/core/ui/CharacterArtwork.kt)
map display inputs to resources. They do not own points, ownership or interaction
authorization. Current `PetAvatar` has no vehicle-condition input; hungry/sick
mapping in v5 is a target, not existing renderer behavior. Reduced motion is saved
but callers do not yet pass it to `isAnimated`.

### Copilot connection UI

[CopilotUiState/Action](../feature/feature-auth/src/main/java/com/monsters/mobimon/feature/auth/CopilotUiState.kt)
and [CopilotConnectionScreen](../feature/feature-auth/src/main/java/com/monsters/mobimon/feature/auth/CopilotConnectionScreen.kt)
are presentation contracts. The host supplies data, QR painter, authorization and
actions. The screen accepts no token, performs no polling and creates no account
session. Rendering Connected does not verify approval or Copilot readiness.

Production `AiFeature` shows introduction/unavailable feedback and a disconnected
conversation entry, guarded by parking and app-use allowance. The shell preserves
Home/Settings origin. The separate
[Debug preview Activity](../feature/feature-auth/src/debug/java/com/monsters/mobimon/feature/auth/preview/CopilotPreviewActivity.kt)
uses example accounts, codes and timers without authentication or vehicle
verification. It saves review state across recreation; pending rehearsal work
runs only while STARTED and cancels when leaving its step. Its launcher and sample
resources are absent from Release. UX and motion belong in [DESIGN.md](DESIGN.md#copilot-connection-ui).

### Vehicle interaction authorization

`CompanionRuntime` owns one provider connection; screen collectors must not start
another. Debug uses `.demo`, `mobimon-demo.db` and `demo-profile`; Release uses
`mobimon.db`, `local-profile` and the REAL unavailable adapter. The Debug freshness
window is 15 seconds. Serialized updates allocate increasing sequences within an
epoch; only nonmoving Park is parked, motion is moving, and stationary D/R/N is
unknown. Display freshness samples the clock for every snapshot/timer emission.

Authorization requires valid parked evidence and the current display's AAOS
`CarUxRestrictionsManager` allowance. `CarAppUseMonitor` fails closed until a
current reading, on service loss and through reconnection. Unknown/stale data
cannot authorize commands. Transactions recheck current evidence and app-use
allowance; restriction changes preserve committed records. Debug controls leave
composition when app use is unavailable/restricted, and Debug writes recheck the
same allowance inside the transaction.

Signals carry source, quality, receive time, epoch and sequence. Parking, battery
and warnings age independently. Real adapters must normalize units and document
measurement ordering/freshness without mixing clock domains or reusing process
monotonic timestamps after restart. Last-known warnings are historical, not
current evidence.

## Domain and storage contracts

[AppDatabase](../core/core-database/src/main/java/com/monsters/mobimon/core/database/AppDatabase.kt)
is schema version 4. Use one database per app process; do not persist transient
vehicle histories or chat transcripts. Observations use `Flow`, writes suspend,
and outcomes distinguish rejection, duplicates and storage failure. Propagate
coroutine cancellation and inject clocks/IDs where behavior depends on them.

Legacy runs fix profile/source, rule version, reward and start boundary. Commands
recheck ownership/revision and permit one active run per profile. Completion
requires later evidence in the same epoch. After an observation gap, fresh parked
evidence may cancel an old run for restart, but cannot complete it. An AI assertion
is never completion evidence.

Reward and purchase writes are atomic:

- Legacy completion reloads/validates the run and evidence, inserts a unique
  completion, marks the run finished and increments XP in one transaction.
  Uniqueness covers run ID and `(profileId, questType)`; duplicates never re-award.
- Point awards validate the catalog, current matching snapshot and authorization,
  then insert an occurrence, ledger credit and balance change together. Unique
  `(profileId, questId, occurrenceKey)` and ledger references prevent duplicate credits.
- Purchases validate item, price, compatibility, ownership and funds, then debit
  and grant ownership together. Equip separately validates committed ownership
  without another charge. Failures roll back all writes; concurrent requests must
  not overspend or duplicate ownership.
- Debug adjustments are a Debug-only exception, recording account and ledger
  deltas together with unique references. Reset records a negative delta rather
  than silently replacing the balance.

Keep network calls outside transactions. Publish committed state; a UI event or
animation is never the only record of a reward. Do not split atomic writes across
Room and DataStore or replace reward records on conflict.

Migrations preserve historical evidence without destructive reset or XP-to-point
conversion. `MIGRATION_1_2` adds zero-balance wallets/free friends without historical
credits. V3 adds leveling tables; `MIGRATION_3_4` handles original and expanded v3
schemas, adding only missing fields with zero defaults while preserving existing
values, rewards and equipment. Register the full chain. Legacy Q01's 80 XP and
Q02/Q03 identifiers remain compatibility data, not current point reward mappings.
See [migration coverage](TESTING.md#integration-boundaries).

## Planned features

These are remaining contracts and integration gaps, not completed functionality.
They do not change the [v5 product rules](DESIGN.md).

### Points, cosmetics and quest occurrences

Current driving/hidden quest UI and catalog are implemented, but the reward
pipeline is not verified real driving progression. `DrivingQuestEvaluator`
computes conditions/weather-adjusted suggestions from in-memory data; the Room
award path validates parked snapshots and credits the catalog's base amount,
without validating those driving/hidden conditions in the transaction.

Before production enablement, bind trusted per-quest evidence to each claim and
validate it atomically. Replace placeholder per-drive IDs and capped-daily counters
with real occurrence identity/counts. Current `completedQuestIds` collapses all
occurrences into quest IDs, so repeat eligibility needs occurrence-aware display.
Reconcile evaluator amounts with catalog rewards. Run-based extensions must check
revision/start evidence and finish the run in the same credit transaction; the
current point API has no run handle. UI/AI must never grant balances or items.

Keep one-time and repeatable schedules explicit, including reset zone, clock,
interruption and qualifying evidence. Never enable recurrence by removing a
uniqueness constraint. Preserve Debug/Release identities and existing ownership
when changing the catalog; SVG sample products do not constitute a migration.

### Shared vehicle condition and overlay

Derive companion condition from the existing shared vehicle stream plus committed
appearance, without new provider connections or database-to-provider dependencies.
Missing/stale signals remain unavailable. Process restart or gaps begin a new
epoch; uninterrupted background observation is not guaranteed.

Launcher display needs a verified OEM/platform placement contract, lifecycle-owned
state, actual service/permission observation and explicit new user opt-in. A
persisted preference or Android overlay permission alone cannot authorize it.
A normal Activity cannot assume launcher visibility or unused screen space.
Verify restart, permission failure and parked restrictions before enabling it.

### AI conversation and session

Keep a future provider behind domain interfaces, with supported user credentials
and no embedded shared secrets. Verify the chosen Copilot SDK/CLI runtime,
ABI/process or server access, authentication and lifecycle on the target Android
device; a relay is a separate infrastructure decision. Stay unavailable until
both account approval and service readiness work.

Implement the [conversation lifetime](DESIGN.md#conversation) with a bounded,
profile-scoped in-memory session. Keep dialogue and requests out of Room,
DataStore, saved state and `rememberSaveable`; verify SDK/runtime artifacts meet
the same retention contract before claiming memory-only storage.

The provider host owns secret storage, expiry, polling and cancellation. Invalidate
requests when session, connection, profile or relevant context changes; accept a
reply only if request/session/profile IDs and context revision still match even
if cancellation fails. Inject timeouts and make retries explicit/idempotent.

An AI gateway receives bounded dialogue, committed state and valid vehicle facts.
It may return replies/allowlisted proposals but cannot write storage, vehicle
state, points or ownership. Commands still require evidence/confirmation. Voice
capture owns permission and cancellation separately and follows the same session
retention contract. AI failure must not disable local vehicle/quest/cosmetic UI.

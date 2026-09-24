# Application architecture

This document owns technical contracts. [DESIGN.md](DESIGN.md) owns UX,
[TESTING.md](TESTING.md) owns coverage, and
[CONTRIBUTING.md](../.github/CONTRIBUTING.md) owns development workflow.

## Current foundation

Implemented: Home, menu, Settings, customization, Vehicle and Quest routes;
Room storage, DataStore preferences; GitHub device authentication and encrypted
session restoration; experimental Copilot text conversation; Mobi/Luna artwork and
breathing animation.

Vehicle input and driving evaluation are Debug simulations. Release vehicle data
is unavailable. Debug raw VSS sources are interpreted before they become vehicle
snapshots; production VSS input still needs a verified adapter. Voice, condition
expressions, background tracking and launcher/overlay rendering remain unimplemented.
Catalog entries and
[UI exports](ui/README.md) do not establish real integration support.

## Scope and decisions

Use MVVM, unidirectional data flow and constructor-injected repositories. Domain
rules stay in plain Kotlin; use cases coordinate work rather than wrap calls.
Records are local, with no MobiMon backend, synchronization or reinstall recovery.

## Target modules and dependencies

[settings.gradle.kts](../settings.gradle.kts) registers the modules.

| Module | Responsibility | Internal dependencies |
| --- | --- | --- |
| `app` | Shell, runtime and Hilt bindings | Features and core implementations |
| `core-domain` | Models, contracts and rules | None |
| `core-auth` | GitHub OAuth, credential storage and experimental Copilot transport | Domain |
| `core-database` | Room, DataStore and transactions | Domain |
| `core-vss` | VSS raw models, generated signal containers and adapter seam | Domain |
| `core-ui` | Stateless components, theme and artwork | None |
| `core-navigation` | Routes, entries and callbacks | None |
| `core-presentation` | Shared wallet, vehicle and appearance state | Domain |
| `feature-pet` | Home and Settings | Domain, UI, navigation, presentation |
| `feature-customization` | Catalog, preview, purchase and equipment | Same four core modules |
| `feature-quest` | Quest progress and commands | Same four core modules |
| `feature-vehicle-info` | Vehicle readings and availability | Same four core modules |
| `feature-auth` | Authentication, keyboard conversation UI and AI context | Same four core modules |

Features never depend on each other or concrete data implementations. Domain has
no Android, Compose, Room, Hilt or SDK DTO dependency. Map transport/storage models
at implementation boundaries; bind implementations in `app`. Test helpers stay
outside production sources.

`verifyModuleBoundaries` checks project dependencies, production external dependencies,
resolved JVM graphs and selected source imports. Domain remains plain Kotlin.
`core-vss` is an Android library so it can host the closed-network adapter seam,
but it still cannot own Room, DataStore, Hilt, UI, presentation or feature code.
Generated code, qualified references and DTO leaks still need review; the guard is
not a complete dependency audit.

## State and lifecycle

Routes collect ViewModel `StateFlow` with `collectAsStateWithLifecycle()` and pass
state/callbacks to screens. `MobiMonApp` owns navigation; `FeatureRegistry` rejects
missing/duplicate destinations and duplicate saved names. Route ViewModels use
the Activity store; local UI uses a saveable-state holder. The shell saves route
and connection origin; menu state and animation geometry remain transient.

Conversation navigation carries the activated button's bounds to the shell;
[DESIGN.md](DESIGN.md#motion) owns reveal and return behavior.

| State | Owner/lifetime |
| --- | --- |
| Profiles, rewards, wallet, inventory and equipment | Room; durable |
| Reduced motion, Debug and dormant launcher preferences | DataStore; independent keys, Debug/launcher default off |
| Vehicle connection and AAOS listener | `CompanionRuntime`; one connection per process foreground |
| Driving evaluation | Repository memory; Debug-fed, not durable evidence |
| Preview and animation | Feature/renderer; never committed equipment |

Shared presentation retains committed values on read failure, with explicit retry
and no duplicate collectors. Settings writes and customization stream retries are
independent of other loads; preview reaches shared appearance only on commit.

Background time uses supplied time or `UtcClock` plus local time zone and never
changes vehicle evidence.
[PetAvatar](../core/core-ui/src/main/java/com/monsters/mobimon/core/ui/PetAvatar.kt)
never owns rewards, equipment or authorization. Mobi's idle renderer caches one
atlas off the main thread, selects cells from elapsed Compose frame time, and
reads time only during draw/layer updates. Its fixed destination preserves layout. Adjacent cells blend with complementary
alpha in an isolated reusable layer (two atlas draws, no bitmap crops). Continuous
breath, sway and bob use separate periods; hands and wheel transform together.
The 7524 x 5016 asset retains every source pixel; runtime keeps the prior 2x decode
sampling (627px cells, about 36MiB) to fit a 4096px texture without a 144MiB bitmap.
Home and Vehicle Info share `core-presentation`'s `vehicleCondition()` classification of the freshness-filtered snapshot.
Only `WARNING` selects Mobi's collapsed idle. A 200ms opacity crossfade switches between normal and collapsed idle;
there are no falling/recovery states or transition frames. Interruptions continue from the current opacity.
Two canonical collapsed bitmaps load once off thread. A cached bitmap mesh deforms only the torso inside a fixed
destination rectangle over four seconds. All ground-contact vertices remain unchanged. A 6.5-second tired blink
blends identical-body closed/open-eye bitmaps; no whole-pose frame registration occurs at runtime.
Non-warning conditions restore existing normal idle without changing vehicle evidence.
Leaving composition cancels playback. Idle breathing ignores the motion preference; reduced motion replaces
Luna's run cycle with the idle breath and selects a static normal/collapsed endpoint for Mobi warnings.
Failed/unknown motion preferences pause other decoration; retries follow shell subscription.
The floating companion overlay applies the same preference: reduced, unknown or failed motion stops autonomous
wandering; dragging stays available.

### Window geometry

The shell owns runtime safe-drawing insets; the sibling menu handles its own
safe window. Features use the available content constraints and width-based
reference scaling, with scrolling or compact layouts for enlarged text. System
bar pixels from the SVG are reference coordinates, never fixed runtime padding.
Conversation additionally handles IME insets and resize without changing width scale.

The floating companion uses full-window coordinates with explicit system-bar and
cutout bounds from its overlay window context and current window metrics. Initial
placement, dragging and each wander step clamp its measured size. Inset/layout
changes reclamp an idle companion.
Debug panels and unlock notices apply safe-drawing insets independently in both build variants.
Panels measure and clamp inside that safe content.
The [overlay verification boundary](#shared-vehicle-condition-and-overlay) still applies.

### Copilot connection UI

`app` binds the domain `GitHubAuthentication` contract to `core-auth`.
[GitHub device flow](https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/authorizing-oauth-apps)
uses an app-owned public client ID, no client secret and `read:user` scope.
[Build configuration](../app/build.gradle.kts) supplies the ID; unconfigured builds
disable sign-in. Polling respects provider intervals, slowdown and expiry.
Leaving/backgrounding or losing parked authorization cancels pending approval.
Device approval requests and acceptance recheck parking and AAOS allowance.

Authentication requires provider approval, `/user` identity validation and durable
credential storage; it does not establish Copilot readiness. The UI receives no
token and performs no polling. Rendered success and Debug preview accounts/codes
are not provider verification; previews remain isolated from Release.

Credentials use atomic AES-256-GCM storage in `noBackupFilesDir` with an Android
Keystore key; package identity separates Debug and Release. Tokens never enter
UI/domain state, saved state, Room, preferences, logs or backups. App foreground
startup restores/validates credentials and refreshes expiring tokens, persisting
rotated tokens before identity validation. Network errors preserve credentials;
revocation/expired refresh tokens require approval again. Unreadable storage fails
closed. Disconnect removes the local credential/key, not the GitHub grant or subscription.

### Keyboard conversation UI

`feature-auth` owns chat; the shell gates new/restored routes on GitHub authentication
and preserves the connection entry route. Fixtures stay in Debug/test sources.
`app` binds domain `ConversationProvider` to the experimental `core-auth` HTTP adapter.

`ConversationViewModel` keeps the draft, selection, IME composition and completed
exchanges in Activity memory across navigation/configuration changes. Profile/account
changes, disconnect and process restart clear them; temporary failures retain them.
New conversation clears the draft and exchanges. Leaving, backgrounding, parking loss
or companion changes cancel pending work; request generations reject late replies.

Explicit Send checks Copilot access and the model catalog; opening chat performs
no preflight. The adapter selects `gpt-4o` only when the catalog advertises it as
enabled for Chat Completions, with no fallback model. It calls Chat Completions
directly without an Auto request or session token. Only a successful reply
establishes readiness. [OkHttpCopilotApi](../core/core-auth/src/main/java/com/monsters/mobimon/core/auth/OkHttpCopilotApi.kt)
owns endpoints, API versions, headers and model metadata.

The memory-only conversation ID changes on a new conversation or profile/account/
companion change; navigation preserves it. Access and selected model are cached
for at most five minutes per credential and rechecked after refresh or errors.

[CopilotMessageCodec](../core/core-auth/src/main/java/com/monsters/mobimon/core/auth/CopilotMessageCodec.kt)
supports Chat Completions and Responses text; other formats fail visibly. Requests
send only the companion name, fixed system instruction and dialogue, with no tools,
vehicle readings or reward/ownership commands. Responses disable storage/truncation;
these flags do not guarantee provider non-retention. Replies return complete text;
reasoning is excluded from display and history.

Tokens stay inside `core-auth`; the credential owner handles OAuth refresh. Recheck
credential ownership and parked/AAOS authorization before each network stage and
reply acceptance. Copilot credentials go only to allowlisted HTTPS hosts; redirects
and automatic HTTP retries are disabled. Errors expose recovery categories, never
provider bodies; bounded JSON/plain-text errors become fixed categories only.
Debug diagnostics contain only stage, HTTP status, catalog counts
and fixed rejection categories; Release logging is disabled. Cancellation cannot
undo provider processing; retries may consume additional usage.

Enforce [ConversationLimits](../core/core-domain/src/main/kotlin/com/monsters/mobimon/core/domain/ConversationProvider.kt),
reserving room for the next reply. Require editing or a new conversation rather than
silently dropping context. No dialogue enters Room, DataStore, saved state or a backend.

The transport follows [Copilot CLI OAuth authentication](https://docs.github.com/en/copilot/how-tos/copilot-cli/set-up-copilot-cli/authenticate-copilot-cli)
but has no supported Android SDK or stable HTTP contract. It makes real requests
without a MobiMon backend; approval/fixtures do not prove endpoint access for this
app/account. Rejections stay visible; never impersonate another OAuth client or editor.

### Vehicle interaction authorization

Debug uses `.demo`, `mobimon-demo.db` and `demo-profile`; Release uses `mobimon.db`,
`local-profile` and a closed-network `VssRawVehicleSource` adapter when present.
Local Release validation falls back to the default parked VSS source so the hidden
Debugger flow can be checked without vehicle hardware. Debug freshness is 15 seconds.
Only nonmoving Park is parked; motion is moving and stationary D/R/N is unknown.

Commands require fresh parked evidence and the current display's AAOS allowance.
`CarAppUseMonitor` fails closed on unknown state, service loss and reconnection.
Transactions recheck evidence and allowance. Restrictions preserve committed data
but remove Debug controls and block their writes.

`VehicleReading` pairs a freshness-normalized display with its original evidence.
Commands use the original evidence; display ages cannot alter transaction identity.
Initial reads and the shared one-second ticker normalize freshness.

Signals carry source, quality, receive time, epoch and increasing sequence.
Parking, battery and warnings age independently; missing/stale values are unavailable,
not healthy. Real adapters must normalize units and define measurement ordering
without mixing clock domains or reusing monotonic timestamps after restart.

## Domain and storage contracts

[AppDatabase](../core/core-database/src/main/java/com/monsters/mobimon/core/database/AppDatabase.kt)
is schema 4, with one instance per process. Do not persist transient vehicle
histories. Observations use `Flow`; writes suspend and distinguish rejection,
duplicates and storage failure. Propagate cancellation and inject clocks/IDs.

All reward writes validate evidence, ownership/revision and uniqueness atomically:

- Legacy runs fix profile/source, rule version, reward and start boundary, with one
  active run per profile. Completion requires later evidence in the same epoch and
  commits completion, run finish and XP together. Run ID and `(profileId, questType)`
  are unique. After a gap, fresh parking can cancel a run, never complete it.
- Point awards validate catalog, matching snapshot and authorization, then commit
  occurrence, ledger and balance together. `(profileId, questId, occurrenceKey)` and
  ledger references are unique. UI/AI assertions cannot establish completion.
- Purchase validates price, compatibility, ownership and funds before atomic debit
  and ownership grant. Equip validates committed ownership without another debit.
  Failures roll back; concurrent calls cannot overspend or duplicate rewards/items.
- Debug adjustments commit ledger/balance together; reset records a negative delta.

Keep network calls outside transactions. Never split atomic writes across Room and
DataStore or replace rewards on conflict. Return `Awarded` only after commit,
with the committed amount; `AlreadyAwarded` never writes another credit.

Migrations preserve evidence, rewards and equipment without destructive reset or
XP-to-point conversion. V1→V2 adds zero-balance wallets/free friends; V3 adds leveling
records; V3→V4 supports both original and expanded V3 schemas. Register the full
chain. Legacy quest IDs/XP are compatibility data, not current reward definitions.

## Planned features

These are integration gaps, not completed functionality.

### Points, cosmetics and quest occurrences

[Catalogs](../core/core-domain/src/main/kotlin/com/monsters/mobimon/core/domain/PointQuestModels.kt)
define rewards, not SVG examples. The award transaction now gates each driving quest on
its per-quest evidence (`DrivingQuestEvaluator.evaluateById` over the in-memory drive
evaluation) inside the credit transaction, returning `ConditionNotMet` when unsatisfied;
hidden quests have no driving condition and stay ungated. In debug the evidence is derived
live from the simulated VSS signals (`DebugVssState.toDriveEvaluationData`), and history
VSS cannot express (safe-drive count) is accumulated in the overlay. Evidence is still
simulated in-memory data crediting catalog base amounts. Production needs a trusted evidence
source, real occurrence IDs/counts and agreement with weather-scaled evaluator amounts.

`completedQuestIds` cannot represent repeat eligibility. Define recurrence, reset
zone/clock, interruption and evidence explicitly without weakening uniqueness.
Run-based extensions must validate revision/start evidence and finish the run in
the credit transaction. Preserve identities and ownership when catalogs change.

### Shared vehicle condition and overlay

Condition rendering must consume the existing shared vehicle stream and committed
appearance. Gaps/restarts start a new epoch; background observation is not guaranteed.
Launcher/overlay support needs a verified OEM placement/lifecycle contract, observed
permissions/service state and explicit opt-in. A preference or overlay permission
alone cannot authorize placement. Verify restart, failures and parked restrictions.

### AI conversation and session

Future SDK/relay adapters need target verification and must preserve the
[conversation contract](#keyboard-conversation-ui), including any SDK artifacts.

AI may return replies or allowlisted proposals, never write vehicle state, rewards
or ownership. Commands still require evidence/confirmation. Voice owns permission
and cancellation; AI failure must not disable local vehicle, quest or cosmetic UI.

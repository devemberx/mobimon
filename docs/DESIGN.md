# Product design

MobiMon is a companion for time spent parked: check vehicle information, talk,
complete quests and personalize Mobi or Luna. The supplied
[v5 SVG references](ui/README.md) are the current visual target. This document
owns UX behavior; [ARCHITECTURE.md](ARCHITECTURE.md) records implemented support
and technical gaps, and [TESTING.md](TESTING.md) owns verification.

Use the SVGs for component positions, text alignment, character scale, spacing,
colors, shapes and icons. Do not duplicate their measurements here. The rules
below cover behavior, runtime adaptation and intentional reference exceptions.

## Concept

The product loop is **quests → points → accessories → personalization**. Points
are spendable rewards, not XP. Do not add levels, evolution, growth stages or a
visible driving score from legacy code or obsolete annotations. Historical XP
and internal driving evaluation are separate implementation concerns.

Allow interaction only while parking is verified and AAOS permits app use.
Moving or unknown state pauses interaction while preserving drafts and committed
rewards, purchases and preferences. Expressions supplement specific vehicle
information; they never diagnose a vehicle or replace its warnings.

## Visual language

- Use the fixed Twilight palette through `MobiMonTheme` and bundled Noto Sans KR
  Regular/Bold. Reflow or scroll for compact windows and enlarged fonts; preserve
  readable text and touch targets instead of shrinking the whole interface.
- Follow the [export content bounds](ui/README.md) when comparing layouts. Use
  runtime insets instead of drawing the exported OS bars; keep the menu inside the
  app content area.
- Keep primary touch targets at least 76 × 76dp, with 24dp spacing/edge clearance
  where possible. Preserve 4.5:1 text and 3:1 meaningful-icon/boundary contrast.
  Combine color with a label or shape for every status.
- Reuse circular navigation controls with distinct Home menu and child Back glyphs.
  Parking badges need the same icon, wording and dimensions across screens;
  point balances remain separate information. Home, Copilot, Quest, Vehicle and
  Settings reuse
  [MobiMonParkingBadge](../core/core-ui/src/main/java/com/monsters/mobimon/core/ui/MobiMonParkingBadge.kt)
  with Copilot's icon, baseline and capsule geometry;
  enlarged text uses a content-sized layout. Confirmed/unconfirmed wording is
  shared; Home retains charging/moving labels and simulation stays separate.
- Order screen-reader/rotary focus by title, information, primary action and
  secondary actions. Trap dialog focus and return it to the trigger on close.

### Launcher icon and native splash

The launcher and [native splash](../app/src/main/res/values/themes.xml) use shared
Mobi artwork on Night, without a wordmark. Preserve adaptive-mask clearance and
the character motif in the monochrome icon. These are app-specific adaptations,
not supplied SVG designs. Dismiss the splash on the first app frame without a hold.

## Reusable Compose library and asset handoff

Use [core-ui](../core/core-ui/src/main/java/com/monsters/mobimon/core/ui) before
adding feature-local equivalents. Shared composables accept values, slots and
callbacks; repositories and ViewModels remain outside them.

Feature owners compose final screens and perform
[visual acceptance](TESTING.md#final-figma-visual-acceptance). The Debug UI catalog
previews components, not provider behavior. Decorative renderers obey explicit
opt-outs and `LocalMobiMonMotionEnabled`; neither grants interaction authorization.

Keep full-screen SVGs only in `docs/ui`. Import individual original icons into
the owning feature with a feature prefix, retaining vector paths during Android
conversion. Put shared character art and fonts in `core-ui`, original raster
bytes in `drawable-nodpi`, and keep the [font license](../core/core-ui/src/main/assets/fonts/OFL-NotoSansKR.txt).
Do not use whole-screen SVGs as runtime UI or duplicate shared artwork.

### Image asset locations

| Asset | Location |
| --- | --- |
| Animated character frames | `core/core-ui/src/main/assets/characters/{mobi,luna}/idle_breath/` |
| Shared character poses and equipped appearances | `core/core-ui/src/main/res/drawable-nodpi/mobimon_*.png` |
| Store accessory thumbnails | `mobimon_mobi_items.png` and `mobimon_luna_items.png` in the same shared drawable folder; `CharacterArtwork` selects crops |
| Home and store-preview backgrounds | `core/core-ui/src/main/res/drawable-nodpi/pet_home_background_{morning,day,afternoon,sunset,night}.webp` |
| Home menu, Settings, conversation and speech-bubble vectors | `feature/feature-pet/src/main/res/drawable/pet_*.xml` |
| Shared parking icon | `core/core-ui/src/main/res/drawable/mobimon_parking.xml` |
| Store navigation/category icons | `feature/feature-customization/src/main/res/drawable/store_*.xml` |
| Menu artwork and icons | `app/src/main/res/drawable-nodpi/drawer_*.png` and `app/src/main/res/drawable/drawer_*.xml` |
| Quest artwork and icons | `feature/feature-quest/src/main/res/drawable-nodpi/` and `res/drawable/` |
| Full-screen design references | [docs/ui](ui/README.md); not packaged in the app |
| Local generation drafts | `output/imagegen/` (ignored); intermediate files go in ignored `tmp/imagegen/` and are deleted after use |

Use stable names describing character, state, equipment or time of day, without
visual revision suffixes. Promote only approved images into runtime folders.
Keep generation prompts and unused manifests out of runtime assets; retain required
database schemas, migration fixtures and tool configuration.

Home and customization share five 2560 × 1440 lossless WebP backgrounds with
matching framing. PNG generation drafts stay outside runtime resources.
Local hours 06–11 use Morning, 12–15 Day, 16–17 Afternoon, 18–19 Sunset and 20–05
Night. This is a fixed visual schedule, not an astronomical sunrise/sunset model.
Provided time values (including Debug overrides) take priority; otherwise the
background follows the device's local clock and time zone while the app is open.
Clouds and lighting vary; permanent scenery and character placement stay fixed.
These runtime backgrounds replace the static SVG backdrop. Home center-crops
them with a light, cool tint and daylight text shadows. Artwork and tint crossfade
together over one second, disabled by reduced motion; controls remain untinted.

Use [home.svg](ui/shell/home.svg) for Home geometry. The menu overlays the same
scene without resizing or moving it; compact layouts use a scrollable arrangement.

Home's speech bubble pops in from its tail on entry, pet tap, and periodic reappearance.
Data updates do not replay it; reduced motion displays the settled bubble immediately.
The animated time-of-day phrase replaces the SVG subtitle; do not render both.
Availability and restriction notices appear only when needed
and do not move the main action. Compact layouts remain scrollable, including
notices below the action.

[PetAvatar](../core/core-ui/src/main/java/com/monsters/mobimon/core/ui/PetAvatar.kt)
is the artwork replacement boundary. Preserve identity, proportions, lighting,
material and steering-wheel motif across expressions. Match visible character
bounds, not only the image canvas; do not stretch, crop or redesign the character
for Vehicle or quest completion. Rewards, equipment and selection stay outside
the renderer.

## Screens and navigation

The [export index](ui/README.md#screen-index) owns the screen and state inventory.

The menu opens from Home and closes through its close control, backdrop or Back.
Its footer stays above the bottom system bar. Selecting a destination closes the
menu; its portrait/name follow the equipped friend. Mobi uses the supplied head
portrait (`drawer_mobi.png`); other friends use `PetAvatar`. The footer uses the app
version. Preserve the SVG layout at AAOS compatibility density, centering minimum
touch bounds around the original rows. Reflow only when those bounds would overlap
or enlarged text needs more room. Back closes the keyboard,
then a dialog/menu, then the current destination. Menu destinations return Home;
connection screens preserve their Home/Settings origin. Purchase cancellation
returns to the same preview. An explicit Home action always opens Home.

Unavailable services must explain the limitation and offer a relevant next step.
Pending actions prevent repeated input. A result that cannot be confirmed offers
status reconciliation before retrying a potentially committed transaction.

## Quests and points

One-time rewards are unique per profile and quest; repeatable rewards are unique
per defined occurrence. Show the actual repeat period and next eligibility.
There is no universal daily reset. SVG quest titles and reward amounts are
examples; use the current catalog and validated evidence rather than copying them
into business logic.

Show received/success only after persistence commits. Saving retains the quest;
an unconfirmed save preserves progress and checks durable status before retry.
Reopening a result or replaying animation must never award again. A loading or
failed wallet is not zero. All screens use the same committed balance, labelled
Points or `1,200 P`; earned-today totals are different from spendable balance.

## Customization

Friends, accessories and backgrounds are independent choices. Keep each friend's
owned/equipped items when switching friends. Previewing is local; purchasing
confirms ownership, and applying is a separate action. Show the selected friend's
name and art consistently and explain compatibility before purchase.

| State | Action |
| --- | --- |
| Unowned | Preview and purchase with explicit price |
| Insufficient points | Show shortfall and route to quests |
| Owned | Apply without another purchase |
| Applied | Identify the active item; no redundant action |
| Pending | Disable duplicate actions and show progress |
| Unconfirmed/failed | Preserve preview and committed selection; reconcile/retry |

Confirmation shows item, price, balance and resulting balance. Cancellation does
not spend points; closing after a committed purchase does not undo it. Vehicle
condition changes preserve cosmetics. No cash purchases, top-ups or conversion
are part of this design. Products/prices in SVGs are visual examples, not a
replacement for the repository catalog. The export index records the missing
standalone unowned-item preview reference.

## Conversation

Voice follows **permission → listening → transcript review → send → wait → reply**.
Stopping recording does not submit it. Allow correction/cancellation and offer
text input when the microphone is unavailable. Use the system keyboard, keep the
composer above it and preserve draft/selection without submitting unfinished IME
composition. Reduce the character to an identity marker when height is limited.

Identify speakers, suppress duplicate sends and preserve drafts/completed replies
on failure. Provide explicit recovery for network, timeout, usage and account
errors. Closing conversation cancels pending work; completed exchanges survive
navigation within the session, but a new session or restart clears them. Spoken
replies have independent controls and yield to calls/navigation. Restriction
stops playback and recording.

Explain what conversation/vehicle information is sent and retained before
connection. App session clearing or disconnect does not promise deletion by the
provider. Long-term personal memory is outside scope.

## Vehicle information

| Available condition | Character and information |
| --- | --- |
| Low battery / charging required | Hungry expression, actual charge and charging guidance |
| Vehicle issue | Sick expression, affected item/location and specific warning |
| Current checked items without warnings | Default expression; describe only the checked items as normal |
| Stale, disconnected or unavailable | Explain missing/currently unknown information and last update; never imply normality |

Specific warnings take priority when multiple conditions apply. Derive the
expression from current validated condition; navigation or recomposition must
not reset it to default. Partial data stays partial: a fresh battery does not
prove fresh tire information. Unavailable readings are neither zero nor healthy.

## AI connection and settings

Preferences save independently and immediately; Done closes the screen. Preserve
the last committed value and expose save failures. Unknown parking disables
changes. Debugger is Debug-only and defaults off; Release omits it. Spoken replies
remain unavailable; Settings omits Do Not Disturb.

The supplied Settings export shows a vehicle-home placeholder and says the
character is static. It does not enable a launcher feature. Current code contains
Mobi/Luna breathing frames. The committed reduced-motion preference pauses
character and particle loops and removes shell destination motion; unknown or
failed preference reads keep decoration static. The vehicle-home row is
informational and cannot enable a launcher.

### Copilot connection UI

Production currently exposes introduction/unavailable feedback. Keep the QR action
disabled until a verified provider exists. Companion observation failure shows
Retry and retains the last committed appearance without claiming it is current.

The eight approval/connection states are reusable presentation and a separate
Debug rehearsal. Example accounts, QR codes and timers are review data. Hide
expired codes, offer address help and distinguish account approval from Copilot
readiness. Their technical boundary is in
[ARCHITECTURE.md](ARCHITECTURE.md#copilot-connection-ui).

### Planned live connection

Explain approval, expiry, cancellation and data use. Detect approval with manual
recheck as a fallback; distinguish authentication, access and usage failures.
Disconnect cancels AI work and removes the app connection while preserving points
and cosmetics. It does not cancel a subscription or revoke provider access.
Vehicle and customization remain usable without AI.

## Motion

Use brief, quiet transitions that preserve context and focus. Outgoing or
restricted controls must stop receiving input immediately; animation never
controls authorization or commit success. Reduced motion stops decorative loops
through the shared renderer contract.

Home's conversation action reveals the destination from the activated button's
rounded bounds over 300ms, with Home stationary underneath. Back closes it toward
the same bounds over 220ms. Sample the button after scrolling and respect runtime
insets. Destination touch targets follow the visible reveal bounds. Reduced
motion shows the destination immediately.

Other Android navigation/drawer transitions use 220ms; connection panels fade
in over 180ms and out over 120ms. Expiry/restriction changes replace content
immediately, and countdown updates keep panel identity. The v5 Figma prototype's
180ms dissolves and delayed success demonstrations are review transitions; the
static exports contain no animation metadata, and a demo delay is never a
purchase/reward completion signal.

## Vehicle launcher

There is one in-app Home. A launcher character is outside current support; its
dormant off-by-default preference cannot authorize display. A future launcher
requires a verified platform contract and an explicit UX/opt-in design.

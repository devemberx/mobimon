# Product design

MobiMon is a companion for time spent parked: check vehicle information, talk,
complete quests and personalize Mobi or Luna. The supplied
[v5 SVG references](ui/README.md) are the current visual target. This document
owns UX behavior; [ARCHITECTURE.md](ARCHITECTURE.md) records implemented support
and technical gaps, and [TESTING.md](TESTING.md) owns verification.

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

- Use Home's Twilight theme everywhere: Night backgrounds, Surface panels, Cream
  primary actions, Sky selection/focus, Mint success, Amber caution and Coral
  warnings. Keep one fixed palette through `MobiMonTheme`.
- Use bundled Noto Sans KR Regular/Bold and shared typography. Start with 32sp
  main and 24sp secondary text; reflow or scroll for compact windows and enlarged
  fonts instead of scaling the whole interface down.
- The references are 2560 × 1440 with drawn system bars of 76 units above and 96
  below. The app content is 2560 × 1268. Use actual runtime insets and never draw
  a second set of OS bars. The menu must remain inside that content area.
- Keep primary touch targets at least 76 × 76dp, with 24dp spacing/edge clearance
  where possible. Preserve 4.5:1 text and 3:1 meaningful-icon/boundary contrast.
  Combine color with a label or shape for every status.
- Match circular navigation controls across screens. Home uses a menu glyph;
  child screens use Back. Their containers match, but the glyphs retain their
  distinct meaning. Parking badges use the same parking icon and dimensions
  wherever that badge is shown; point balances are separate information.
- Home prioritizes the character and conversation action. Information screens
  pair a character with content; customization prioritizes preview and selection.
  Preserve those different hierarchies rather than repeating one card layout.
- Order screen-reader/rotary focus by title, information, primary action and
  secondary actions. Trap dialog focus and return it to the trigger on close.

### Launcher icon and native splash

The launcher reuses the shared [Mobi artwork](../core/core-ui/src/main/res/drawable-nodpi/mobimon_mobi_v4.png)
on Night, without a wordmark. The [foreground inset](../app/src/main/res/drawable/ic_launcher_foreground.xml)
preserves its proportions and adaptive-mask clearance; the
[monochrome vector](../app/src/main/res/drawable/ic_launcher_monochrome.xml) retains
the sprout, face and steering-wheel motif for themed launchers. These are icon
adaptations of the current character, not an exported Figma launcher design.

[Theme.MobiMon](../app/src/main/res/values/themes.xml) supplies the same Night
background and icon to the native splash. Android dismisses it on the first app
frame; there is no custom splash Activity or hold. API 34 is the minimum, so
adaptive resources replace the obsolete density-specific launcher bitmaps.

## Reusable Compose library and asset handoff

Use [core-ui](../core/core-ui/src/main/java/com/monsters/mobimon/core/ui) before
adding feature-local equivalents. Shared composables accept values, slots and
callbacks; repositories and ViewModels remain outside them.

| APIs | Responsibility |
| --- | --- |
| `MobiMonTheme`, `MobiMonColors`, `MobiMonTwilightColors`, `MobiMonDimensions` | Semantic color, typography, spacing and target sizes |
| `MobiMonDestination`, `MobiMonContentColumn`, `MobiMonSection` | Navigation shell, scrolling and groups |
| `MobiMonButton`, `MobiMonPanel`, `MobiMonListItem` | Actions, panels and aligned rows |
| `MobiMonTabs`, `MobiMonTab`, `MobiMonSelectionCard` | Caller-owned selection and focus |
| `MobiMonStatusBadge`, `MobiMonMessage`, `MobiMonSourceBadge` | Labelled state, feedback and signal provenance |
| `MobiMonPointSummary` | Loading, failed, zero and committed balances |
| `PetAvatar`, `CompanionIcon` | Replaceable character assets and shared icons |

Feature owners compose final screens and perform
[visual acceptance](TESTING.md#final-figma-visual-acceptance). A tested shared
component does not establish full-screen parity. The Debug UI catalog is a
component preview without a repository or provider.

Keep full-screen SVGs only in `docs/ui`. Import individual original icons into
the owning feature with a feature prefix, retaining vector paths during Android
conversion. Put shared character art and fonts in `core-ui`, original raster
bytes in `drawable-nodpi`, and keep the [font license](../core/core-ui/src/main/assets/fonts/OFL-NotoSansKR.txt).
Do not use whole-screen SVGs as runtime UI or duplicate shared artwork.

[PetAvatar](../core/core-ui/src/main/java/com/monsters/mobimon/core/ui/PetAvatar.kt)
is the artwork replacement boundary. Preserve identity, proportions, lighting,
material and steering-wheel motif across expressions. Match visible character
bounds, not only the image canvas; do not stretch, crop or redesign the character
for Vehicle or quest completion. Rewards, equipment and selection stay outside
the renderer. Existing `*_v4` resource names identify implementation assets and
are not instructions to target an older Figma page.

## Screens and navigation

The [export index](ui/README.md#screen-index) owns the complete state inventory.

| Area | Primary purpose |
| --- | --- |
| Home | Equipped companion/background, points, parking status and conversation entry |
| Menu | Overlay with Home, Conversation, Quests, Vehicle, Customization and Settings |
| Vehicle | Readings, specific warnings, freshness and recovery |
| Quests | Conditions, progress, claim/save/result and useful empty states |
| Customization | Friends, accessories and backgrounds; preview, buy and apply |
| Connection | Approval, expiry, help, readiness, reconnect and disconnect |
| Conversation | Messages, voice capture/review, keyboard input and recovery |
| Settings | Connection guidance and independent preference controls |

The menu opens from Home and closes through its close control, backdrop or Back.
Its footer stays above the bottom system bar. Selecting a destination closes the
menu; its portrait/name follow the equipped friend. Back closes the keyboard,
then a dialog/menu, then the current destination. Menu destinations return Home;
connection screens preserve their Home/Settings origin. Purchase cancellation
returns to the same preview. An explicit Home action always opens Home.

Unavailable services must explain the limitation and offer a relevant next step.
Pending actions prevent repeated input. A result that cannot be confirmed offers
status reconciliation before retrying a potentially committed transaction.

## Quests and points

A card shows the condition, reward, schedule/status and one next action. Filters
are All, In progress and Completed, with distinct empty states. Center the empty
illustration and its text/action as one stack, keeping balanced space above and
below. Quest artwork uses the same companion identity as Home.

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

Across the three supplied condition screens, keep the character at the same
visible scale and position. Keep the charging/attention/checked badges at matching
proportions, with centered content. Banner title, divider and description use
fixed shared columns, so longer labels do not move the divider.

## AI connection and settings

Preferences save independently and immediately; Done closes the screen. Preserve
the last committed value and expose save failures. Unknown parking disables
changes. Debugger is Debug-only and defaults off; Release omits it. Spoken replies
and Do Not Disturb remain unavailable in current implementation.

The supplied Settings export shows a vehicle-home placeholder and says the
character is static. It does not enable a launcher feature. Current code already
contains Mobi breathing frames, but the reduced-motion preference is not wired
to `PetAvatar.isAnimated`; this remains an implementation gap, not a completed
accessibility behavior. The existing unsupported Do Not Disturb row is not
removed merely because the export omits it.

### Copilot connection UI

Production currently exposes introduction/unavailable feedback. Keep the QR action
disabled until a verified provider exists. Companion observation failure shows
Retry and retains the last committed appearance without claiming it is current.

The eight approval/connection states are reusable presentation and a separate
Debug rehearsal. Example accounts, QR codes and timers are review data. Preserve
panel/header alignment, hide expired codes, offer address help and distinguish
account approval from Copilot readiness. Their technical boundary is in
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
controls authorization or commit success. Reduced motion must stop decorative
loops when wired through all renderers.

Current Android navigation/drawer transitions use 220ms; connection panels fade
in over 180ms and out over 120ms. Expiry/restriction changes replace content
immediately, and countdown updates keep panel identity. The v5 Figma prototype's
180ms dissolves and delayed success demonstrations are review transitions; the
static exports contain no animation metadata, and a demo delay is never a
purchase/reward completion signal.

## Vehicle launcher

There is one in-app Home. A launcher character is outside current support; its
dormant off-by-default preference cannot authorize display. A future launcher
requires a verified platform contract and an explicit UX/opt-in design.

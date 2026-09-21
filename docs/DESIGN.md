# Product design

MobiMon is a parked companion for vehicle information, conversation, quests and
personalization. [V5 exports](ui/README.md) define visual geometry;
[ARCHITECTURE.md](ARCHITECTURE.md) distinguishes implemented support from plans.

## Concept

The product loop is **quests → points → accessories → personalization**. Do not
restore legacy XP, levels, evolution or driving-score UI. Follow the
[parking/AAOS authorization contract](ARCHITECTURE.md#vehicle-interaction-authorization).
Moving/unknown state pauses interaction while preserving drafts and committed data.
Expressions supplement vehicle facts; they never diagnose a vehicle or replace warnings.

## Visual language

- Target the fixed 2560 × 1440px AAOS display. Other display resolutions,
  aspect ratios and arbitrary compact windows are outside the product scope.
  App content excludes system bars; AAOS compatibility density changes its dp
  dimensions. Preserve the target layout with those insets, enlarged text and
  the system keyboard's reduced content height.
- Use `MobiMonTheme`, Twilight colors and bundled Noto Sans KR. Follow export
  positions, typography, proportions and icons; use actual runtime insets.
- Reflow or scroll when enlarged text or the keyboard requires it. Controls are at least
  76 × 76dp, with 24dp spacing/edge clearance where possible. Maintain 4.5:1 text
  contrast and 3:1 control/icon contrast; pair status colors with labels or shapes.
- Reuse shared navigation controls and `MobiMonParkingBadge`. Keep points and
  simulation labels separate. Order focus by heading, information and actions;
  trap dialog focus and return it to the trigger on dismissal.

### Launcher icon and native splash

Use shared Mobi artwork on Night, without a wordmark, with adaptive-mask clearance.
The splash ends on the first app frame. These are app adaptations, not SVG references.

## Reusable Compose library and asset handoff

Use `core-ui` primitives before feature-local equivalents. Feature owners perform
[final visual acceptance](TESTING.md#final-figma-visual-acceptance).

Full-screen SVGs stay in `docs/ui`. Import original icons into the owning feature
with a feature prefix; keep shared fonts/artwork in `core-ui` and preserve licenses.
Do not package full-screen references or generation drafts as runtime UI.

### Image asset locations

| Asset | Location |
| --- | --- |
| Character frames | `core/core-ui/src/main/assets/characters/{mobi,luna}/idle_breath/` |
| Shared artwork, accessories and backgrounds | `core/core-ui/src/main/res/drawable-nodpi/` |
| Feature icons/artwork | Owning module's `res/drawable/` or `res/drawable-nodpi/` |
| References | [docs/ui](ui/README.md) |
| Generation drafts | Ignored `output/imagegen/` and `tmp/imagegen/` |

Use approved master assets for variants. Preserve identity, proportions, style,
scene geometry, canvas size, framing, subject scale/anchor and transparency; change
only requested properties. Check dimensions and compare visually before use.

### Home scene

Home and store preview share five 2560 × 1440 WebP backgrounds. Local hours select
Morning 06–11, Day 12–15, Afternoon 16–17, Sunset 18–19 and Night 20–05. Supplied
time overrides the clock. Home center-crops them with cool tint/daylight shadows;
artwork and tint crossfade for one second under the [motion rules](#motion).
Controls remain untinted.

Home follows [home.svg](ui/shell/home.svg). Its menu overlays the same scene.
The animated time phrase replaces the SVG subtitle. Availability notices must not
move the main action; enlarged-text layouts remain scrollable.

## Screens and navigation

The [export index](ui/README.md#screen-index) owns the screen inventory. Back closes
the keyboard, then dialog/menu, then destination. Menu destinations return Home;
connection returns to its entry route. Purchase cancellation returns to its preview.
Explicit Home always opens Home.

The menu closes through its close control, backdrop, Back or destination selection.
It shows the equipped friend and app version, with its footer above the system bar.
Preserve the SVG layout at AAOS compatibility density, centering minimum touch
bounds around each row. Reflow only when those bounds overlap or enlarged text
needs more room.

Unavailable services explain the limitation and recovery. Pending actions block
duplicates; uncertain writes offer reconciliation before retry.

## Quests and points

Show catalog values and actual repeat eligibility, never SVG sample rewards or a
universal daily reset. Celebrate only a committed award and its returned amount.
An unknown wallet is not zero.
Use the same committed balance throughout the app, labelled Points or `1,200 P`.
Repeated claims reconcile without another celebration. Later repository updates,
including resets, replace temporary claim confirmations.

## Customization

Friends, accessories and backgrounds are independent. Preserve equipment per friend.
Preview stays local until Apply commits; purchase confirms ownership only. Show
compatibility, price and wallet balance before purchase; cancellation spends nothing.

| State | Action |
| --- | --- |
| Unowned | Preview and purchase with explicit price |
| Insufficient points | Show shortfall and offer quests |
| Owned | Apply without another purchase |
| Applied | Mark active; no redundant action |
| Pending/unconfirmed | Block duplicates and reconcile or retry while preserving selection |

No cash purchases, top-ups or conversion.

## Conversation

Use the V5 split panels, empty state, suggestions and composer. Home/menu Chat opens
connection settings when signed out and chat when authenticated; the Settings account
card always opens connection management. Follow the [session and provider contract](ARCHITECTURE.md#keyboard-conversation-ui).
Enable Send for a valid draft when authenticated and parked; show readiness only after
a successful reply. Do not add a consent panel, connection-check button or entry preflight.
Suggestions fill the draft without sending; preserve selection and unfinished IME input.

Use the system keyboard; [keyboard-input.svg](ui/conversation/keyboard-input.svg)
defines the resized app layout. Keep header scale and the composer above the IME,
shorten the panels and shrink the companion. Enlarged-text layouts prioritize
chat and hide secondary content. Preserve visible control geometry while meeting
minimum touch bounds at AAOS density.

Identify speakers and preserve drafts/replies on recoverable errors. Parking loss
disables editing and hides the IME; AAOS restrictions remove the screen. Show specific
recovery for network, service/Auto availability, timeout, account, access, usage and
length errors. Retries are explicit. Put New conversation beside the follow-up
suggestion, retaining the reference header and message geometry.

Hide unsupported voice controls. Future voice input requires permission, transcript
review and explicit Send; stopping never submits.
Playback yields to calls/navigation; restrictions stop playback and recording.

Use the existing footer to name GitHub Copilot, disclose dialogue/companion-name
transmission and warn about AI accuracy. Provider retention/training policies and
account limits apply; local clearing does not promise provider deletion.

## Vehicle information

| Observed condition | Presentation |
| --- | --- |
| Low battery / charging required | Hungry expression, actual charge and charging guidance |
| Vehicle issue | Sick expression, affected item and specific warning |
| Current checked items without warnings | Default expression; describe only checked items as normal |
| Missing/stale data | Unavailable, with last update; never imply normality |

Specific warnings take priority; partial data stays partial. See
[current support](ARCHITECTURE.md#current-foundation) before using condition expressions.

## AI connection and settings

Settings expose save failures. Debug controls are Debug-only and off by default.
Spoken replies and vehicle-home display remain unavailable; Do Not Disturb is omitted.

### Copilot connection UI

Configured builds show GitHub's approval URL as a QR with a separate user code and
address help. Update approval status automatically and hide expired codes. Apply the
[authentication lifecycle rules](ARCHITECTURE.md#copilot-connection-ui).

Authentication success uses [connected.svg](ui/connection/connected.svg) geometry,
with “모비와 대화하기” (using the equipped friend's name) opening chat and Settings
secondary. Show the verified account, local persistence and a note that Copilot access
is checked on Send. Successful restoration replaces prior sign-in errors.
Loading/failures offer retry or local clearing; companion failures retain appearance
with Retry.

Account-row Disconnect opens confirmation and explains that only the local connection
is removed; preserve points/cosmetics. Offer approval again for revoked/expired
credentials. Unconfigured builds disable sign-in.

Loading and failure states have no v5 export; reuse panel typography and controls.

## Motion

Motion preserves context and focus; outgoing/restricted controls lose input
immediately. Animation never authorizes or commits a command. Reduced motion shows
settled states and pauses character/particle loops. Unknown or failed motion
preference reads keep decoration static.

Home's conversation action reveals the destination from the activated button's
rounded bounds over 300ms, with Home stationary underneath. Back closes it toward
the same bounds over 220ms. Sample the button after scrolling and respect runtime
insets. Destination touch targets follow the visible reveal bounds.

The Home speech bubble enters from its tail on entry, pet tap and periodic
reappearance; data updates do not replay it. Other shell/drawer transitions use
220ms. Connection panels fade in over 180ms and out over 120ms; expiry/restrictions
replace content immediately, and countdown ticks keep panel identity. Prototype
delays are not success signals.

## Vehicle launcher

Future launcher placement requires explicit opt-in and the
[platform contract](ARCHITECTURE.md#shared-vehicle-condition-and-overlay).

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
  the system keyboard's reduced content height. Use the current
  [export content bounds](ui/README.md), including the larger system bars.
- Fit panel surfaces and bottom action groups to the available height. Reflow
  Menu, Settings, store hints/actions and conversation input together; keep
  footers, retry controls and pending indicators clear of adjacent controls and
  system UI. Preserve font sizes and artwork proportions instead of scaling the
  whole screen to fit.
- Use `MobiMonTheme`, Twilight colors and bundled Noto Sans KR. Follow export
  positions, typography, proportions and icons; use actual runtime insets.
- Reflow or scroll when enlarged text or the keyboard requires it. Controls are at least
  76 × 76dp, with 24dp spacing/edge clearance where possible. Maintain 4.5:1 text
  contrast and 3:1 control/icon contrast; pair status colors with labels or shapes.
- Reuse shared navigation controls and `MobiMonParkingBadge` outside Conversation;
  Conversation uses its export-sized parking capsule. Keep points and
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
| Mobi idle sprite / Luna frames | `core/core-ui/src/main/assets/characters/{mobi,luna}/idle_breath/` |
| Original Mobi warning artwork | `art/characters/mobi/unhealthy/` (not packaged) |
| Shared artwork, accessories and backgrounds | `core/core-ui/src/main/res/drawable-nodpi/` |
| Feature icons/artwork | Owning module's `res/drawable/` or `res/drawable-nodpi/` |
| References | [docs/ui](ui/README.md) |
| Generation drafts | Ignored `output/imagegen/` and `tmp/imagegen/` |

Use approved master assets for variants. Preserve identity, proportions, style,
scene geometry, canvas size, framing, subject scale/anchor and transparency; change
only requested properties. Check dimensions and compare visually before use.

Mobi idle uses a lossless 6 x 4 atlas of all 24 original 1254px RGBA frames.
The atlas retains every source frame pixel; separate frame PNGs are not kept.
Forward playback already contains inhale/exhale; 24-to-01 is visually identical.
The 4.05-second cycle includes the source's repeated extreme poses, with
an independent 6.2-second, +/-2.35-degree seated-pivot tilt. Eyes and sprout are baked
in; there are no synthetic blink/sprout layers. Original source discontinuities
remain strongest at 08-to-09, 13-to-14 and 14-to-15; alpha-correct adjacent-cell blending softens these jumps but cannot repair artwork.
Breathing adds up to 1.2% width/2.4% height; a separate 6.6-second bob adds tiny
settle and lift. All transforms share the seated pivot and preserve layout.

Mobi's collapsed idle uses a lossless 24-frame sprite atlas `mobi_collapsed_sprite.png` (6 x 4 grid of 408px RGBA cells) under `core/core-ui/src/main/assets/characters/mobi/unhealthy/`.
Original collapsed and transition artwork remains under `art/characters/mobi/unhealthy/` for future edits; it is not packaged.
The 200ms normal/warning crossfade is unchanged. The 24-frame animation loop plays continuously over 4.05 seconds with smooth frame interpolation, capturing shivering, sweating, eye movements, and dizziness.
Reduced motion snaps to frame 0 and disables frame cycling.
Equipped Mobi uses base collapsed artwork during warnings and restores its equipped normal look afterward.

Use the replaceable [PetAvatar](ARCHITECTURE.md#state-and-lifecycle) renderer.

### Home scene

Home and store preview share five 2560 × 1440 WebP backgrounds. Local hours select
Morning 06–11, Day 12–15, Afternoon 16–17, Sunset 18–19 and Night 20–05. Supplied
time overrides the clock. Home keeps the SVG artwork framing: a centered crop in
the original 2560 × 1268 rectangle at y=76, clipped by the current safe content
starting at design y=96. Changing available height does not recenter the horizon.
Store previews crop within their own cards. Home adds cool tint/daylight shadows;
artwork and tint crossfade for one second.
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
Enable Send for a valid draft when Copilot access and `gpt-4o` are verified and the vehicle is parked; show readiness after
a successful model check. Do not add a consent panel, connection-check button or entry preflight.
Suggestions fill the draft without sending; preserve selection and unfinished IME input.
At 2560 × 1440, place the companion at x72–752, chat at x796–2488, and keep
both panels 24px above the available content bottom. Use 244 × 60px authentication
and 258 × 60px parking capsules in the chat header. The empty state has three
70px suggestions; the 1577 × 87px composer stays above the footer and keyboard.

Use the system keyboard; [keyboard-input.svg](ui/conversation/keyboard-input.svg)
defines the resized app layout. Keep header scale and the composer above the IME,
shorten the panels and shrink the companion. Enlarged-text layouts prioritize
chat and hide secondary content. Preserve visible control geometry while meeting
minimum touch bounds at AAOS density.

Identify speakers and preserve drafts/replies on recoverable errors. Unverified parking
shows the [parking dialog](ui/conversation/parking-required.svg) over chat, disables
editing and hides the IME. Home and Back return home without clearing the draft; verified
parking removes the dialog. AAOS restrictions remove the screen. Show specific
recovery for network, service, timeout, account, access, usage and
length errors. Retries are explicit. Put New conversation beside the follow-up
suggestion, retaining the reference header and message geometry. Show failed
replies with inline retry and edit actions while keeping history visible.

On foreground entry, restore the GitHub session and check Copilot access/model when
Park and AAOS allow interaction. Home remains available during the check. Chat shows
“Copilot 확인 중” with Send disabled until verification succeeds. A network failure
during connection checks shows the [network dialog](ui/conversation/network-error.svg)
over chat. Other connection failures explain the cause and offer recheck or connection
management. “다시 확인” checks access/model without sending the draft and displays the
[checking dialog](ui/conversation/network-checking.svg) until it finishes. A failed
message stays inline with its attempted turn and offers edit or explicit retry;
retry may consume usage. An access failure requires recheck before Send. Home and
Back preserve the draft. The modal stays on chat and does not interrupt Home.

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
immediately. Animation never authorizes or commits a command. The Settings motion
switch stops the floating companion's autonomous movement on vehicle Home; dragging
and idle breathing remain. It does not change in-app scene or navigation motion.
Unknown or failed preference reads keep the floating companion stationary.

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

The floating companion can be shown outside the app with explicit opt-in and
overlay permission. Its intended location is vehicle Home; verify placement and
lifecycle on the target vehicle under the
[platform contract](ARCHITECTURE.md#shared-vehicle-condition-and-overlay).

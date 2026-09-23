# Product design

MobiMon is a parked companion for vehicle information, conversation, quests and
personalization. [V5 exports](ui/README.md) define visual geometry;
[ARCHITECTURE.md](ARCHITECTURE.md) distinguishes implemented support from plans.

## Concept

The product loop is **quests → points → accessories → personalization**. Do not
restore legacy XP, levels, evolution or driving-score UI. Interaction requires
verified parking and AAOS allowance. Moving/unknown state pauses interaction while
preserving drafts and committed data. Expressions supplement vehicle facts; they
never diagnose a vehicle or replace warnings.

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

Use `core-ui` primitives before feature-local equivalents. Screens receive state
and callbacks; shared components stay stateless. Feature owners perform
[final visual acceptance](TESTING.md#final-figma-visual-acceptance).

Full-screen SVGs stay in `docs/ui`. Import original icons into the owning feature
with a feature prefix; keep shared fonts/artwork in `core-ui` and preserve licenses.
Do not package full-screen references or generation drafts as runtime UI.

### Image asset locations

| Asset | Location |
| --- | --- |
| Mobi idle sprite / Luna frames | `core/core-ui/src/main/assets/characters/{mobi,luna}/idle_breath/` |
| Original Mobi source frames | `docs/art/characters/mobi/idle_breath/` (not packaged) |
| Shared artwork, accessories and backgrounds | `core/core-ui/src/main/res/drawable-nodpi/` |
| Feature icons/artwork | Owning module's `res/drawable/` or `res/drawable-nodpi/` |
| References | [docs/ui](ui/README.md) |
| Generation drafts | Ignored `output/imagegen/` and `tmp/imagegen/` |

Use approved master assets for variants. Preserve identity, proportions, style,
scene geometry, canvas size, framing, subject scale/anchor and transparency; change
only requested properties. Check dimensions and compare visually before use.

Mobi idle uses a lossless 6 x 4 atlas of all 24 original 1254px RGBA frames.
Rebuild with `python scripts/build_mobi_idle_sprite.py` (Pillow required).
Forward playback already contains inhale/exhale; 24-to-01 is visually identical.
The 4.05-second cycle includes the source's repeated extreme poses, with
an independent 6.2-second, +/-2.35-degree seated-pivot tilt. Eyes and sprout are baked
in; there are no synthetic blink/sprout layers. Original source discontinuities
remain strongest at 08-to-09, 13-to-14 and 14-to-15; alpha-correct adjacent-cell blending softens these jumps but cannot repair artwork.
Breathing adds up to 1.2% width/2.4% height; a separate 6.6-second bob adds tiny
settle and lift. All transforms share the seated pivot and preserve layout.

Mobi's collapsed idle uses two 408px RGBA images under `core/core-ui/src/main/assets/characters/mobi/unhealthy/`:
`mobi_collapsed_closed.png` and `mobi_collapsed_tired_eyes.png`. Both use the same canonical lying pose.
The eye variant differs only inside the existing dark eyelids; skin, bandage, silhouette and body pixels are identical.
`scripts/build_mobi_unhealthy_sprites.py` builds them from the accepted source and one local eye edit.
The 200ms normal/warning crossfade is unchanged. Collapsed breathing deforms only the side torso inside the
fixed sprite canvas over four seconds (maximum 1.4 native pixels). Ground contact, wheel, face and sprout remain fixed.
A slow slight eye opening/closing occurs every 6.5 seconds. Neither effect translates, rotates or scales the whole sprite.
Reduced motion snaps to closed-eye endpoints and disables breathing/blinking. Original native body detail remains 256px;
transparent padding adds no detail. Older independently drawn idle/transition sheets remain archived, never loaded.
Equipped Mobi uses base collapsed artwork during warnings and restores its equipped normal look afterward.

Use the replaceable [PetAvatar](ARCHITECTURE.md#state-and-lifecycle) renderer.

### Home scene

Home and store preview share five 2560 × 1440 WebP backgrounds. Local hours select
Morning 06–11, Day 12–15, Afternoon 16–17, Sunset 18–19 and Night 20–05. Supplied
time overrides the clock. Home center-crops them with cool tint/daylight shadows;
artwork and tint crossfade for one second unless reduced motion is enabled.
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
universal daily reset. Success requires a committed award and its returned amount;
reopening or replaying the result cannot award again. An unknown wallet is not zero.
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

Keyboard chat uses the V5 split panels. Home/menu Chat opens connection settings
when signed out and chat when authenticated. The Settings account card always opens
connection management. Until a reply provider exists, explain the limitation and
disable Send. Suggestions fill the draft without sending; preserve selection and
unfinished IME input. Blank input cannot be sent.

Use the system keyboard; [keyboard-input.svg](ui/conversation/keyboard-input.svg)
defines the resized app layout. Keep header scale and the composer above the IME,
shorten the panels and shrink the companion. Enlarged-text layouts prioritize
chat and hide secondary content. Preserve visible control geometry while meeting
minimum touch bounds at AAOS density.

With a verified provider, identify speakers, block duplicate sends and preserve
drafts/replies on recoverable errors. Leaving cancels pending work; completed exchanges
last for the session. Voice controls remain hidden until supported. Future voice input
requires permission, transcript review and explicit Send; stopping never submits.
Playback yields to calls/navigation; restrictions stop playback and recording.

Explain transmitted data and provider retention before enabling conversation.
Disconnect/session clearing does not promise provider deletion. Long-term memory
is outside scope.

## Vehicle information

| Observed condition | Presentation |
| --- | --- |
| Low battery / charging required | Hungry expression, actual charge and charging guidance |
| Vehicle issue | Sick expression, affected item and specific warning |
| Current checked items without warnings | Default expression; describe only checked items as normal |
| Missing/stale data | Unavailable, with last update; never imply normality |

Specific warnings take priority. Partial data stays partial. Condition expressions
are a target, not completed renderer behavior; see architecture for support.

## AI connection and settings

Settings save independently and expose save failures. Unknown parking disables
changes. Debug controls are Debug-only and off by default. Spoken replies and
vehicle-home display remain unavailable; Do Not Disturb is omitted.

### Copilot connection UI

Configured builds show GitHub's approval URL as a QR with a separate user code and
address help. Update approval status automatically and hide expired codes. Apply the
[authentication lifecycle rules](ARCHITECTURE.md#copilot-connection-ui).

Authentication success uses [connected.svg](ui/connection/connected.svg) geometry,
with “모비와 대화하기” (the equipped friend's name) opening keyboard chat and Settings
secondary. Show the verified account and local persistence while stating that Copilot
replies remain unavailable; do not claim full `Connected` readiness. Successful
restoration replaces prior sign-in errors. Loading/failures offer retry or local
clearing; companion read failures retain appearance with Retry.

Account-row Disconnect opens confirmation and explains that only the local connection
is removed; preserve points/cosmetics. Offer approval again for revoked/expired
credentials. Unconfigured builds disable sign-in. Debug examples never verify a provider.

Loading and failure states have no v5 export; reuse panel typography and controls.

## Motion

Motion preserves context and focus; outgoing/restricted controls lose input
immediately. Animation never authorizes or commits a command. Reduced motion shows
settled states, keeps the idle breath in place and pauses running, wandering and
particle loops. Unknown or failed motion preference reads keep decoration static.

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

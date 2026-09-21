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

- Use `MobiMonTheme`, Twilight colors and bundled Noto Sans KR. Follow export
  positions, typography, proportions and icons; use actual runtime insets.
- Reflow or scroll for compact windows and enlarged text. Controls are at least
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
| Character frames | `core/core-ui/src/main/assets/characters/{mobi,luna}/idle_breath/` |
| Shared artwork, accessories and backgrounds | `core/core-ui/src/main/res/drawable-nodpi/` |
| Feature icons/artwork | Owning module's `res/drawable/` or `res/drawable-nodpi/` |
| References | [docs/ui](ui/README.md) |
| Generation drafts | Ignored `output/imagegen/` and `tmp/imagegen/` |

Use approved master assets for variants. Preserve identity, proportions, style,
scene geometry, canvas size, framing, subject scale/anchor and transparency; change
only requested properties. Check dimensions and compare visually before use.
`PetAvatar` is the replaceable renderer, with no rewards or authorization logic.

Home and store preview share five 2560 × 1440 WebP backgrounds. Local hours select
Morning 06–11, Day 12–15, Afternoon 16–17, Sunset 18–19 and Night 20–05. Supplied
time overrides the clock. Home center-crops them with cool tint/daylight shadows;
artwork and tint crossfade for one second unless reduced motion is enabled.
Controls remain untinted.

Home follows [home.svg](ui/shell/home.svg). Its menu overlays the same scene.
The speech bubble enters from its tail on entry, pet tap and periodic reappearance;
data updates do not replay it. The animated time phrase replaces the SVG subtitle.
Availability notices must not move the main action; compact layouts remain scrollable.

## Screens and navigation

The [export index](ui/README.md#screen-index) owns the screen inventory. Back closes
the keyboard, then dialog/menu, then destination. Menu destinations return Home;
connection preserves its Home/Settings origin. Purchase cancellation returns to
its preview. Explicit Home always opens Home.

Menu content respects system insets and shows the equipped friend. Unknown services
explain availability and recovery. Pending actions block duplicates; uncertain writes
offer reconciliation before retry.

## Quests and points

Show catalog values and actual repeat eligibility, never SVG sample rewards or a
universal daily reset. Success requires a committed award and its returned amount;
reopening or replaying the result cannot award again. An unknown wallet is not zero.
Use the same committed balance throughout the app, labelled Points or `1,200 P`.

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

No cash purchases, top-ups or conversion. The export index records the missing
standalone unowned-item preview reference.

## Conversation

Conversation remains planned. Voice follows **permission → listening → transcript
review → send → wait → reply**. Stopping recording never submits. Offer text input,
keep the composer above the keyboard and preserve unfinished IME input.

Identify speakers, block duplicate sends and preserve drafts/replies on recoverable
errors. Leaving cancels pending work; completed exchanges survive navigation within
the session, but a new session/restart clears them. Playback yields to calls/navigation;
restrictions stop playback and recording.

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

Reduced motion pauses character/particle loops and shell motion. Unknown or failed
preference reads keep decoration static.

### Copilot connection UI

Configured builds show GitHub's approval URL as a QR with a separate user code and
address help. Hide expired codes; poll automatically and keep manual rechecks within
the provider interval. Leaving/backgrounding or losing parking cancels pending approval.

Authentication success shows the verified account and local session persistence,
while explicitly stating that Copilot conversation remains unavailable. It must not
render fully `Connected`. Loading/failures offer appropriate retry or local clearing;
companion read failures retain appearance with Retry.

Disconnect confirms local removal, preserves points/cosmetics and does not revoke
the GitHub grant or subscription. Revoked/expired credentials may need approval again.
Unconfigured builds disable sign-in. Debug example states never verify a provider.

Authentication-only states have no v5 export; reuse panel typography, colors and
controls, and review both reference-size and enlarged-text layouts. Technical
contracts belong in [ARCHITECTURE.md](ARCHITECTURE.md#copilot-connection-ui).

## Motion

Motion preserves context and focus; outgoing/restricted controls lose input
immediately. Animation never authorizes or commits a command. Reduced motion shows
settled states. Current shell/drawer transitions use 220ms; connection panels fade
in over 180ms and out over 120ms. Expiry/restrictions replace content immediately,
and countdown ticks keep panel identity. Prototype delays are not success signals.

## Vehicle launcher

Only the in-app Home exists. The dormant launcher preference cannot authorize a
surface; future support needs the platform contract in architecture and explicit opt-in.

# Product design

MobiMon is a calm in-car companion for time spent parked. It combines a friendly
character, understandable vehicle information, conversation and personal
expression. The v4 [Figma design](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=231-1176)
is the visual reference; this document defines the product experience.
Technical contracts belong in [ARCHITECTURE.md](ARCHITECTURE.md), and verification
belongs in [TESTING.md](TESTING.md).

## Concept

The core loop is **complete quests → earn points → buy accessories → personalize
your companion**. Points are spendable rewards, not experience. There are no
growth stages, levels, evolution or driving scores. XP/LEVEL/growth labels still
visible in Figma are obsolete annotations, not implementation requirements.
Historical XP records remain a storage compatibility concern; do not convert
them into points or infer new reward amounts from those examples.

Users choose Mobi the dog or Luna the cat. Their choice expresses personality
and does not change quest rewards. The companion's expression reflects available
vehicle information, while clear text explains the actual condition. The
character provides warmth without replacing vehicle warnings or implying a
diagnosis.

Interaction is available while parking is verified and the vehicle allows app
use. Moving or unknown parking status pauses interaction and hides the launcher
character. Existing purchases, rewards, preferences and drafts remain intact.

## Visual language

- Use the v4 Twilight palette: Night backgrounds, Surface panels, Cream primary
  actions, Sky selection/focus, Mint success and rounded panels. Alternate backgrounds keep
  the same meanings for selection, warning and action colors.
- Use one fixed in-app vehicle-display palette through the shared MobiMonTheme;
  system light/dark preferences do not switch the app palette.
- Give the character visual priority on the home screen. Keep the menu and brand
  at the upper left, and align point balance and parking status at the upper
  right. The Home surface does not render Vehicle or Quest artwork shortcuts;
  those destinations remain owned by the app menu. Keep the conversation action
  below the character. Battery details belong on Vehicle status, not in a
  second Home panel.
- Information screens pair the character with a clear content area.
  Customization pairs a large preview with the selection list. Reflow or scroll
  when space is limited instead of shrinking every element.
- Treat the 2560 × 1440 landscape artboard as a composition reference. Respect
  system bars, cutouts and the available window; do not require fixed pixels or
  a single aspect ratio.
- The v4 artboards reserve 76/96 units for the drawn OS bars. Compare the
  2560:1268 app content area, respecting actual system insets without drawing a
  second set of bars. Use bundled Noto Sans KR Regular/Bold; reflow or scroll
  compact and enlarged-text windows. The [font license](../core/core-ui/src/main/assets/fonts/OFL-NotoSansKR.txt)
  ships with the app.
- Use at least 76 × 76 dp touch targets for primary controls, with 24 dp spacing
  and edge clearance where possible. Start with 32 sp main text and 24 sp
  secondary text, retaining readability with enlarged fonts. These are project
  design targets informed by the [vehicle visual principles](https://developers.google.com/cars/design/design-foundations/visual-principles).
- Maintain at least 4.5:1 text contrast and 3:1 contrast for meaningful icons,
  boundaries and selection indicators against their actual backgrounds.
  Combine color with text, shape or an icon for status.
- Make settings rows easy to select, with one toggle per action. Keep icons
  consistent. Screen-reader and rotary focus follows title, information, primary
  action and secondary actions. Dialogs contain focus and return it to their
  trigger when closed.
- Keep motion brief and quiet. The reduced-motion preference limits repeated
  character movement when character animation is introduced; the current
  character artwork is static. A successful connection may trigger one silent,
  character-appropriate greeting; it must not delay the next action.

## Reusable Compose library and asset handoff

The stateless library lives in [core-ui](../core/core-ui/src/main/java/com/monsters/mobimon/core/ui).
Use these components before adding a feature-local equivalent:

| API | Use |
| --- | --- |
| `MobiMonTheme`, `MobiMonColors`, `MobiMonTwilightColors` | Bundled typography and semantic v4 Night/Surface/Cream/Sky/Mint roles |
| `MobiMonDimensions` | Shared 76dp target, content spacing and panel/message corners |
| `MobiMonDestination` | Back/Home header and a slot for the full-content feature body |
| `MobiMonContentColumn`, `MobiMonSection` | Scrollable content and titled groups |
| `MobiMonButton`, `MobiMonButtonStyle` | Primary, secondary and destructive actions; explicit enabled state, visible focus and 76dp minimum targets |
| `MobiMonPanel`, `MobiMonListItem` | Information panels and aligned leading/headline/supporting/trailing slots; compact rows reflow |
| `MobiMonTabs`, `MobiMonTab`, `MobiMonSelectionCard` | Wrapping categories and selectable previews; caller-owned selection, enabled state and callbacks |
| `MobiMonStatusBadge` | Labelled informational, success, warning and error states without granting authorization |
| `MobiMonMessage`, `MobiMonSourceBadge` | Accessible feedback and explicit simulated/real labels |
| `MobiMonPointSummary` | Loading, failed, zero and committed balances |
| `PetAvatar`, `CompanionIcon` | Replaceable character artwork and shared icon rendering |

Each component and palette family has its own file. Production Quest, Vehicle,
Companion and AI routes use these APIs. `ComponentGallery` provides normal and
enlarged-text previews and a Debug-only **MobiMon UI Catalog · Demo** launcher;
it has no repository or provider. AI's rehearsal implementation, sample strings,
QR and manifest now live entirely in `feature-auth/src/debug`.

`MobiMonTheme` uses v4 by default across production, Debug and previews. There
is no separate Home/Settings palette or older-design opt-in. Shared components
accept display values, slots and callbacks, never repositories or ViewModels.
Selection is separate from purchase/equip/claim actions; pending and disabled
states are caller-owned. Use native controls and labels for accessibility and
keep 76dp targets even where an artboard's scaled pixels would be smaller.

Build shared primitives before parallel screen work. Feature owners compose
them into final layouts and own feature-specific dialogs, cards and state.
Keep fixed artboard coordinates inside a feature when needed for its reference
layout; do not expand a shared API with unrelated screen-specific flags.

Import original Figma exports into the owning feature's resources, with a
feature-specific prefix. Shared fonts and character artwork belong in `core-ui`;
keep original raster bytes in `drawable-nodpi` and font licenses alongside fonts.
Record the node link in the relevant design section when adopting an asset.
Use Figma **Copy/Paste as SVG** for exact geometry, then retain the original
vector paths during Android conversion. Do not commit temporary MCP asset URLs,
whole-screen SVGs as UI, invented icon replacements, or duplicated shared artwork.
Remove superseded assets only after checking all variants, previews and tests.

Mobi uses the original v4 PNG through `PetAvatar`, including when a temporary
accessory overlay is equipped; the retired drawn dog is not a Mobi fallback.
Customization and Home use a Luna cutout derived from the supplied 04 SVG.
The equipped Mobi/Luna looks use isolated transparent character resources,
not source-sheet crops; the historical Cream fallback remains temporary.
Cream is not an additional product appearance choice.
Artwork replacement must not change rewards, ownership or equipment.

The shared library is implemented independently of full-screen migration.
Home, Menu, Settings and Shop currently combine these primitives with existing
behavior; their final v4 compositions remain feature work. Conversation and
expanded Vehicle/Quest layouts are also pending. Do not require all feature
screens to be finished in a shared-component integration change. Each owner
must perform [final visual acceptance](TESTING.md#final-figma-visual-acceptance)
for their completed screens; unavailable exports and differences remain explicit.

## V4 reference ownership

Only the current v4 page is a visual target; earlier pages and superseded
coordinate specifications must not guide new implementation. This table links
the authoritative groups without duplicating each frame's geometry here.

| Area | V4 reference | Implementation boundary |
| --- | --- | --- |
| Shared rules and assets | [UI rules](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=243-6341), [artwork](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=231-4243) | `core-ui` tokens and reusable primitives |
| Home, Menu, Settings | [01 group](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=255-9686) | Companion UI and app-owned menu navigation |
| Connection | [02 group](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=255-9688) | AI presentation; real adapter remains separate |
| Conversation | [03 group](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=255-9690) | AI feature |
| Customization and Shop | [04 group](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=255-9692) | Companion feature, committed points/ownership |
| Vehicle | [05 group](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=255-9694) | Vehicle feature; unavailable values stay unavailable |
| Quest | [06 group](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=255-9696) | Quest feature; exclude obsolete XP/progression and reconcile retained flows with points rules |

## Screens and navigation

| Screen | Purpose and primary content |
| --- | --- |
| Home | Selected companion and background, upper-right parking status and point balance, and conversation entry. Vehicle, Quest and customization navigation live in the app menu. |
| Menu | V4 target: companion panel and entries for conversation, customization, Vehicle, Quest, Settings and Home. |
| Vehicle status | Available readings, specific warnings, freshness and connection status. |
| Quests | Conditions, reward type, point reward, progress and the next available action. |
| Customization | Friends, Outfits and accessories, and Backgrounds; preview, ownership and application states. |
| Copilot connection | Account connection guidance, approval/help, expiry, readiness, reconnect and disconnect states; see the [current UI scope](#copilot-connection-ui). |
| Conversation | Clearly identified speakers, conversation history, voice input and a text composer. |
| Settings | AI connection, spoken replies, reduced motion and Do Not Disturb. |

The P02 menu is a left-side overlay, opened by the Home menu button and
dismissed by its close control, backdrop, or Back. It exposes Home,
Conversation, Quest, Vehicle, Customization and Settings in that order.
The profile portrait and name follow the equipped friend identifier, with
portrait resources kept outside Compose UI code. Selecting an entry closes
the menu and opens its destination. Conversation currently routes to the
Copilot connection/introduction screen while its full chat UI is pending.

Back closes the keyboard first, then an open dialog or menu, then returns to the
previous screen. Menu destinations return to home; settings subpages return to
settings. A purchase dialog returns to the same item preview. An explicit
"Return home" action always opens home.

The app has one Home surface. It does not expose an in-app vehicle-launcher
preview or a setting that changes companion visibility on another Home surface.
The Home tagline does not carry a Simulation badge. Signal provenance remains
explicit on Vehicle and Quest information where it qualifies displayed data.

## Quests and points

| Reward type | Eligibility and completion display |
| --- | --- |
| One-time | Awarded once per profile and quest. Keep the completed quest visible without offering another reward. |
| Repeatable | Awarded once per defined occurrence. Show the repeat period and the next eligible time after completion. |

Each quest defines its condition, reward and, when repeatable, its schedule.
There is no universal repeat period. Use "Completed today" only for daily quests;
otherwise describe the completed occurrence.

A quest card shows a title, concise condition, reward type or schedule, point
reward, status and one next action. Distinguish available, in progress, saving
reward, completed, interrupted and failed states. Provide useful empty states
for a new profile or when no quests are currently available.

Show a reward as received only after it is saved. Reopening a result or replaying
a celebration does not award it again. A failed save offers retry without
presenting success.

Show the same point balance on home, quests and customization. Use a clear
"Points" label or a value such as "1,200 P"; distinguish it from parking status.
"Earned today" is the total earned that day, not the current balance, and does
not decrease after a purchase. A loading or failed balance is not displayed as
zero.

## Customization

Friends, outfits/accessories and backgrounds are independent choices. Switching
between Mobi and Luna preserves points and owned items. Use the selected
companion's name and artwork consistently throughout the app.

Customization uses the local [P20](ui/store/P20_friend_selection.svg) and
[P22](ui/store/P22_store.svg) exports: the 2560:1268 content frame, 916-unit preview,
1448-unit catalog, category pills and bottom apply action. The header uses a larger,
vertically centered title and committed point balance without parking or a subtitle.
All three categories expose selected semantics and a sky-colored active pill.
Compact/enlarged-text windows retain the scrollable catalog and the selected category.
Reference icons retain the SVG paths. Catalog data comes from the repository; sample
SVG products/prices do not become purchasable products. Background/outfit IDs reach
all companion renderers, but have no new product artwork or catalog entries.

The customization preview uses external Mobi and Luna character PNG resources.
Its accessory catalog shows the selected friend's own items: Mobi headphones
and goggles, or Luna cap and sunglasses. Each friend's equipped accessory is
retained when switching friends. Background art is a separate layer behind the
character; the current gradient is a placeholder until approved background
resources are mapped to background item IDs.

Selecting an item changes the preview. A separate, specific action applies it.
Distinguish **previewing**, **owned** and **currently applied** states. Show
accessories on the selected companion before purchase and explain any
compatibility restriction before the user spends points.

| Item state | Available action |
| --- | --- |
| Currently applied | Identify it as active; no redundant purchase or application. |
| Owned, not applied | Apply the item without a price or purchase action. |
| Not owned | Preview it and show a purchase action with its point price. |
| Insufficient points | Show the shortfall and a route to quests; disable purchase. |
| Processing | Show progress and prevent duplicate actions. |
| Failed | Preserve the preview and previously saved selection; offer retry. |

Purchase confirmation shows the item, price, current balance and resulting
balance. Cancel returns to the preview without spending points. Closing the
screen after a completed purchase does not undo it.

Purchasing grants ownership; it does not automatically equip the item. The
success state offers an explicit application action. Vehicle condition changes
do not replace the selected background or accessories. There are no cash
purchases, point top-ups or currency conversion.

## Conversation

Support voice and text while interaction is available. Identify both speakers
clearly, and avoid decorative speech bubbles that look like additional replies.

Voice follows **permission → listening → transcript review → send → waiting →
reply**. Finishing speech ends recording; the user can correct or cancel the
transcript before sending. A canceled recording is not sent. Offer text input
when the microphone is unsupported, denied or cannot recognize speech.

Use the system keyboard. Keep the composer and send action above it, preserve
text and selection when it closes, and do not submit unfinished text composition.
As available height decreases, reduce the character to a small identity marker
and prioritize the latest messages and composer. Restore the layout when space
returns.

Prevent duplicate sends while waiting. Distinguish network failure, timeout,
service failure, usage limits and authentication problems. Preserve the draft
and last completed exchange, with an explicit retry action. Closing conversation
cancels pending work; reopening it within the session keeps completed exchanges.
Navigation retains the session, while a new session or app restart clears it.

Spoken replies are independent of microphone permission and voice input. Provide
a stop action and yield to calls and navigation audio. Playback and recording
stop when interaction becomes restricted.

Before AI connection, explain what conversation and vehicle information is sent
and what is retained. MobiMon's session clearing and account disconnection do not
promise deletion by the external AI provider. Long-term personal memory is not
part of the conversation experience.

## Vehicle information

Show only information the vehicle actually provides. A valid battery reading
does not establish that every vehicle system is healthy. Pair an overall summary
with the checked items and their freshness.

| Condition | Presentation |
| --- | --- |
| Current readings without warnings | State that no issues were found in the checked items. |
| Low battery | Show the actual charge and applicable warning; a hungry expression is secondary. |
| Vehicle warning | Prioritize the affected item, location, severity and next action. Use "Needs attention" with the specific warning. |
| Checking | Show progress only while a check is active, then a result or actionable failure. |
| Unsupported, permission denied or disconnected | Explain the specific cause and offer only relevant recovery actions. |
| Stale information | Show when it was last checked and that the current condition is unknown. |
| Partial information | Mark the affected item unavailable without discarding other valid readings. |
| Interaction restricted | Explain the restriction, preserve user state and provide a safe return when use becomes available. |

Friendly language must not understate or exaggerate a warning. Never show an
unavailable reading as zero or an unchecked item as normal.

## AI connection and settings

Settings uses shared information rows and action/status components. Its
supported behavior below remains independent of final screen composition.
The fixed-coordinate specification for the retired design is not retained.

Settings exposes the persisted character-motion preference plus the Copilot
connection introduction. The character is currently static, so this preference
has no visible effect yet. Spoken replies and Do Not Disturb remain visibly
unavailable. Unknown parking disables preference changes and connection actions,
and app-use restrictions remain enforced by the app shell.

The Debug build adds a separate Debug mode row without replacing Do Not Disturb.
Its setting controls the labeled simulation overlay and defaults off independently
of the dormant launcher-character preference. Release does not expose the row or
overlay.

Settings take effect immediately. "Done" closes the screen rather than saving a
batch of changes. Show pending or failed saves and retain the last saved value
on failure. Each preference is independent.

### Copilot connection UI

The UI is implemented before live authentication. Settings and the Home/menu
conversation entries open the introduction. The disconnected Conversation placeholder
also offers a connection action when parking is verified.
When a provider is unavailable, the introduction keeps the same steps, companion,
content and action positions. Its note area states the limitation and its QR action
is disabled from first render. Back and Later return to the originating screen. No sample account,
working code or successful authentication is presented in the production route.
The remaining states are reusable components for future provider integration.

If companion context cannot be loaded, show a failure message and Retry. If an
observation fails after loading, keep the last committed companion visible and
offer Retry alongside the failure. Replace retained data when observation
recovers. A retained companion does not imply that its data is still updating or
that connection is available.

The Copilot presentation follows the current v4
[connection group](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=255-9688):
P51 introduction, P52 QR approval, P52 C address help, P52 B expiry, P53 success,
P54 reconnect, P56 access checks and P55 disconnect. A shared header and companion
panel frame the content; fit the 2560:1268 safe area with 884-unit companion and
1488-unit content panels separated by 44 units. Compact or enlarged-text windows
scroll and stack controls, with at least 76dp touch targets. Use the shared
`MobiMonColors` roles and bundled Noto Sans KR Regular/Bold, with zero
letter spacing and fractional glyph advances when scaling the composition. The
eight supplied SVGs provide the shared header, 48-unit panel corners, 656-unit
character placement, screen-specific font metrics, layouts and original icon
paths. Their identical original 1254×1254 transparent character
PNG is preserved in [mobimon_mobi_v4.png](../core/core-ui/src/main/res/drawable-nodpi/mobimon_mobi_v4.png)
and selected through `PetAvatar` for the default Mobi connection presentation.
Other appearances retain the renderer's existing fallbacks. Reference layouts
use the exported coordinates, except the account row uses shared centered layout
to correct the source reference's raised username; compact, enlarged-text and additional feedback
states reflow. The supplied QR pattern and example-account label are Debug-only
review data. Apply the [final visual acceptance](TESTING.md#final-figma-visual-acceptance)
separately from behavior verification.

The Debug-only **Copilot UI 체험** launcher connects all eight states using the
same components, with a persistent simulation notice, a fixed sample timer,
explicit expiry/reconnect/access scenarios, and labeled destination placeholders.
Keep its review controls outside the design composition without forcing the reference viewport
into the compact layout. It does not open GitHub or provide an operational service.
Missing QR images fall back to the selectable GitHub address and code, and expiry
hides the old code.

Home menu and Copilot Back use the same circular navigation control. Drawer
movement, backdrop fade and destination change share a 220ms timing. Selecting
a drawer destination closes the drawer while the destination changes. Both
screens remain rendered during the change: the old screen recedes slightly as
the new screen enters with a subtle fade and horizontal offset. Back reverses
the direction. Outgoing screens have no accessible or active controls.
Eligible panel changes fade in over 180ms and out over 120ms, without moving the
header or character. Countdown and pending-status updates retain the current panel.
Outgoing controls cannot dispatch actions or retain accessibility targets.
Expiry, reconnect, access checks and lost parking replace the panel immediately.
The character-motion preference does not alter navigation, drawer or panel
transitions. Android's animation duration scale still applies. These timings follow
the motion guidance above; the supplied static SVGs contain no motion specification.

### Planned live connection

The following behavior requires the
[future provider integration](ARCHITECTURE.md#ai-conversation-and-session).
Conversation will use the user's GitHub Copilot connection. When disconnected,
vehicle information and customization remain available; starting conversation
opens the connection flow.

The flow explains why connection is needed, shows the approval code and expiry,
and offers cancel or a fresh code. Detect approval automatically, with manual
recheck as a secondary action. Distinguish account approval from AI readiness,
access permissions and remaining usage. Show the actual account and failure
reason instead of treating every error as an expired login.

Disconnecting stops AI requests and removes the app's connection while
preserving points and customization. Clearly distinguish it from canceling a
Copilot subscription or revoking access at the provider.

## Vehicle launcher

Vehicle-launcher character placement is not part of the current product. Home
does not include a launcher-preview switch, and Settings does not expose a
launcher-character visibility control. A dormant off-by-default launcher
preference remains persisted for compatibility, but it has no supported UI or
renderer and cannot authorize display. Any future launcher integration requires
a new platform capability, safety review and explicit UX specification rather
than reusing the in-app Home screen.

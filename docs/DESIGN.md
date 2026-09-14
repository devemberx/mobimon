# Product design

MobiMon is a calm in-car companion for time spent parked. It combines a friendly
character, understandable vehicle information, conversation and personal
expression. The [Figma design](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=231-1176)
is the visual reference; this document defines the product experience.
Technical contracts belong in [ARCHITECTURE.md](ARCHITECTURE.md), and verification
belongs in [TESTING.md](TESTING.md).

## Concept

The core loop is **complete quests → earn points → buy accessories → personalize
your companion**. Points are spendable rewards, not experience. There are no
growth stages, levels, evolution or driving scores.

Users choose Mobi the dog or Luna the cat. Their choice expresses personality
and does not change quest rewards. The companion's expression reflects available
vehicle information, while clear text explains the actual condition. The
character provides warmth without replacing vehicle warnings or implying a
diagnosis.

Interaction is available while parking is verified and the vehicle allows app
use. Moving or unknown parking status pauses interaction and hides the launcher
character. Existing purchases, rewards, preferences and drafts remain intact.

## Visual language

- Use a deep navy setting, muted green scenery, cream speech bubbles and primary
  buttons, rounded panels and restrained decoration. Alternate backgrounds keep
  the same meanings for selection, warning and action colors.
- Use one fixed in-app vehicle-display palette through the shared MobiMonTheme;
  system light/dark preferences do not switch the app palette.
- Give the character visual priority on the home screen. Place navigation and
  parking status above it, with a vehicle summary and conversation action below.
  Point balance is secondary information beside the customization entry.
- Information screens pair the character with a clear content area.
  Customization pairs a large preview with the selection list. Reflow or scroll
  when space is limited instead of shrinking every element.
- Treat the 2560 × 1440 landscape artboard as a composition reference. Respect
  system bars, cutouts and the available window; do not require fixed pixels or
  a single aspect ratio.
- The [letterbox Home reference](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=128-569)
  reserves 76/96 artboard units for OS bars. Fit its 2560:1268 content inside the
  actual safe window without stretching or duplicating system bars. Use bundled
  Noto Sans KR Regular/Bold for this composition; keep enlarged-text and compact
  windows scrollable. The [font license](../core/core-ui/src/main/assets/fonts/OFL-NotoSansKR.txt)
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
- Keep motion brief and quiet. Reduced motion replaces decorative movement with
  a static expression. A successful connection may trigger one silent,
  character-appropriate greeting; it must not delay the next action.

## Reusable Compose library and asset handoff

The stateless library lives in [core-ui](../core/core-ui/src/main/java/com/monsters/mobimon/core/ui).
Use these components before adding a feature-local equivalent:

| API | Use |
| --- | --- |
| `MobiMonTheme`, `MobiMonTwilightColors` | Bundled typography and semantic v4 Night/Surface/Cream/Sky/Mint roles |
| `MobiMonDimensions` | Shared 76dp target, content spacing and panel/message corners |
| `MobiMonDestination` | Back/Home header and a slot for the full-content feature body |
| `MobiMonContentColumn`, `MobiMonSection` | Scrollable content and titled groups |
| `MobiMonButton` | Caller-owned enabled state and a label/icon slot with a 76dp minimum target |
| `MobiMonMessage`, `MobiMonSourceBadge` | Accessible feedback and explicit simulated/real labels |
| `MobiMonPointSummary` | Loading, failed, zero and committed balances |
| `PetAvatar`, `CompanionIcon` | Replaceable character artwork and shared icon rendering |

Each component and palette family has its own file. Production Quest, Vehicle,
Companion and AI routes use these APIs. `ComponentGallery` provides normal and
enlarged-text previews and a Debug-only **MobiMon UI Catalog · Demo** launcher;
it has no repository or provider. AI's rehearsal implementation, sample strings,
QR and manifest now live entirely in `feature-auth/src/debug`.

For new v4 screens, wrap the feature body with
`MobiMonTheme(colorScheme = MobiMonTwilightColors) { ... }`. The default palette
preserves existing screens during staged migration. The v4 color roles match the
shared UI rules in the supplied Figma file and the already exported Copilot
palette. Keep fixed artboard coordinates and screen-specific styling inside the
feature until a second consumer establishes a reusable component. Do not shrink
touch targets to match an artboard pixel measurement.

Import original Figma exports into the owning feature's resources, with a
feature-specific prefix. Shared fonts and character artwork belong in `core-ui`;
keep original raster bytes in `drawable-nodpi` and font licenses alongside fonts.
Record the node link in the relevant design section when adopting an asset.
Use Figma **Copy/Paste as SVG** for exact geometry, then retain the original
vector paths during Android conversion. Do not commit temporary MCP asset URLs,
whole-screen SVGs as UI, invented icon replacements, or duplicated shared artwork.
Remove superseded assets only after checking all variants, previews and tests.

Home's default Mobi artwork is the supplied `그림1.png` for P01 (128:569),
bundled unchanged as `drawable-nodpi/mobimon_mobi_v3.png`. Copilot selects the
original v4 artwork through `PetArtwork.COPILOT`, as described in its
[connection composition](#copilot-connection-ui). Luna, legacy Cream and
equipped-accessory variants still use placeholders.

This library is a migration foundation, not a claim that every screen matches
v4. Exact screen acceptance still requires the
[final visual comparison](TESTING.md#final-figma-visual-acceptance); unavailable
exports or unresolved differences must remain explicit in the implementation PR.

## Screens and navigation

| Screen | Purpose and primary content |
| --- | --- |
| Home | Selected companion and background, parking status, vehicle summary, point balance, conversation and customization entries. |
| Menu | A small overlay with Vehicle status, Quests and Settings. |
| Vehicle status | Available readings, specific warnings, freshness and connection status. |
| Quests | Conditions, reward type, point reward, progress and the next available action. |
| Customization | Friends, Outfits and accessories, and Backgrounds; preview, ownership and application states. |
| Copilot connection | Account connection guidance, approval/help, expiry, readiness, reconnect and disconnect states; see the [current UI scope](#copilot-connection-ui). |
| Conversation | Clearly identified speakers, conversation history, voice input and a text composer. |
| Settings | AI connection, vehicle-home character, spoken replies, reduced motion and Do Not Disturb. |

The menu opens over home. Its destinations, conversation and customization use
the full content area. Entering a menu destination closes the menu.

Back closes the keyboard first, then an open dialog or menu, then returns to the
previous screen. Menu destinations return to home; settings subpages return to
settings. A purchase dialog returns to the same item preview. An explicit
"Return home" action always opens home.

The app home and the vehicle launcher are separate surfaces. Launcher previews
illustrate placement; they do not provide map, media or climate controls.

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

The current Settings and menu composition follows v3: [P05 Settings](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=128-1030)
and [P02 Menu](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=128-642).
Settings fits the same 2560:1268 safe content reference as Home. Artboard rows
start at (352, 300), measure 1856×144 and have 24-unit gaps; Done is
(800, 1192), 960×104. Row titles use Noto Sans KR Regular 40/52 and descriptions
32/44, with zero letter spacing. The target AAOS image reports a physical
160dpi display but applies approximately 229dpi compatibility density to this
app (about 1792×888dp of content); previews use that app window, not pixel counts.
Smaller or enlarged-text windows reflow and scroll. At the reference viewport,
the menu preserves its measured visual bounds while expanding transparent hit
areas to at least 76dp. When these areas would overlap, it uses a scrolling
layout with 24dp gaps. Panel and control outlines use `#708C99` and `#748F9A`
(3.28:1 and 3.04:1 against their respective surfaces), brighter than the reference
outlines to retain the required non-text contrast.

Settings exposes the persisted in-app vehicle-home preview and reduced-motion
preferences, plus the Copilot connection introduction. Spoken replies and Do Not
Disturb remain visibly unavailable. Unknown parking disables preference changes
and connection actions, and app-use restrictions remain enforced by the app shell.

Settings take effect immediately. "Done" closes the screen rather than saving a
batch of changes. Show pending or failed saves and retain the last saved value
on failure. Each preference is independent.

### Copilot connection UI

The UI is implemented before live authentication. Settings and the conversation
entry open the introduction; Back and Later return to the originating screen.
Requesting a QR currently explains that connection is not available. No sample
account, working code or successful authentication is presented in the production
route. The remaining states are reusable components for future provider integration.

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
`MobiMonConnectionColors` roles and bundled Noto Sans KR Regular/Bold, with zero
letter spacing and fractional glyph advances when scaling the composition. The
eight supplied SVGs provide the shared header, 48-unit panel corners, 656-unit
character placement, screen-specific font metrics, layouts and original icon
paths. Their identical original 1254×1254 transparent character
PNG is preserved in [mobimon_mobi_v4.png](../core/core-ui/src/main/res/drawable-nodpi/mobimon_mobi_v4.png)
and selected through `PetAvatar` for the default Mobi connection presentation.
Other appearances retain the renderer's existing fallbacks. Reference layouts
use the exported coordinates; compact, enlarged-text and additional feedback
states reflow. The supplied QR pattern and example-account label are Debug-only
review data. Apply the [final visual acceptance](TESTING.md#final-figma-visual-acceptance)
separately from behavior verification.

The Debug-only **Copilot UI 체험** launcher connects all eight states using the
same components, with a persistent simulation notice, a fixed sample timer,
explicit expiry/reconnect/access scenarios, and labeled destination placeholders.
Its local motion toggle does not change the saved app preference. Keep its review
controls outside the design composition without forcing the reference viewport
into the compact layout. It does not open GitHub or provide an operational service.
Missing QR images fall back to the selectable GitHub address and code, and expiry
hides the old code.

Eligible panel changes fade in over 180ms and out over 120ms, without moving the
header or character. Countdown and pending-status updates retain the current panel.
Outgoing controls cannot dispatch actions or retain accessibility targets.
Expiry, reconnect, access checks and lost parking replace the panel immediately.
The persisted reduced-motion preference also removes transitions. Android's animation
duration scale still applies. These timings are implementation choices following
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

## Vehicle-home character

The vehicle-home character is an optional companion on a supported vehicle
launcher, off by default. The existing in-app vehicle-home preview setting does
not opt the user in to launcher display. It does not control the character inside
MobiMon.

Separate the user's display preference from actual status: visible, permission
needed, waiting for parking, unsupported or temporarily unavailable. Permission
requests are not themselves a successful enablement.

Offer size and position choices only within a safe area that avoids navigation,
warnings, system bars and vehicle controls. This applies to the character,
accessories, touch target and expanded menu. Hide the character when no safe
placement is available.

The character menu provides Open MobiMon, Hide for this parking session and
Close. Temporary hiding preserves the display preference and ends at the next
verified parking session or an explicit restore action in settings.

Do Not Disturb suppresses automatic sounds, unsolicited speech bubbles and
decorative motion. User-initiated text conversation remains available while
parked. It does not silence vehicle warnings; fully hiding the character is a
separate action.

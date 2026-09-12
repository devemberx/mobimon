# Product design

MobiMon is a calm in-car companion for time spent parked. It combines a friendly
character, understandable vehicle information, conversation and personal
expression. The [Figma design](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=6-3347)
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
- Give the character visual priority on the home screen. Place navigation and
  parking status above it, with a vehicle summary and conversation action below.
  Point balance is secondary information beside the customization entry.
- Information screens pair the character with a clear content area.
  Customization pairs a large preview with the selection list. Reflow or scroll
  when space is limited instead of shrinking every element.
- Treat the 2560 × 1440 landscape artboard as a composition reference. Respect
  system bars, cutouts and the available window; do not require fixed pixels or
  a single aspect ratio.
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

## Screens and navigation

| Screen | Purpose and primary content |
| --- | --- |
| Home | Selected companion and background, parking status, vehicle summary, point balance, conversation and customization entries. |
| Menu | A small overlay with Vehicle status, Quests and Settings. |
| Vehicle status | Available readings, specific warnings, freshness and connection status. |
| Quests | Conditions, reward type, point reward, progress and the next available action. |
| Customization | Friends, Outfits and accessories, and Backgrounds; preview, ownership and application states. |
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

Conversation uses the user's GitHub Copilot connection. When disconnected,
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

Settings take effect immediately. "Done" closes the screen rather than saving a
batch of changes. Show pending or failed saves and retain the last saved value
on failure. Each preference is independent.

## Vehicle-home character

The vehicle-home character is an optional companion on a supported vehicle
launcher, off by default. It does not control the character inside MobiMon.

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

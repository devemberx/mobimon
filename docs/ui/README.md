# V5 UI reference exports

The 39 SVGs in this folder are direct exports of the current visible screens on the
[v5 Figma page](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-2),
exported on 2026-09-23 after the [issue #127](https://github.com/devemberx/mobimon/issues/127)
system-bar and content reflow updates.
Figma checkpoint: `2402430463934068827`. The hidden duplicate Home frame is excluded.
They are full-screen visual references, not Android runtime resources or proof
that a feature is implemented. [DESIGN.md](../DESIGN.md) defines behavior and
implementation exceptions; [ARCHITECTURE.md](../ARCHITECTURE.md) records current support.

Use lowercase kebab-case names describing the visible state, grouped by feature.
Keep recovery and pending screens beside the feature they belong to. Do not use
export sequence numbers as filenames. Preserve the original SVG bytes when moving
or renaming; replace a reference only with an intentional new export. Each visible
state in the index links to its source frame.

Every export has a 2560 × 1440 viewBox. System bars occupy y=0–96 and y=1280–1440,
leaving 2560 × 1184 app content. Compare app content separately from real system UI.
Standard tall panels and bottom customization actions end at y=1256, leaving 24
reference units above navigation. Menu, Settings and conversation controls reflow
within the available height; font sizes and artwork proportions are preserved.
These are design coordinates, not runtime dp values; use actual device insets.
Embedded character bitmaps and outlined text are part of the source export;
extract approved assets for implementation instead of displaying an entire SVG.

The [keyboard frame](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=630-2382)
uses a native AAOS API 34-ext9 keyboard capture: 2560 × 404 at y=1036, replacing the
bottom bar and leaving 2560 × 940 app content below the top bar. The background,
message panel and composer fit above the IME. Runtime keyboards vary by device and language.

The [connected frame](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-3261)
and app share the exported settings gear from
[Lucide 0.468.0](https://github.com/lucide-icons/lucide/blob/0.468.0/icons/settings.svg),
with its [license](licenses/lucide.txt).

The current page has no standalone parking-unverified, service-unavailable,
conversation connection-failed, microphone-unavailable, purchase-confirmation,
purchase-unconfirmed, vehicle data-unavailable, reward-saving or reward-unconfirmed
frames. Their earlier exports have been removed. These behaviors remain required
where specified in DESIGN.md; missing references do not establish visual acceptance.
Reply failure with inline retry and unowned item selection have current exports.

## Screen index

### Shell (3)

| Reference | Visible state |
| --- | --- |
| [home.svg](shell/home.svg) | [Home](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-3973) |
| [menu.svg](shell/menu.svg) | [Home menu overlay](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-3844) |
| [settings.svg](shell/settings.svg) | [Settings](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-3641) |

### Connection (8)

| Reference | Visible state |
| --- | --- |
| [introduction.svg](connection/introduction.svg) | [Connection introduction](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-3458) |
| [qr-approval.svg](connection/qr-approval.svg) | [QR approval pending](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-3361) |
| [qr-expired.svg](connection/qr-expired.svg) | [Approval expired](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-3086) |
| [approval-help.svg](connection/approval-help.svg) | [Address and code help](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-3167) |
| [connected.svg](connection/connected.svg) | [Connected](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-3261) |
| [reconnect.svg](connection/reconnect.svg) | [Reconnect required](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-3556) |
| [disconnect-confirmation.svg](connection/disconnect-confirmation.svg) | [Disconnect confirmation](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-2907) |
| [access-check.svg](connection/access-check.svg) | [Copilot access check](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-2989) |

### Conversation (7)

| Reference | Visible state |
| --- | --- |
| [empty.svg](conversation/empty.svg) | [Conversation entry](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=630-2118) |
| [messages.svg](conversation/messages.svg) | [Conversation with messages](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=630-2006) |
| [reply-failed.svg](conversation/reply-failed.svg) | [Reply failed with inline retry](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=630-1668) |
| [reply-pending.svg](conversation/reply-pending.svg) | [Waiting for reply](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=630-1908) |
| [voice-listening.svg](conversation/voice-listening.svg) | [Voice recording](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=630-2206) |
| [voice-review.svg](conversation/voice-review.svg) | [Transcript review before send](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=630-2294) |
| [keyboard-input.svg](conversation/keyboard-input.svg) | [Keyboard and composer](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=630-2382) |

### Customization (9)

| Reference | Visible state |
| --- | --- |
| [friend-preview.svg](customization/friend-preview.svg) | [Friend preview before apply](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-2094) |
| [accessories.svg](customization/accessories.svg) | [Accessories catalog](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-1200) |
| [item-selected.svg](customization/item-selected.svg) | [Unowned accessory selected before purchase](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-1313) |
| [backgrounds.svg](customization/backgrounds.svg) | [Background catalog](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-1868) |
| [purchase-pending.svg](customization/purchase-pending.svg) | [Purchase pending](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-1426) |
| [owned.svg](customization/owned.svg) | [Owned item before apply](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-1539) |
| [apply-pending.svg](customization/apply-pending.svg) | [Application pending](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-1652) |
| [applied.svg](customization/applied.svg) | [Item applied](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-1755) |
| [insufficient-points.svg](customization/insufficient-points.svg) | [Insufficient points](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-1981) |

### Vehicle (3)

| Reference | Visible state |
| --- | --- |
| [charging-required.svg](vehicle/charging-required.svg) | [Charging required / hungry](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-890) |
| [tire-warning.svg](vehicle/tire-warning.svg) | [Tire warning / sick](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-999) |
| [checked-items-normal.svg](vehicle/checked-items-normal.svg) | [Checked items normal / default](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-1099) |

### Quests (9)

| Reference | Visible state |
| --- | --- |
| [list-all.svg](quests/list-all.svg) | [All quests](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-83) |
| [list-in-progress.svg](quests/list-in-progress.svg) | [In-progress quests](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-329) |
| [list-completed.svg](quests/list-completed.svg) | [Completed quests](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-446) |
| [empty-in-progress.svg](quests/empty-in-progress.svg) | [No in-progress quests](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-721) |
| [empty-completed.svg](quests/empty-completed.svg) | [No completed quests](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-805) |
| [detail-actionable.svg](quests/detail-actionable.svg) | [Quest detail before completion](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-565) |
| [detail-claimable.svg](quests/detail-claimable.svg) | [Quest detail ready to claim](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-5) |
| [detail-completed.svg](quests/detail-completed.svg) | [Quest detail already rewarded](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-643) |
| [reward-success.svg](quests/reward-success.svg) | [Reward committed dialog](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-202) |

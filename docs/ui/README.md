# V5 UI reference exports

The 46 SVGs in this folder are the user-supplied exports of the revised
[v5 Figma page](https://www.figma.com/design/7tyb4oJsJAUc15KnU7H0F6?node-id=524-2).
They are full-screen visual references, not Android runtime resources or proof
that a feature is implemented. [DESIGN.md](../DESIGN.md) defines behavior and
implementation exceptions; [ARCHITECTURE.md](../ARCHITECTURE.md) records current support.

Use lowercase kebab-case names describing the visible state, grouped by feature.
Keep recovery and pending screens beside the feature they belong to. Do not use
export sequence numbers as filenames. Preserve the original SVG bytes when moving
or renaming; replace a reference only with an intentional new export.

Every export has a 2560 × 1440 viewBox, including 76-unit top and 96-unit bottom
system bars. Compare the 2560 × 1268 app content separately from real system UI.
Embedded character bitmaps and outlined text are part of the source export;
extract approved assets for implementation instead of displaying an entire SVG.

The supplied set has no standalone **unowned item selected before purchase**
export. Use the purchase-confirmation reference for its transaction information;
do not claim full visual acceptance for the missing preview state.

## Screen index

### Shell (4)

| Reference | Visible state |
| --- | --- |
| [home.svg](shell/home.svg) | Home |
| [menu.svg](shell/menu.svg) | Home menu overlay |
| [settings.svg](shell/settings.svg) | Settings |
| [parking-unverified.svg](shell/parking-unverified.svg) | Parking verification required |

### Connection (9)

| Reference | Visible state |
| --- | --- |
| [introduction.svg](connection/introduction.svg) | Connection introduction |
| [qr-approval.svg](connection/qr-approval.svg) | QR approval pending |
| [qr-expired.svg](connection/qr-expired.svg) | Approval expired |
| [approval-help.svg](connection/approval-help.svg) | Address and code help |
| [connected.svg](connection/connected.svg) | Connected |
| [reconnect.svg](connection/reconnect.svg) | Reconnect required |
| [disconnect-confirmation.svg](connection/disconnect-confirmation.svg) | Disconnect confirmation |
| [access-check.svg](connection/access-check.svg) | Copilot access check |
| [service-unavailable.svg](connection/service-unavailable.svg) | Service unavailable |

### Conversation (8)

| Reference | Visible state |
| --- | --- |
| [empty.svg](conversation/empty.svg) | Conversation entry |
| [messages.svg](conversation/messages.svg) | Conversation with messages |
| [reply-pending.svg](conversation/reply-pending.svg) | Waiting for reply |
| [voice-listening.svg](conversation/voice-listening.svg) | Voice recording |
| [voice-review.svg](conversation/voice-review.svg) | Transcript review before send |
| [keyboard-input.svg](conversation/keyboard-input.svg) | Keyboard and composer |
| [connection-failed.svg](conversation/connection-failed.svg) | Conversation connection failed |
| [microphone-unavailable.svg](conversation/microphone-unavailable.svg) | Microphone unavailable |

### Customization (10)

| Reference | Visible state |
| --- | --- |
| [friend-preview.svg](customization/friend-preview.svg) | Friend preview before apply |
| [accessories.svg](customization/accessories.svg) | Accessories catalog |
| [backgrounds.svg](customization/backgrounds.svg) | Background catalog |
| [purchase-confirmation.svg](customization/purchase-confirmation.svg) | Purchase confirmation |
| [purchase-pending.svg](customization/purchase-pending.svg) | Purchase pending |
| [purchase-unconfirmed.svg](customization/purchase-unconfirmed.svg) | Purchase result unconfirmed |
| [owned.svg](customization/owned.svg) | Owned item before apply |
| [apply-pending.svg](customization/apply-pending.svg) | Application pending |
| [applied.svg](customization/applied.svg) | Item applied |
| [insufficient-points.svg](customization/insufficient-points.svg) | Insufficient points |

### Vehicle (4)

| Reference | Visible state |
| --- | --- |
| [charging-required.svg](vehicle/charging-required.svg) | Charging required / hungry |
| [tire-warning.svg](vehicle/tire-warning.svg) | Tire warning / sick |
| [checked-items-normal.svg](vehicle/checked-items-normal.svg) | Checked items normal / default |
| [data-unavailable.svg](vehicle/data-unavailable.svg) | Vehicle data unavailable |

### Quests (11)

| Reference | Visible state |
| --- | --- |
| [list-all.svg](quests/list-all.svg) | All quests |
| [list-in-progress.svg](quests/list-in-progress.svg) | In-progress quests |
| [list-completed.svg](quests/list-completed.svg) | Completed quests |
| [empty-in-progress.svg](quests/empty-in-progress.svg) | No in-progress quests |
| [empty-completed.svg](quests/empty-completed.svg) | No completed quests |
| [detail-actionable.svg](quests/detail-actionable.svg) | Quest detail before completion |
| [detail-claimable.svg](quests/detail-claimable.svg) | Quest detail ready to claim |
| [detail-completed.svg](quests/detail-completed.svg) | Quest detail already rewarded |
| [reward-saving.svg](quests/reward-saving.svg) | Reward save pending |
| [reward-unconfirmed.svg](quests/reward-unconfirmed.svg) | Reward save unconfirmed |
| [reward-success.svg](quests/reward-success.svg) | Reward committed dialog |

# Android development skills

This project vendors two English-language skills for Android development.
The upstream skill files and their bundled references are unchanged.

| Skill | Purpose | Upstream source | Pinned commit | License |
| --- | --- | --- | --- | --- |
| [compose-agent](compose-agent/SKILL.md) | Write and review Jetpack Compose code, state, lifecycle, and animations. | [hamen/compose_skill](https://github.com/hamen/compose_skill/tree/f815c31d6cc1a4af4ce2796d08eb7d8b506785c9/skills/compose-agent) | `f815c31d6cc1a4af4ce2796d08eb7d8b506785c9` | [MIT](compose-agent/LICENSE) |
| [testing-setup](testing-setup/SKILL.md) | Review and configure Android unit, UI, screenshot, and device testing. | [android/skills](https://github.com/android/skills/tree/bac232fd02b0855df9275281a2a7a47643768719/testing/testing-setup) | `bac232fd02b0855df9275281a2a7a47643768719` | [Apache-2.0](testing-setup/LICENSE.txt) |

Imported on 2026-09-09 using the Codex skill installer.
Repository-level licenses are included with each skill.

## Usage

Codex discovers these skills in `.agents/skills/`. For other agents, use their
skill import mechanism or ask them to read the relevant `SKILL.md` directly.

Example prompts:

- `Use compose-agent to implement this Compose screen using the project's existing dependencies.`
- `Use compose-agent to review state restoration and animation lifecycle in this feature.`
- `Use testing-setup to review the current Android test setup and identify the checks needed for this change.`

Apply these workflows within the user's requested scope and the repository's
instructions. Check upstream version assumptions against the project's actual
toolchain. The Compose skill is not a substitute for AAOS-specific documentation
and validation on the provided vehicle environment.

## Updates

Review upstream changes before replacing either skill. Import the skill directory
with all of its references, keep its license, and update the pinned commit above.

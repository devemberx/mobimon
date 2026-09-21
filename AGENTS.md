# Repository guidelines

## Workflow

- Read [CONTRIBUTING.md](.github/CONTRIBUTING.md) before development or Git/GitHub work. It owns setup, dependencies, checks, branches, commits, PRs and squash merges; prefer `gh pr merge --squash` when merging.
- Inspect source/build configuration before claiming a module, integration or task exists. Read [ARCHITECTURE.md](docs/ARCHITECTURE.md) for structural/behavior changes, [DESIGN.md](docs/DESIGN.md) for UI, and [TESTING.md](docs/TESTING.md) for tests.
- Before using `ui-ux-pro-max`, read the [project integration rules](.agents/skills/README.md#uiux-design-guidance).
- For PR/commit description requests (including "description 적어줘/써줘"), output exactly two single-line English bullets starting directly with `- `: why, then what changed; at most 120 characters each.

## Code and safety

- Prefer clear names and structure. Comment only non-obvious intent, constraints or workarounds; keep comments and API contracts current.
- Keep domain code independent of Android, features independent of each other/concrete data implementations, and bindings in `app`. Follow the [module boundaries](docs/ARCHITECTURE.md#target-modules-and-dependencies).
- Keep simulations in Debug/test sources with separate IDs, profiles and databases. Release vehicle data stays unavailable until a real adapter is verified; unknown driving state cannot authorize commands.
- Example accounts/codes and rendered connection success are not provider verification. Follow the [connection boundary](docs/ARCHITECTURE.md#copilot-connection-ui).
- Reward writes must use atomic repository transactions with evidence, ownership/revision and occurrence-uniqueness checks. UI/AI must not grant rewards directly.
- Use the current [visual specification](docs/DESIGN.md), shared `core-ui` primitives and replaceable [PetAvatar](core/core-ui/src/main/java/com/monsters/mobimon/core/ui/PetAvatar.kt). Keep rewards, equipment and authorization outside the renderer. Do not restore obsolete XP/progression UI.
- Generate asset variants from the approved master. Follow the [variant constraints](docs/DESIGN.md#image-asset-locations), change only requested properties and reject unintended drift.

## Documentation

- Write briefly and plainly. Include only information needed to understand, implement or verify the project; remove unnecessary, stale and verbose content.
- Keep one authoritative home per topic: CONTRIBUTING for workflow, ARCHITECTURE for technical contracts, DESIGN for UX, TESTING for coverage, `docs/ui/README.md` for exports, and AGENTS for essential agent rules. Link instead of repeating content; keep safety reminders brief.
- When code changes make documentation or AGENTS.md inaccurate or incomplete, update the affected files in the same change. Keep current implementation separate from planned work, and update the test requirement map for changed critical behavior.
- Revise existing sections instead of adding overlapping ones. Keep assignments, plans and run logs in issues/PRs; local `docs/superpowers/` plans remain ignored.
- Review AGENTS.md itself for unnecessary detail and duplication. Before committing docs, check relative links/anchors and preserve safety contracts and verification limits when shortening.
- Update CONTRIBUTING and CI together when required checks change. For shared skill changes, edit `.agents/skills/` and copy the entire affected folder, including references/licenses, to `.claude/skills/` in the same commit.

## Verification

- Run the [canonical checks](.github/CONTRIBUTING.md#verification) for the change type. Place focused tests in the subject module/package; one test file per source file is unnecessary.
- Complete [final visual acceptance](docs/TESTING.md#final-figma-visual-acceptance) after the final UI code change. Feature owners own screen acceptance; shared-component or build success is insufficient.
- Report only checks actually executed. APK assembly and Robolectric do not prove device tests or real integrations ran.

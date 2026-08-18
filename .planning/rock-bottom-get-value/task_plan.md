# Task Plan: 谷底石 GetValue 重构

> Final state note (2026-08-15): This feature history includes several superseded
> proposals. The implemented design does not preserve unpublished old-save
> compatibility, does not use sprint-specific handling, and owns Rock Bottom
> counting in `handleObtain()`/`handleRemove()`. See
> `.planning/project-memory/` for the consolidated final state.

> 执行必须继续使用 `isaac-disaster-item-creation` 技能。

## Goal

将谷底石从每 20 tick 添加补偿 modifier 的实现重构为在
`AttributeInstance#getValue()` 返回最终属性值时维护历史最高值，并保持客户端同步和现有注册身份；不考虑未发布版本的旧存档兼容。

## Confirmed Identity And Scope

- Wiki original item name: Rock Bottom
- Wiki original ID: `485`
- Mod `ItemId`: `ItemId.ROCK_BOTTOM`
- Registry ID: `rock_bottom`
- Existing level: `3`
- Do not add or reorder `ItemId` constants
- Target attributes: attack damage, movement speed, luck, bullet speed, tears,
  tears correction, and bullet range
- Texture, item pools, and registry IDs remain unchanged

## Phases

### Phase 1: Plan And Source Map

- [x] Read the root planning files and complete session catch-up.
- [x] Review the approved implementation plan and required skills.
- [x] Create this feature-specific planning directory and files.
- [x] Verify current Rock Bottom, capability, Mixin, packet, and test patterns.
- **Status:** complete

### Phase 2: TDD State Resolution

- [ ] Locate the project test harness or establish the smallest available focused test.
- [ ] Write a failing test for historical-value resolution before production changes.
- [x] Verify the failure is caused by the missing new behavior.
- [x] Implement the minimal pure state-resolution helper and make the test pass.
- **Status:** complete

### Phase 3: Attribute Ownership And GetValue Hook

- [ ] Add public owner binding/accessors for `AttributeMap` and `AttributeInstance`.
- [ ] Bind every living entity's attribute instances to that entity.
- [ ] Intercept `AttributeInstance#getValue()` for the seven target attributes.
- [ ] Return vanilla final values for non-player entities, non-target attributes, and players without Rock Bottom.
- **Status:** complete

### Phase 4: Persistence, Sync, Lifecycle, And Migration

- [ ] Keep server history authoritative in `ExtraData`.
- [ ] Add client cache and S2C history synchronization for login, updates, and complete removal.
- [ ] Refactor passive ability to stop installing the recursive module and compensating modifier.
- [ ] Clear history when the final Rock Bottom copy is removed.
- [x] Remove old recursive modules, executable effects, and legacy modifiers without compatibility migration.
- [x] Verify clone, reload, and login paths preserve or clear state as specified.
- **Status:** complete

### Phase 5: Localization And Verification

- [ ] Update the tooltip wording to describe historical final attribute value.
- [ ] Confirm lore layout is `no split needed` and resources/pools/textures are unchanged.
- [x] Run `runData` once and inspect generated-data output.
- [ ] Run `git diff --check`.
- [x] Run `git diff --check`.
- [x] Review the final diff for unrelated changes and record residual test limitations.
- **Status:** complete

## Follow-up: State Safety And Sprint Exclusion

- [x] Document the Rock Bottom state utility with Javadocs.
- [x] Guard resolution with `AttributeMap.hasAttribute` before history access.
- [x] Preserve the `null AttributeInstance` guard in `AttributeMapMixin`.
- [x] Remove special sprint handling; movement speed follows normal history resolution.
- [x] Keep `activeCurioSlots` as the effective passive-item count source.
- [x] Keep old recursive, executable, modifier, and migration code removed.

### Follow-up Verification

- [x] Run the configured `test` task.
- [x] Run `runData` once and confirm the Mixin loads.
- [x] Run `git diff --check`.
- [x] Search for removed legacy references with `rg`.
- [x] Do not run `build`.
- [x] Record the non-fatal locked `run-data/logs/latest.log` cleanup warning.

## Verification Constraints

- Do not run `build` or compile tasks.
- Server data is authoritative; client cache is display/interception support only.
- Multiple Rock Bottom copies share one history.
- Re-obtaining after complete removal starts from the current value again.
- `getItemCountFromAll` and `getItemCountMapFromAll` use the applied Curios slot records.

## Errors Encountered

| Error | Attempt | Resolution |
| --- | --- | --- |
| `python` was unavailable on PowerShell `PATH` | 1 | Use the bundled absolute Python runtime path |
| `py` launcher was unavailable | 2 | Use the bundled absolute Python runtime path |
| Combined isolation probe returned nonzero with no output | 1 | Split successful Git queries into independent read-only calls |
| Gradle test could not start because `JAVA_HOME` points to a missing JDK 21 | 1 | Search for a local Java 17 runtime before proceeding |
| First `runData` failed because a `getValue()` HEAD Mixin injection used `CallbackInfo` | 1 | Change the callback to `CallbackInfoReturnable<Double>` and rerun `runData` |

## Follow-up: PlayerTick And ServerTick Responsibility Split

- [x] Confirm the old player-tick handler performed nested full-player traversal and repeated global maintenance.
- [x] Process only the current server player in `PlayerTickEvent.END` using `player.tickCount` cadences.
- [x] Run scheduled functions and token-bucket recovery once from `ServerTickEvent.END`.
- [x] Run `test`, `runData`, and `git diff --check` for this follow-up.
- **Status:** complete

## Follow-up: Lifecycle Count And Event-Driven Sync

- [x] Move Rock Bottom count updates into `handleObtain` and `handleRemove`.
- [x] Rebuild the transient count after capability load and death-copy.
- [x] Remove Rock Bottom-specific count inference from item/Curios container mutators.
- [x] Remove the 20-tick full passive-data sync.
- [x] Synchronize passive counts after completed mutations and on respawn.
- [x] Run final `test`, `runData`, and `git diff --check`.
- **Status:** complete

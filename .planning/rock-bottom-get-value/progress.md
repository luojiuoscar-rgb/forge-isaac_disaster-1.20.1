# Progress Log: 谷底石 GetValue 重构

## Session: 2026-08-14

### Planning And Recovery

- Read the root `task_plan.md`, `findings.md`, and `progress.md`.
- Ran planning session catch-up with the bundled Python runtime; no unsynced
  context was reported.
- Read and applied `planning-with-files`, `minecraft-modding`,
  `isaac-disaster-item-creation`, TDD, executing-plans, and worktree guidance.
- Confirmed the worktree is the user-provided normal checkout on `master` and
  preserved unrelated untracked files.
- Created the feature-specific planning files.

### Current Phase

- Phase 2, TDD state resolution: in progress.

### Source Map Updates

- Confirmed the old passive -> recursive module -> normal effect chain and all
  existing registration identities with `rg`.
- Confirmed `ExtraData`, `ClientDataManager`, and network-related classes are
  the relevant persistence/synchronization areas.
- Read the old passive, recursive, normal effect, `ExtraData`, client cache,
  network registration, and Mixin configuration files.
- Confirmed packet IDs are order-sensitive and the new S2C registration should
  be appended.
- Confirmed JUnit 5 is configured and located the login/recursive tick owners in
  `ForgeEvents` and `ServerTickEvent`.
- Read `PassiveAbility`, `LivingEntityMixin`, and the relevant Forge event/tick
  sections; identified the per-stack removal and death-clone boundaries.
- Confirmed passive counts include Curios slots, `ExtraData` is cloned, and the
  recursive queue needs an explicit silent legacy-removal operation.
- Read the Curios equip/unequip reconciliation path and recorded the potential
  physical-slot timing issue for final-copy cleanup.

### Errors During Source Lookup

- A packet read used a guessed filename and produced no result; locate packet
  names with `rg --files` before retrying.
- Located the actual packet files and local Forge mapped artifacts for the next
  API-signature check.
- Read map-based S2C packet templates; confirmed full-replace/empty-map sync
  semantics and append-only packet registration.
- Added attribute owner accessors and common Mixins, the Rock Bottom state
  helper, client history cache, S2C packet, packet registration, lifecycle
  migration calls, and applied passive-count cleanup.
- Confirmed unchanged texture/pool resources and located the tooltip wording
  that requires the planned final-value update.
- During core review, identified and planned a pre-calculation legacy migration
  guard so an old modifier cannot affect the first post-load value read.

### Errors During Planning

- One findings patch did not match because the file's existing Chinese text was
  displayed with a different encoding; no content changed, and the next patch
  used an ASCII heading anchor.

### Test Results

| Check | Result |
| --- | --- |
| Session catch-up | pass; no output indicating unsynced context |
| RED focused JUnit | pass; 1 test failed because `RockBottomValueHistory` is missing |
| GREEN focused JUnit | pass; 1 test passed after adding `RockBottomValueHistory.resolve` |

### Error Log

| Error | Attempt | Resolution |
| --- | --- | --- |
| `python` command missing | 1 | Used bundled runtime at `C:\Users\16136\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe` |
| `py` launcher missing | 2 | Used the same bundled runtime |
| Multi-query isolation shell call returned exit 1 without output | 1 | Re-ran only successful Git probes independently |
| Gradle test refused invalid `JAVA_HOME` (`jdk-21.0.8.9-hotspot`) | 1 | Search for a Java 17 runtime; RED test remains unexecuted |
| Sandboxed Gradle download denied | 1 | Re-ran the focused test with approved network escalation |

### runData Failure

- The first requested `runData` execution reached Mixin application and failed
  because the `getValue()` HEAD injection used `CallbackInfo` on a method with
  a return value. Mixin requires `CallbackInfoReturnable<Double>` for this
  target; the source is being corrected before the verification rerun.

### TDD State Resolution

- Added the failing JUnit test before production code.
- Added `RockBottomValueHistory.resolve(double, Double)` as the minimal pure
  implementation.
- Re-ran the focused test successfully with Java 17.

### Integration And Datagen

- Added owner binding through `LivingEntity -> AttributeMap -> AttributeInstance`
  and the GetValue HEAD/RETURN hooks.
- Added server `ExtraData` history, client cache, full-replace S2C sync, final
  copy cleanup, and legacy recursive/modifier migration.
- Updated only the Rock Bottom tooltip wording; texture and pools were unchanged.
- First `runData` failed at runtime because of the invalid HEAD callback type;
  after changing it to `CallbackInfoReturnable<Double>`, `runData` completed
  successfully and loaded the new Mixin.
- Final focused JUnit passed 1/1 after the runtime correction.
- Refactored the focused test to call the production API directly; the final
  direct-API test passed 1/1.
- `git diff --check` passed; only Git's normal LF-to-CRLF conversion warnings
  were emitted.
- Final status review found no generated-resource changes, no pool/texture
  changes, and no edits to the pre-existing unrelated untracked files.
- The new `RockBottomHistorySyncS2CPacket.java` remains untracked in the
  worktree status even though Gradle compiled and loaded it successfully.

### Residual Verification Limits

- No dedicated Minecraft game test was added for live equipment/potion,
  multiplayer packet timing, Curios replacement timing, or save/reload. The
  source-level lifecycle paths and `runData` Mixin loading were verified, while
  those scenarios remain runtime test cases for a real client/server session.

### Integration State

- Kept changes on the user's existing `master` checkout.
- No commit, merge, push, branch deletion, or worktree cleanup was performed.

## 2026-08-14 - State Safety And Sprint Exclusion

- Recovered the previous session with the bundled Python runtime; the system
  Python command is unavailable on PATH.
- Confirmed that `AttributeMapMixin` already skips owner binding for a null
  instance, while `RockBottomState` still needed an explicit `hasAttribute`
  guard before history processing.
- Confirmed vanilla 1.20.1 sprinting adds a movement-speed modifier; the new
  implementation will preserve the historical floor but skip sprint reads when
  deciding whether to raise or synchronize history.
- Implementation and verification are in progress.

### Implementation Details

- Added Javadoc to the Rock Bottom state class, target lists, value resolver,
  count lookup, sprint recording rule, synchronization, and cleanup methods.
- Added an `AttributeMap.hasAttribute` guard before any Rock Bottom history key
  or capability access.
- Kept the vanilla value as the returned value during sprint when it exceeds
  history, but prevented that sprint value from being persisted or synchronized.
- Preserved the existing null check in `AttributeMapMixin`.

## 2026-08-14 - State Pipeline Simplification

- Changed scalar and map passive-item counts to use `activeCurioSlots` instead
  of reading physical Curios slots; removed the duplicate applied-count method
  and updated all in-project callers.
- Moved inactive-history cleanup into an idempotent `RockBottomState` helper,
  called by passive removal and guarded from the server `getValue()` path.
- Removed the legacy recursive module, executable effect, modifier UUID,
  migration calls, and `removeSilently`; the passive item and all item assets
  remain unchanged.
- Expanded the pure history test to cover missing, higher, lower, and equal
  current values.
- The first focused test attempt was blocked by the sandbox while Gradle tried
  to download its distribution; the approved network retry passed with Java
  21.0.8 and completed `compileJava`, `compileTestJava`, and the focused test.
- Reordered login synchronization so `CuriosHelper.syncAllIsaacCurios` rebuilds
  applied slot state before the client item-count and history packets are sent.
- Final focused JUnit passed after that ordering change; final `runData` passed
  and loaded `AttributeInstanceMixin` without data-generation errors.
- Final `git diff --check` passed. No `build` task was run. The remaining
  untracked files are pre-existing unrelated workspace files.
- The full configured Gradle `test` task also passed with Java 21.0.8.

## 2026-08-14 - Follow-up Completion

- Completed the requested Javadocs for the Rock Bottom state utility, including
  target attributes, history keys, value resolution, item count, sprint rules,
  synchronization, and inactive cleanup.
- Added the `AttributeMap.hasAttribute` guard and retained the null-instance
  guard in `AttributeMapMixin`; missing target attributes return the vanilla
  value without reading or writing history.
- Excluded sprint movement-speed values from raising or synchronizing history,
  while preserving existing historical-value enforcement during sprinting.
- Re-ran the configured `test` task, `runData`, `git diff --check`, and legacy
  reference searches successfully. `runData` emitted only the known non-fatal
  warning that `run-data/logs/latest.log` was locked during cleanup.
- No `build` task was run. No resource, texture, pool, ItemId, or registry
  identity changes were needed for this follow-up.

## 2026-08-14 - Sprint Rule Removed

- Removed the dedicated sprint check from `RockBottomState`.
- Movement speed now follows the same history-recording path as every other
  target attribute; sprinting is intentionally not treated as a special case.
- Verified with `.\gradlew.bat test --no-daemon`; the test task passed. The only
  compiler warning is the pre-existing deprecated `SoulStateEffect` API usage.

## 2026-08-14 - PlayerTick And ServerTick Split

- Reworked `ServerTickEvent.onPlayerTick` to process only its current `ServerPlayer`.
- Replaced the shared `tickCounter` with each player's `player.tickCount`, preserving the 4, 20, 200, and 3 tick cadences without multiplayer acceleration.
- Moved `ScheduledFuncHelper.tick(server)` and `AbilityEffectTokenBucket.tick()` to one `ServerTickEvent.END` handler.
- Removed the per-player event's nested full-player-list traversal; player-level work is now `O(n)` per server tick and global maintenance runs once.
- The required session catch-up could not run with the unavailable system `python` and `py` commands; existing planning files and Git state were inspected directly.

### PlayerTick Verification

- The first sandboxed `test` and `runData` attempts were blocked while the Gradle wrapper tried to download Gradle 8.8; approved network retries completed successfully.
- `test` passed with Java 21.0.8; compilation emitted only the pre-existing `SoulStateEffect` deprecation warning.
- `runData` passed and the log confirmed `AttributeInstanceMixin` was applied; no data-generation error occurred.
- Final `git diff --check` passed. `ServerTickEvent.java` contains no `tickCounter` or nested player-list traversal, and the scheduler/token-bucket calls each occur only in `onServerTickEnd`.
- No `build` task was run.

## 2026-08-15 - Lifecycle Count And Event-Driven Sync

- Added a transient `rockBottomCount` cache to `PlayerIsaacItems`.
- Moved normal count increments and decrements into `RockBottom.handleObtain`
  and `RockBottom.handleRemove`; container mutators no longer infer or adjust
  the Rock Bottom count.
- Rebuilt the cache after capability NBT loading and death-copy restoration.
- Changed `RockBottomState` server lookup to read the O(1) cache directly.
- Removed the per-player 20-tick full client data synchronization.
- Added passive-count synchronization after completed passive and Curios
  mutations, with change detection for the existing 200-tick Curios repair.
- Added login/respawn Curios reconstruction followed by complete client data
  synchronization.

### Verification In Progress

- `git diff --check`: passed before Gradle verification.
- `test`: first attempt blocked before Gradle startup because `JAVA_HOME`
  points to the missing `C:\Users\16136\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.8.9-hotspot` directory.
- The Java 17 retry reached the Gradle wrapper but sandbox network policy
  denied downloading Gradle 8.8; the next attempt uses approved external
  network access.
- `runData`: first attempt was blocked at the same Gradle 8.8 wrapper download;
  retry with approved external network access is required.
- The external-network approval retry was rejected because the approval
  service disconnected; a complete local Gradle 8.8 distribution was found and
  will be invoked directly without network access.
- Direct local Gradle startup reached project configuration, but offline
  resolution could not find the `foojay-resolver-convention:0.7.0` plugin;
  an offline retry is being checked before marking `runData` environment-blocked.

## 2026-08-15 - Lifecycle Count Completion

- Changed `getItemCountMapFromAll()` so Rock Bottom is reported from the
  lifecycle cache instead of being inferred from passive-item containers.
- Prevented a full passive-item backpack from invoking `handleObtain()` before
  `addItem()` rejects the new item, avoiding a count/effect mismatch.
- Final `test` passed with Java 21.0.8.
- Final `runData` passed and loaded `AttributeInstanceMixin` successfully.
- Final `git diff --check` passed; only normal Git line-ending warnings were
  emitted. No `build` task was run.
- Legacy Rock Bottom module, executable effect, modifier UUID, migration API,
  and player-tick counter searches returned no references.

# Progress Log: AttackContext Refactor

## Session: 2026-08-28 Final Synchronization
- **Status:** complete
- Consolidated the completed refactor into the final `task_plan.md` and `findings.md` state. Removed stale descriptions of angle offsets, old pipeline modes, standalone BrimstonePattern, and per-emission Context allocation.
- Recorded the final main-axis, freeze, runtime snapshot, split inheritance, Cricket's Body impact-plane, and Parasite contracts for subsequent split-item work.
- Recorded the latest split derivation fix: `toBuilder().direction(velocity).build()` now normalizes `direction` as the derived Context main axis instead of requiring a non-null `mainAxis`.
- Latest verification before this documentation sync: Gradle `compileJava` and `test` passed in the available terminal JDK environment; final Forge/JDK 17 runtime validation remains IDE/manual work.
- Documentation note: the first replacement patch attempted delete/add for the same file in one `apply_patch` operation and was rejected before changes. The files were then replaced through separate delete and add operations.

## Session: 2026-08-27 V5 Freeze Boundary Implementation
- **Status:** in progress
- Added `AttackContext` freeze state; frozen mutation attempts log a warning and preserve the existing value.
- Removed Context trigger/split mutable getters. Prepare modules now use controlled trigger/split mutation methods; runtime Tear/Laser objects own independent trigger and split-sequence snapshots.
- Pipeline finalizes the plan list, runs Prepare, then freezes successful Contexts immediately before execution. `EXECUTE_ONLY` freezes supplied Contexts before direct execution.
- Pattern, split, Brimstone, and Shoop paths now use `toBuilder()` for mutable derivation rather than mutating a Context copy.
- Added freeze and derivation regression coverage; plain Gradle focused tests pass after retaining the Unsafe fixture's non-entity owner fields through the dedicated derived-builder path.
- Verification: focused freeze, Bullet/Laser/Wiz Pattern, and Brimstone tests passed; `compileJava --offline` completed with updated class output; `git diff --check` reports only existing line-ending warnings.
- **Status:** complete

## Session: 2026-08-27 V4 Pipeline Semantics
- **Status:** complete
- Approved scope: rename pipeline modes to explicit lifecycle stages; remove `AttackOrigin.PLAYER_SCHEDULED`; route Cursed Eye scheduled shots through player-primary plan/prepare execution while retaining one release-level Before event.
- Confirmed `EXECUTE_ONLY` remains necessary for split children and Laser Plus Fetus trigger-snapshot isolation.
- Environment note: system `python` is unavailable; verification will use the Gradle-managed JDK 17 toolchain.
- Renamed all pipeline modes and migrated all production callers; no old mode names or `PLAYER_SCHEDULED` remain in source/test code.
- Cursed Eye now sends one release-level Before event and uses `PLAYER_PRIMARY + PLAN_PREPARE_AND_EXECUTE` for each delayed shot, allowing The Wiz Plan handling.
- Verification: focused pipeline-mode, GeometryHelper, Bullet/Laser/Wiz Pattern, Cricket's Body, and Brimstone tests passed; offline `compileJava` passed; `git diff --check` clean apart from pre-existing line-ending warnings.

## Session: 2026-08-25

### Phase 0: Review and Scope Capture
- **Status:** complete
- **Actions taken:**
  - Read the `planning-with-files` instructions and existing planning layout.
  - Inspected current `AttackContext`, `AttackType`, `AttackPipeline`, `AttackPlan`, `AttackRequest`, `AttackPattern`, Bullet/Laser/Brimstone/CSection implementations, trigger event handlers, and relevant call sites.
  - Compared the initial attack-construction refactor commit to identify newly introduced fields and migration residue.
  - Discussed and confirmed the intended Builder + Pattern + runtime-state direction with the user.
  - Created this subproject plan and findings record.
- **Files created/modified:**
  - `.planning/attack-context-refactor/task_plan.md` (created)
  - `.planning/attack-context-refactor/findings.md` (created)
  - `.planning/attack-context-refactor/progress.md` (created)

### Phase 1: Stabilize AttackContext
- **Status:** complete
- **Actions taken:**
  - Replaced duplicated constructor initialization with `AttackContext.Builder` and `toBuilder()`.
  - Resolved damage, range, and speed during construction with safe finite/default handling.
  - Made trajectory storage private and immutable to callers; added trigger snapshot helpers and controlled color setter.
  - Removed generic `attackSequenceIndex`; retained the runtime field on `LaserProjectile` and moved Brimstone's counter to its scheduler local state.
  - Migrated split reference/child scalar updates to builder-created contexts.
  - Migrated Bullet/Laser trajectory and color reads to accessors.
  - Migrated all production construction call sites to the Builder; removed the unused legacy long constructors because this mod is unreleased.
  - Added focused `AttackContextBuilderTest` coverage.
  - Compile verification succeeded with Gradle-managed JDK 17.
  - Plain Gradle test execution was blocked by Forge registry bootstrap requirements; the focused test class is retained but disabled until a Forge-launched test fixture is available.
- **Files created/modified:**
  - `src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/AttackContext.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/BulletAttack.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/LaserAttack.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/BrimstoneAttack.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/registries/split_module/SplitExecutor.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/registries/split_module/impl/ParasiteSplitModule.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/registries/split_module/impl/CricketsBodySplitModule.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/registries/trigger_module/impl/normal/MomsEyeshadow.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/registries/trigger_module/impl/normal/MomsPerfume.java`
  - `src/main/java/net/luojiuoscar/isaac_disaster/registries/trigger_module/impl/normal/TheCommonCold.java`
  - `src/test/java/net/luojiuoscar/isaac_disaster/registries/attack_type/AttackContextBuilderTest.java`

## V1 Acceptance Review
- Builder and `toBuilder()` are available; no legacy public long constructors remain.
- Damage, range, and speed are resolved at creation; invalid range/speed fall back safely and remain finite/positive.
- Generic `attackSequenceIndex` was removed from `AttackContext`; Brimstone sequence state remains in `LaserProjectile` and the Brimstone scheduler.
- Trigger remains intentionally mutable for preparation; `copyTrigger()` and controlled trigger helpers were added.
- Trajectory maps are private immutable snapshots; split sequences are copied and initialized.
- Scalar setter call sites were migrated to builder-derived contexts; geometry setters and pipeline behavior remain unchanged for V1.

## Verification Results
| Check | Result |
|-------|--------|
| `./gradlew.bat compileJava --no-daemon` with Gradle JDK 17 | PASS |
| Focused `AttackContextBuilderTest` | COMPILES; runtime disabled because plain worker cannot bootstrap Forge registries |
| `git diff --check` | PASS |
| Scan for generic Context scalar setters/sequence index/direct trajectory field access | PASS; remaining matches are runtime entity setters or expected runtime sequence state |

## Session: 2026-08-26

### Follow-up cleanup
- Removed the two unused public long-parameter `AttackContext` constructors. This is an unreleased mod, so no deprecated compatibility facade is needed.
- Rechecked Brimstone sequence propagation without changing implementation. Scheduled shots retain their local counter; exact-spawn split children still need a Brimstone-only runtime descriptor or request metadata in V3 so their originating sequence is not collapsed to `0`.
- Re-ran `compileJava` with Gradle-managed JDK 17: PASS.
- Re-ran `git diff --check`: PASS.

### Brimstone single-context sequence execution
- Updated `BrimstoneAttack.shoot` so one Context schedules one 13-shot sequence without mutating that base Context. Each callback creates a fresh builder-derived shot Context, samples shooter position, and updates the main axis only for controllable attacks.
- Added public `BrimstoneAttack.shootSingle(AttackContext, int)` as the sole single-laser execution path used by both scheduled Brimstone shots and Brimstone split children.
- Updated `SplitExecutor` to execute RAW Brimstone children directly through `shootSingle`, passing the parent `LaserProjectile` runtime sequence index. Other split child types retain the existing `AttackExecutor.perform` path.
- Confirmed `ScheduledFuncHelper` supports concurrent tasks with the same player and schedule type when `override` is false, so each spread Context retains an independent 13-shot sequence.
- Verified `compileJava` and `testClasses` with Gradle-managed JDK 17. The focused runtime behavior still requires a Forge-launched manual test because plain JUnit cannot bootstrap Forge registries.

## Test Results
### Cricket's Body block-hit plane alignment
- Added a runtime lastBlockHitNormal snapshot to LaserProjectile when a laser or brimstone hits a block.
- Reworked CricketsBodySplitModule so BLOCK splits first project the incoming direction onto the impact plane, then build the 4-way spread inside that plane with a bounded 0..45 degree spin.
- Kept ENTITY and END_OF_LIFE behavior on the legacy path.
- Added pure vector tests for floor hits, wall hits, and perpendicular fallback handling.
- Verified compileTestJava, the focused test class, and git diff --check with Gradle-managed JDK 17.
| Test | Input | Expected | Actual | Status |
|------|-------|----------|--------|--------|
| Static source review | Attack construction and pipeline call chains | Identify contracts, risks, and migration boundaries | Findings recorded; no production changes made | PASS |
| Planning session catch-up | `python ... session-catchup.py` | Recover prior planning state | `python` unavailable on PATH; existing files inspected directly | LIMITED |
| OCR delegate preview | `ocr delegate preview --format json` | Produce reviewable file inventory | No usable inventory returned | LIMITED |

## Error Log
| Timestamp | Error | Attempt | Resolution |
|-----------|-------|---------|------------|
| 2026-08-25 | `python` is not recognized by PowerShell | 1 | Did not repeat; inspected existing planning files directly and recorded limitation. |
| 2026-08-25 | OCR delegate preview did not provide a usable inventory | 1 | Completed targeted manual source review and recorded limitation. |

## 5-Question Reboot Check
| Question | Answer |
|----------|--------|
| Where am I? | V1 AttackContext implementation complete; V2 is pending. |
| Where am I going? | V2 common Pattern migration, V3 Brimstone, V4 pipeline, V5 freeze/cleanup. |
| What's the goal? | Stable creation-time Context snapshots with Pattern-driven attacks and separated runtime state. |
| What have I learned? | See `findings.md`; key decisions are Builder, Pattern, per-step Brimstone contexts, and runtime-only sequenceIndex. |
| What have I done? | Implemented and verified V1 Builder/scalar/snapshot changes and recorded the results above. |

## Session: 2026-08-26 V2 main-axis pattern migration
- **Status:** complete
- **Actions taken:**
  - Renamed AttackPatternContext to main-axis terminology and migrated pattern callers.
  - Added shared main-axis pattern support plus Bullet, Laser, and Wiz pattern implementations.
  - Routed BulletAttack and LaserAttack through the new pattern classes.
  - Refactored TheWizAttackPlan to replace base-context lists through the Wiz helper instead of mutating them in place.
  - Changed AttackContext.copy() into a true snapshot clone.
  - Added focused regression tests for Bullet, Laser, Wiz, and the shared main-axis helper.
  - Verified compileJava and the focused pattern test set with Gradle-managed JDK 17.
  - Verified git diff --check; remaining output was only pre-existing LF/CRLF warnings in dirty files.
- **Files created/modified:**
  - src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_pattern/AttackPatternContext.java
  - src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_pattern/impl/MainAxisPatternSupport.java
  - src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_pattern/impl/BulletAttackPattern.java
  - src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_pattern/impl/LaserAttackPattern.java
  - src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_pattern/impl/WizAttackPattern.java
  - src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/AttackContext.java
  - src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/BulletAttack.java
  - src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/LaserAttack.java
  - src/main/java/net/luojiuoscar/isaac_disaster/registries/ability_effect/impl/normal/TheWizAttackPlan.java
  - src/test/java/net/luojiuoscar/isaac_disaster/registries/attack_pattern/impl/PatternTestSupport.java
  - src/test/java/net/luojiuoscar/isaac_disaster/registries/attack_pattern/impl/MainAxisPatternSupportTest.java
  - src/test/java/net/luojiuoscar/isaac_disaster/registries/attack_pattern/impl/BulletAttackPatternTest.java
  - src/test/java/net/luojiuoscar/isaac_disaster/registries/attack_pattern/impl/LaserAttackPatternTest.java
  - src/test/java/net/luojiuoscar/isaac_disaster/registries/attack_pattern/impl/WizAttackPatternTest.java

## Session: 2026-08-26 V3 Brimstone sequence helper
- **Status:** complete
- **Actions taken:**
  - Added a dedicated BrimstonePattern helper to own the explicit 13-shot sequence boundary.
  - Moved Brimstone shot snapshot creation into the helper so each callback derives a fresh copied Context with the current spawn position.
  - Kept controllable Brimstone rotation updates while preserving the copied spread offsets.
  - Left AttackPipeline semantics and shootSingle runtime behavior unchanged.
  - Added focused BrimstonePatternTest coverage and reused the existing pattern test support across packages.
  - Verified compileJava, the focused test set, and git diff --check with Gradle-managed JDK 17.
- **Files created/modified:**
- src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/BrimstonePattern.java
- src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/BrimstoneAttack.java
- src/test/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/BrimstonePatternTest.java
- src/test/java/net/luojiuoscar/isaac_disaster/registries/attack_pattern/impl/PatternTestSupport.java

## Session: 2026-08-26 V4 Brimstone single-context sequence
- **Status:** complete
- **Actions taken:**
  - Collapsed the Brimstone helper back into BrimstoneAttack so scheduled shots reuse one working context.
  - Refresh the shared shot context in place before each emission, updating only spawn position and controllable rotation.
  - Removed the standalone BrimstonePattern helper and replaced its coverage with BrimstoneAttack behavior tests.
  - Kept shootSingle as the runtime-sequence entrypoint for split children and direct single-laser emission.
- **Files created/modified:**
  - src/main/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/BrimstoneAttack.java
  - src/test/java/net/luojiuoscar/isaac_disaster/registries/attack_type/impl/BrimstoneAttackTest.java

## Session: 2026-08-27 V2/V3 follow-up main-axis optimization
- **Status:** complete
- Replaced `AttackContext`'s `xRot`, `yRot`, and offset state with one validated, normalized `Vec3 mainAxis`.
- Moved direction/rotation conversion, axis rotation, lateral-axis selection, and impact-plane projection to `helper/GeometryHelper`.
- Added `AbstractAttackPattern` for Pattern-specific Context snapshot cloning and main-axis replacement; removed the obsolete pattern support helpers.
- Migrated Bullet, Laser, Wiz, Ring, Semicircle, Parasite, Brimstone, Shoop Da Whoop, Context construction call sites, TearBullet, LaserProjectile, and Cricket's Body to the main-axis contract.
- Replaced the duplicate `AttackType.rotateAroundAxis` implementation with `GeometryHelper` usage.
- Verified with the Gradle-managed Java 17 runtime in offline mode:
  - `compileJava`: PASS
  - focused Bullet/Laser/Wiz/Cricket's Body/Brimstone tests: PASS
  - `testClasses`: PASS
- The first online compile attempt was blocked by MCPRepo connection timeout; offline verification succeeded from the local Forge cache.

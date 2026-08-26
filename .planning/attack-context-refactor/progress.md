# Progress Log: AttackContext Refactor

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

# BulletState Responsibility Split

## Goal

继续拆分 `BulletState` 的职责，降低文件复杂度，同时保持 `IBulletObject`、轨迹运动、碰撞、追踪、分裂和网络快照行为不变。

## Current Phase

Phase 1 complete; Phase 2 pending.

## Phases

### Phase 1: Existing state extraction

- [x] Extract `BulletProfile` for immutable visual and collision properties.
- [x] Extract `BulletAttackIdentity` for owner, shooter and attack type identity.
- [x] Extract `BulletCollisionState` for hit memory and cooldown state.
- [x] Extract `BulletTrackingState` for homing and control state.
- [x] Extract `BulletTrajectoryState` for trajectory runtime, motion snapshot and diagnostics.
- [x] Remove confirmed unused trajectory façade methods.

### Phase 2: Base motion state

- [ ] Map every position, velocity, acceleration, age, lifetime, traveled, range and alive access.
- [ ] Introduce a private `BulletMotionState` (name may be refined after call-graph review).
- [ ] Move only base-motion arithmetic and lifecycle counters; keep orchestration in `BulletState`.
- [ ] Preserve range-limited versus lifetime-limited termination and collision truncation semantics.
- [ ] Run focused movement, trajectory and collision tests before proceeding.

### Phase 3: Trigger and split state

- [ ] Audit `SplitSequence`, `SplitTriggerCounts` and `CompositeTrigger` ownership.
- [ ] Extract a small trigger/split state holder only if it removes real coupling.
- [ ] Preserve split copying, trigger consumption and `IBulletObject` accessors.

### Phase 4: Façade and builder cleanup

- [ ] Keep `IBulletObject` methods stable while reducing duplicate delegation in `BulletState`.
- [ ] Evaluate the large nested `Builder` separately; do not break its public construction API without evidence.
- [ ] Narrow internal helper visibility after a complete repository call-graph scan.

### Phase 5: Verification and delivery

- [ ] Run Java 17 compile and tests when the runtime is available; otherwise record the verified runtime.
- [ ] Run focused projectile, trajectory, network and collision tests after each extraction.
- [ ] Run the full JUnit suite and relevant Forge GameTests.
- [ ] Run `git diff --check` and verify no unrelated files or staging state were changed.

## Invariants

- `BulletState` remains the façade consumed by `IBulletObject` and managers.
- `slot` and `generation` remain manager-owned identity, not projectile motion state.
- `getAttackContext()` continues to merge hit blocks and the current trajectory snapshot.
- `noGravity()` remains unchanged.
- `currentTrajectoryMotion` is retained internally until collision processing and range accounting finish.
- State holders own mutable data; callers receive defensive snapshots where the existing contract requires them.

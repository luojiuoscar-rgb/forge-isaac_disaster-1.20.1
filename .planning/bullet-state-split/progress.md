# Progress

## 2026-09-26

### Phase 1: Existing state extraction

- Completed the remaining `BulletState` references to the new `BulletTrajectoryState` façade.
- Preserved trajectory suspension, runtime advancement, motion capture, collision truncation, path-cost retention, range checks, redirects and attack-context snapshots.
- Removed uncalled `currentTrajectoryMotion()`, `trajectoryProgressRate()`, `applyTrajectoryMotion(...)` and the unused `BulletState.finite(Vec3)` helper.
- Restricted `BulletTrajectoryState` construction to the package and added a non-null runtime check.
- Verification: Java 23 `compileJava`, focused trajectory/state/network tests, and full JUnit all passed.
- `git diff --check` reported no whitespace errors; only existing line-ending warnings.

## 2026-09-26: trajectory result contract cleanup

- Removed the unused `orientation` value and single-value `ControlMode` from `TrajectoryMotion`.
- Updated all primary/offset trajectory producers and the evaluator to use only the consumed motion fields.
- Removed the unused `anchor` parameter from `TrajectoryFrame` construction while retaining its launch-axis and laser classification behavior.
- Verification: Java 23 `compileJava` passed after one transient ForgeGradle cache/file-lock failure; focused trajectory, state and network tests passed.
- No trajectory evaluator performance changes were made; those remain deferred until the `BulletState` split is complete.

### Phase 2: Base motion state

- Completed the base-motion extraction in `BulletMotionState`.
- Moved position history, velocity/acceleration, base speed, age/lifetime, traveled distance, range, alive state, low-speed validation, ordinary/trajectory advancement, snapshot restoration and correction handling.
- Kept `BulletState` responsible for trajectory suspension, cooldown ordering, dynamic range-mode selection, trajectory runtime advancement, charged-distance replacement, and the public `IBulletObject` facade.
- Kept `slot`/`generation` in `BulletState` because they belong to `BulletManager` identity management.
- Java 23 `compileJava` passed.
- Java 17 focused tests passed: `BulletStateTest`, `BulletStreamTest`, `TrajectorySamplingTest`, `TrajectoryNetworkStateTest`, `TinyPlanetTrajectoryTest`, `MyReflectionTrajectoryTest` and `GravityTrajectoryTest`.
- Java 17 full JUnit suite passed after the extraction.
- `git diff --check` passed; line-ending notices are pre-existing workspace formatting warnings.
- No staging operations were performed.

### Phase 3: Trigger and split state

- Pending.

## 2026-09-26: trajectory context input consolidation

- Removed the unused `age` and `trajectoryDistance` fields from `TrajectoryContext`.
- Added immutable `TrajectoryContext.Input` for position, current velocity, base velocity, acceleration and step duration.
- Updated the evaluator, trajectory modules and focused tests to use the grouped input without changing trajectory output semantics.
- Verification: Java 23 `compileJava test` passed; `git diff --check` reported no whitespace errors, only existing line-ending warnings.

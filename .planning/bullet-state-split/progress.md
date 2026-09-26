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

- Not started.

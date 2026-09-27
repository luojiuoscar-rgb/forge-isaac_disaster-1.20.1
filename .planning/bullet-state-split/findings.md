# Findings

## Current structure

- `BulletState` now composes `BulletProfile`, `BulletAttackIdentity`, `BulletCollisionState`, `BulletTrackingState` and `BulletTrajectoryState`.
- `BulletState` still owns base position/velocity/lifetime/range/alive data, the façade methods, and the nested `Builder`.
- `BulletTrajectoryState` owns the per-bullet `TrajectoryRuntime`, current motion snapshot, trajectory progress and diagnostic counters.
- `TrajectoryRuntime` remains mutable by design because managers, evaluators, trajectory modules and network restoration update it during a projectile step.

## Candidate next split

The next safe extraction is base motion state: position, previous position, velocity, acceleration, base speed, age, lifetime, traveled distance, range and alive status. The extraction should move arithmetic and simple lifecycle transitions first, while `BulletState` keeps ordering between tracking, trajectory evaluation and collision resolution.

`slot` and `generation` should remain in `BulletState` for now because they are assigned and reclaimed by `BulletManager`, not part of the projectile's physical motion.

The nested `Builder` is large but is also the construction API used by tests and runtime adapters. It should be considered only after the data holders are stable; moving it early would create compatibility noise without reducing runtime coupling.

## Base motion extraction result

- `BulletMotionState` now owns position history, velocity and acceleration, base speed, age/lifetime, traveled distance, range, alive state, low-speed validation, ordinary/trajectory advancement, snapshot restoration and visual correction.
- `BulletState` remains the orchestration facade. It decides whether the current projectile is range-limited from `TrajectoryRuntime.specs()`, suspends trajectory state before ordinary physics, ticks collision cooldowns, and applies trajectory range accounting after the motion state advances.
- `slot` and `generation` remain in `BulletState`; they are manager identities rather than physical state.
- The public `BulletState.MIN_VALID_SPEED` constant remains as a compatibility facade for existing tests and callers while the implementation constant lives in `BulletMotionState`.
- `BulletMotionState` has no trajectory or manager imports. It receives the range-limited decision as an argument so it does not create a dependency cycle with `TrajectoryRuntime`.

## Known boundaries

- Do not change trajectory formulas as part of this structural work.
- Do not change tracking suspension, trajectory snapshot copying, split inheritance or collision path accounting.
- Do not clean unrelated dirty files or operate the Git index.

## Verification already available

- Java 23 `compileJava` passed after the motion-state integration.
- Java 17 focused movement, trajectory, network and collision tests passed: `BulletStateTest`, `BulletStreamTest`, `TrajectorySamplingTest`, `TrajectoryNetworkStateTest`, `TinyPlanetTrajectoryTest`, `MyReflectionTrajectoryTest` and `GravityTrajectoryTest`.
- Full JUnit should be rerun after later state extractions.

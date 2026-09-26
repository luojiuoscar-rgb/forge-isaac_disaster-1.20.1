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

## Known boundaries

- Do not change trajectory formulas as part of this structural work.
- Do not change tracking suspension, trajectory snapshot copying, split inheritance or collision path accounting.
- Do not clean unrelated dirty files or operate the Git index.

## Verification already available

- Java 23 `compileJava` passed after the trajectory-state integration.
- Focused `BulletStateTest`, `TrajectorySamplingTest` and `TrajectoryNetworkStateTest` passed.
- Full JUnit suite passed in the current workspace with Java 23.
- The configured Java 17 path is currently unavailable in this environment and must be retried before a Java 17 claim is made.

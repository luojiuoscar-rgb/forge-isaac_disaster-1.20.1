# Trajectory Module-Owned State

## Goal

Replace the mixed `TrajectoryRuntimeState` and `TrajectoryPathState` bags with
state owned by each trajectory module. Preserve existing motion formulas,
composition, tracking suspension, split inheritance, and network behavior.

## Status

- [x] Define `TrajectoryState` with copy and lifecycle hooks.
- [x] Give every registered `TrajectoryModule` its own state type, factory,
  type check, and network codec.
- [x] Store per-projectile states by module ID in `TrajectoryRuntime`.
- [x] Copy, rebase, suspend, restore, and serialize each module state through
  the owning module; keep shared kinematics in `TrajectoryRuntime`.
- [x] Migrate the primary and offset evaluators, concrete modules, clients,
  projectiles, and tests; remove both mixed-state classes.
- [x] Guard unknown module IDs and mismatched state types.
- [x] Verify with Java 17 JUnit, Forge GameTest, source-reference scan, and
  `git diff --check`.

## Boundaries

Registry module instances define behavior and codecs. Mutable phase/path data
belongs to individual projectiles. Gravity has an immutable empty state and may
share its singleton. Existing formulas and visible effects are outside this
state-ownership refactor; manual client visual acceptance remains separate.

No compatibility layer or old-save migration is required for this unpublished
mod. Do not stage or discard unrelated working-tree changes.

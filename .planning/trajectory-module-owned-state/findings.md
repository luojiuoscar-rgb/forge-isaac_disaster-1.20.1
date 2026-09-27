# Findings

## State ownership

`TrajectoryState` supplies `copy()` and optional `suspend()`, `resume()`, and
`rebase()` hooks. Each concrete module declares `TrajectoryModule<State>` and
implements `createState()`, `stateClass()`, `writeState()`, and `readState()`.
The module registry entry is a behavior/codec definition, not a store for
per-shot phase data.

`TrajectoryRuntime` belongs to one projectile. Its state map associates each
module ID with that module's state. Common launch geometry, anchor, distance
clock, composition residuals, suspension, and `TrajectoryKinematics` remain
runtime-wide because they describe the composed projectile rather than one
module. The state map is mutable for the execution and restoration path; its
values are copied for snapshots and split children.

The module boundary checks state type before evaluation and network writing.
`castState(null)` creates a default state, but the evaluator normally first
uses `ensureState` or `computeIfAbsent` to store that state in the runtime.
Unknown module IDs are ignored during motion evaluation; a snapshot cannot
decode an unknown module payload without its registered codec and fails clearly.

## Verification and limits

On 2026-09-25, Java 17 full JUnit passed: 152 tests, zero failures/errors.
Forge GameTest passed all 21 required tests. `git diff --check` passed, and
production/test/GameTest sources contain no references to either removed mixed
state class. This is server and automated evidence, not an in-client visual
comparison of every trajectory combination.

The original plan file was already staged before this planning-location
correction. The index is intentionally untouched; a staged-only commit may
still contain that old path until the user stages the final working-tree state.

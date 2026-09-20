# 2026-09-11: root types and trajectory rewrite rules

## Four-worm implementation

Implemented independent worm classes, primary/offset separation, synchronized
kinematics, phase-preserving rebase, Hook stages and range-free crossings,
per-segment collision costs, and finite laser budgets. Removed BuiltinTrajectory
and the obsolete horizontalizing frame helpers. New tests replace finite-ring
and horizontal-laser expectations rather than preserving incorrect behavior.
Full JUnit passed before final collision fix. Server generated 864 samples across
four variants, three directions, and four worms. One GameTest exposed a real
laser collision gap: boundary-aligned samples could start inside an expanded
block shape and skip it. Reused clipExpandedTarget to include initial overlap;
final regression initially exposed a second real issue: legal zero-charge Hook
lateral motion was treated as laser no-progress. The guard now accepts actual
position progress, while retaining its finite budget and zero-motion break.
Final regression run is pending. Existing three JUnit skips remain unchanged.

Implemented two root types with separate variants, retained one trigger per effect,
added registry/index/cache and freeze-time rewrites, and replaced ordinary Reflection
steering with integrated force. Final Java 17 test + runGameTestServer --offline
passed: 127 JUnit cases (124 passed, 3 existing skipped) and all 12 Forge GameTests.
Validated external registration, indexed rewrites, stacks/order, cyclic termination,
copy/freeze/direct execution, removed runtime states, four variants and boomerang
deceleration/reversal. Client visual/feel acceptance remains pending.
Sandbox helper failed; located current Codex apply-patch executable. Automatic review
rejected broad directory rewriting; used reviewed explicit file lists and line patches.
No staging/reset or cleanup of unrelated changes.

# Current Progress

## Distance-weighted Reflection - 2026-09-10

- Ordinary Reflection now uses continuous attraction with no return/orbit phase.
  Near-target strength decreases to zero. At 12 blocks it is 10% of maximum,
  then smoothly increases to maximum at 18 blocks. Speed stays unchanged.
- Self-fired shots target live owner position; other shooters target the actual
  spawn origin. Null references do not count as shooter=owner.
- Snapshot/copy state carries the target policy so client prediction without
  entity references follows the same rule using its synchronized owner anchor.
- Latest target rule supersedes the fixed-origin-only rule in the history below.
- Java 17 verification: 121 JUnit cases, 118 passed / 3 existing skipped;
  all 10 Forge GameTests passed, including real owner/shooter selection and
  copied/decoded state prediction without client entity references.

## Own spawn, projectile family, horizontal Reflection - 2026-09-10

- Ordinary/fetus origin now defaults to the final builder position, including
  actual spawn offsets. Explicit origins remain for network snapshot restoration.
- Laser origin is its own constructor position. Copied laser Reflection state
  rebases to a child's origin and requests a smooth rejoin without resetting phase.
- AttackType declares BulletSourceType; preparation copies it into AttackContext
  before triggers attach. Unknown types stay null; copies/freeze preserve the enum.
- Reflection now targets only its own origin, prioritizing yaw over pitch at the
  existing 0.15 rad/tick budget. Player movement cannot drag its target downward.
- Added automated coverage for builder/snapshot origins, player independence,
  horizontal-first steering, four-family context copies, real spawn offsets,
  laser child origins and family independence from attack sequence indices.
- Final Java 17 `test runGameTestServer --offline`: 120 JUnit cases,
  117 passed / 3 existing skipped; all 9 Forge GameTests passed.

## Gravity and Reflection tuning - 2026-09-10

- Increased ordinary Reflection turn rate from 0.025 to 0.15 radians/tick.
- Extracted GravityTrajectoryModule, removed the old built-in formula, preserved
  Ipecac trigger attachment and explosion effects. Lasers produce no gravity intent.
- Added range termination/final-step clipping and fixed double advancement when
  relative controllers compose. Gravity follows ordinary Reflection.
- Verified reversal within range, accumulated gravity for both ordinary families,
  laser invariance, ordering, and resume from steering velocity.
- Initial full run exposed a short-lifetime laser test fixture; corrected its
  lifetime to model the real laser backend. No tests removed.
- Java 17 `test runGameTestServer --offline`: 116 JUnit cases, 113 passed /
  3 existing skipped; all 8 Forge GameTests passed.
- Client feel and remaining worm combinations still require visual acceptance.

## Status - 2026-09-10: My Reflection

- [x] Reproduce old ordinary weak-turn and laser range-charge defects with
  two failing behavior tests before replacing the implementation.
- [x] Register separate ordinary/fetus and laser/Brimstone modules, dispatched
  by the existing My Reflection TriggerModule. Remove the old shared formula.
- [x] Implement current-player weak steering, speed/range bounds, smooth laser
  left-entry/U-turn/return, original 3D reverse direction and free prelude.
- [x] Serialize fixed launch plane and phase; preserve pause/recovery state.
- [x] Sequence Planet before Reflection and account for return to the original
  firing point. Consume every sampled laser segment for collision.
- [x] Java 17 final `test runGameTestServer --offline`: 111 JUnit cases,
  108 passed / 3 existing skipped; all 7 Forge GameTests passed.
- [x] Correct stale Planet test IDs/lifetime fixtures and replace the obsolete
  horizontal laser-height assertion with inclined-plane coverage.
- [ ] Client visual acceptance and broader combinations with unfinished modules.

No staging, unrelated cleanup or compatibility layer. The sandbox execution
helper failed during setup; approved elevated commands and the native Codex
apply-patch entry point were used. Reports below predate this implementation.

## Historical Milestone - 2026-09-09

- [x] Confirmed the TrajectoryModule attachment architecture and all seven
  trajectory requirements, range rules, steering behavior, and scalar defaults.
- [x] Saved the current contract in findings.md and actionable checklists in
  task_plan.md.
- [x] Removed obsolete investigation history and repeated descriptions from
  active planning documents at the user's request.
- [x] Implement TrajectoryModule/ModTrajectoryModules and TrajectorySequence;
  attach all seven effects through TriggerModule before context freezing.
- [x] Remove PlayerAbility trajectory storage/NBT and StatManager.addTrajectory;
  migrate attack, runtime, network, client, and debug consumers. No compatibility.
- [x] Verify stack/order/freeze and copy isolation, ability lifecycle and
  save/reload, attack families, execute-only behavior, packet roundtrip and
  client snapshot receipt. Latest Java 17 full test run: 98 JUnit tests,
  95 passed / 3 existing skipped; all 5 Forge GameTests passed, including
  400-step Tiny Planet samples for four projectile families and three launch axes.
- [x] Implement Tiny Planet as an independent subclass of abstract TrajectoryModule:
  continuous player-centered orbit, launch-pitch height, laser one-revolution
  entry/exit and original 3D axis, independent progress/range cost, pause/rejoin,
  serialized path memory, client anchor updates, and subdivided collision sweeps.
- [ ] Implement the remaining six trajectory repairs and full shared composition.
- [ ] Complete movement, combination, collision, and in-client visual acceptance.

Latest correction: both families launch along the view axis for 1 block before
the 2-block entry transition; orbit entry is in front rather than to the right.
Ordinary bullets use speed-bounded height/radius correction and tangential motion
from their actual position. Moving the player alone never moves the projectile.
Updated the previous instantaneous-follow assertions to bounded pursuit and
eventual convergence; the two reported regressions failed before the fix.
Focused tests, full JUnit suite, and Forge GameTests all pass. Client visual
verification remains pending.

Latest laser fix: reproduced and corrected zero charge caused by cancellation
of nearly equal cumulative distances at the range endpoint. Added no-progress
and module-aware finite-work guards. Real laser/Brimstone stepping tests cover
repeated homing interruptions, resumed orbit, range exhaustion, and an injected
stalled controller. Both families now map pitch +/-60 degrees to height 0.1..0.9.
No live stack survived the reported forced game shutdown; log evidence and
the reproducible loop are distinguished in findings.md.

Tiny Planet changes its formula and necessary runtime interfaces; the other six
formulas remain unchanged. GameTests use a test-only source set and build/gametest world.
Existing sampling reports do not validate the new motion requirements.
No staging or cleanup of unrelated work is part of this migration.

## Worm pitch continuity and ring tuning - 2026-09-11

- Reworked TrajectoryKinematics.orient to parallel-transport the previous side axis, project it onto the new forward direction, and retain its sign across pitch changes. This prevents Hook/Wiggle/Ring/Ouroboros offsets from mirroring when player xRot crosses the horizontal plane.
- Increased Ring Worm radius to 2 blocks (+1 per amplifier) and frequency to one revolution per 4 blocks of primary travel.
- Java test suite passes: 132 tests, 3 existing skips. Forge/client visual acceptance remains pending.

## Worm rollback and fixed launch basis - 2026-09-12

- Reverted the previous turn's Ring Worm radius/frequency increase; Ring Worm is back to radius `1 + 0.5 * amplifier` and one revolution per 8 primary-distance blocks.
- Applied the requested tuning to Wiggle Worm instead: amplitude `1 + 0.5 * amplifier`, period 4.
- Added per-projectile launch-side and launch-up vectors to `TrajectoryKinematics`, initialized once from the launch axis and serialized/copied with runtime state. Primary direction updates no longer rebuild worm offset axes.
- Hook, Wiggle, Ring, and Ouroboros now read the fixed launch basis. Added a phase-distance substep cap for smooth curved sampling.
- Focused worm tests and full JUnit pass (133 tests, 3 existing skips). Forge GameTest still has the pre-existing `laserloopterminatesacrosshominginterruptions` failure at the range-end no-progress guard; no tests were removed or weakened.
## Fixed worm launch coordinates and corrected tuning - 2026-09-12

- Fixed launch forward/right/up axes are now stored in `TrajectoryKinematics`, copied and serialized with each projectile. Worm offsets no longer rebuild their basis from current primary velocity, so pitch sign changes and later steering cannot mirror them.
- Ring Worm reverted to the original circular parameters (`1 + 0.5 * amplifier`, period 8). Wiggle Worm now carries the increased settings (amplitude `1 + 0.5 * amplifier`, period 4). Sampling also caps primary-distance phase increments for smooth curves.
- Full JUnit passes: 133 tests, 3 existing skips. Forge GameTest still reports the previously known `laserloopterminatesacrosshominginterruptions` failure at the range-end no-progress guard; this turn did not alter that guard or remove the test.
## 2026-09-13 laser performance remediation

- Root cause confirmed in `LaserAttack.traceLaser`/`stepLaser`: a 0.1-block laser step combined with curved trajectory subdivision performed trajectory evaluation, particle interpolation, block shape traversal, and entity queries for every fine segment on the server thread. High-rate firing multiplied this synchronous work and could starve ordinary bullet ticks, matching the reported suspended-speed symptom. No unbounded loop or stack overflow appeared in the reproduced logs.
- Added bounded trace termination (`RANGE`, `BLOCK`, `NO_PROGRESS`, `WORK_LIMIT`, `INVALID_INPUT`) and a hard finite step/segment budget. Range completion normalizes residual floating-point error with epsilon, fixing the reproduced `15.99999999999999 < 16` failure.
- Added `LaserPath` to merge only collinear path edges with equal cost density; curved edges and charged/free boundaries remain separate for collision correctness. Particle emission is now sampled by cumulative path distance (0.2 blocks), independent of collision subdivision.
- Reworked free-distance accounting to deduplicate module IDs and subtract completed primary-path distance. Hook offset stages do not inflate laser prelude budget when another primary controller is active.
- Added Java and Forge coverage for path compaction, particle spacing, finite work, ordinary bullet motion after a laser burst, terminal epsilon, and combined Planet+Ouroboros laser paths.
- Java 17 focused JUnit passed; Forge GameTest passed all 18 required tests. Client visual acceptance remains pending.
- Removed confirmed trajectory automation artifacts and old diagnostic notes from `.planning/trajectory-runtime-validation/` and `codex/`; retained the three canonical planning records. Unrelated dirty files and staged changes were left untouched.

## 2026-09-13 collision-cache boundary correction

- The first post-optimization GameTest run exposed one false performance regression: `LaserProjectile.invalidateCollisionCache()` cleared entity broad-phase candidates after callbacks, so one trace step could issue multiple synchronous entity queries.
- The cache now invalidates block-shape data immediately but retains the entity candidate snapshot until `beginCollisionStep()`; per-entity alive/friendly/damaged filtering still runs against current state. This enforces the intended one broad-phase query per step without reducing collision sampling.
- Java 17 compilation and the full Forge GameTest run pass after the correction: all 18 required tests passed. The pressure sample reported 32 shots, about 1.03 s, 79,328 collision edges, 7,488 particles, and 12,768 entity queries across the full burst.

## 2026-09-14 AttackContext runtime consolidation

- Reproduced the standalone BulletState launch-axis mismatch with a failing test;
  the old builder kept an X axis despite a different final velocity.
- Replaced eight loose runtime fields and builder setters on AttackContext with one
  immutable optional inheritance snapshot. Added per-projectile TrajectoryRuntime
  through IBulletObject; configuration-only copies release consumed parent snapshots.
- Centralized state copying and network encoding; removed seed plumbing and the fake
  runtime_kinematics module entry. Kinematics is allocated only when needed.
- Child construction rebases launch geometry while preserving phases and selected
  Planet direction; network restore retains original launch data. Both projectile
  families now derive their frame from final generation position and direction.
- Client anchor resolution now follows the same shooter as authority. Corrections
  restore all runtime clocks/states together rather than mixing old and new counters.
- Final verification: Java 17 `test runGameTestServer --offline` succeeded;
  140 JUnit cases (137 passed, 3 existing skipped), all 19 Forge GameTests passed.
  Laser burst sample: 32 shots, 79,328 collision edges, 7,488 particles, 12,768
  entity queries. Timing is environment-dependent; no new client visual claim.
- Scanned sources for removed field APIs/seed/fake state ID; no stale references.
  No staging, reset, commit or unrelated cleanup was performed in this refactor.

## 2026-09-20: AttackContext audit and extensible attack identity

- Audited actual source, `daa72a3`, and the context/size plans. Preserved intentionally mutable trigger/split getters and damage-dependent Builder size recalculation; documented these boundaries and added regression coverage. Removed the unused direct-size-override flag.
- Removed both type enums. AttackType defines concrete/root IDs; contexts bind the definition before Prepare and direct execution. Migrated projectiles, trajectory dispatch/rules, split requests, hit-event identity, debug spawning, spawn packets and client restoration. Fixed Technology 2 / Shoop concrete identity.
- Initial new API tests failed as expected before implementation. During migration, corrected compile errors and removed ID-only lookups that unnecessarily initialized Forge in plain JUnit. A full test run exposed a fixture that still assigned a separate runtime type over a differently typed context; updated it to retype the creation context and audited direct laser fixtures/debug spawning. No tests were deleted or disabled.
- Final Java 17 `test runGameTestServer --offline`: 147 JUnit cases, 144 passed / 3 existing skipped; all 21 Forge GameTests passed. Includes addon registration/root dispatch, HIGHEST-priority Prepare visibility, direct execution without reattachment, derived laser and cross-root split identities, packet/client restoration, deliberate trigger mutability and size derivation. Log: `build/attack-identity-final.log`.
- Source scan has no removed enum/API references; diff whitespace check passed. Existing staged work remains untouched. Client visual acceptance remains unverified.

# Trajectory Redesign Checklist

## Active: review remediation and legacy cleanup (2026-09-21)

- [ ] Make bounce redirects update the trajectory main direction without resetting module phase.
- [ ] Make client corrections atomically synchronize simulation position, traveled distance and runtime while retaining render interpolation.
- [ ] Roll back an unaccepted trajectory step before a canceled/redirected collision resumes from the contact point.
- [ ] Bound offset evaluation before allocating sampled paths; preserve collision precision for accepted work.
- [ ] Move primary ordering, range-cost merging and default-motion pausing out of concrete ID/class checks.
- [ ] Reduce module-state snapshot payload and narrow raw mutable runtime access without removing the intentional execution interface.
- [ ] Replace the global bullet entity-query AABB with local spatial queries and remove disabled telemetry/client/collision hot-path overhead.
- [ ] Remove confirmed dead trajectory APIs and fields, then run Java 17 JUnit and Forge GameTests.

Behavior constraint: preserve the current seven trajectory formulas and visible paths for normal supported stacks. Extreme work-limit termination must be explicit rather than silently lowering collision precision.
## Active: per-projectile TrajectoryRuntime (2026-09-14)

- [x] Remove loose trajectory runtime parameters from AttackContext and IBulletObject.
- [x] Centralize runtime ownership, immutable snapshots, copy/restore and network serialization.
- [x] Rebase child launch frames while inheriting progress; keep network restore exact.
- [x] Migrate server/client/laser/evaluator/debug callers and remove obsolete seed pipeline.
- [x] Verify launch frames, inheritance isolation, packet round trips and full Java 17 tests.

Verification: Java 17 full JUnit (137 passed, 3 existing skipped), 19/19 Forge GameTests.
Only runtime ownership changes; preserve trajectory formulas and module preparation.


## Active: laser performance and cleanup (2026-09-13)

- [x] Reproduce terminal-range failure and audit ordinary-bullet speed updates.
- [x] Bound laser work, deduplicate free-prelude budgets, and normalize terminal range.
- [x] Preserve collision samples; reuse per-shot data and sample particles by path distance.
- [x] Make Tiny Planet module dispatch explicit without changing its formulas.
- [x] Remove confirmed trajectory automation artifacts and unused implementation remnants.
- [x] Run Java 17 JUnit and Forge GameTests, including laser pressure/collision cases.
- [x] Record measurements and remaining client-only acceptance.

Ruling: work in the existing dirty checkout, preserve the index and unrelated changes.
The approved user plan overrides historical notes below. No asynchronous tracing,
reduced collision precision, compatibility layer, or unrelated build changes.

## Active: four worm modules

- [x] Separate primary kinematics from offsets; synchronize independent frame/state.
- [x] Implement Hook, Wiggle, Ring and Ouroboros classes; remove BuiltinTrajectory.
- [x] Compose actual sampled paths, preserve range and support Hook free lateral stages.
- [ ] Verify primary-module regressions, four variants, combinations, recovery and copies.
- [ ] Run full Java 17 JUnit/GameTests and server sampling; record client visual acceptance separately.

Approved: shared 3D geometry for both roots; offsets may increase actual speed.
No 3-block delay: 2-block smooth entry, Hook starts with a 2-block forward leg.
Hook lateral movement does not suspend other primary modules; other worm phases
advance only with actual primary distance. Preserve existing registry IDs.

## Current revision: root types, rules, and boomerang (2026-09-11)

- [x] AttackType declares concrete/root ResourceLocation IDs; closed type enums removed (2026-09-20).
- [x] Keep one TriggerModule per effect, dispatching by root type during preparation.
- [x] Add Forge TrajectoryRule registry, inverse index, bounded cache, and freeze-time resolution.
- [x] Add ordinary Planet + Reflection -> Planet, retaining Planet stacks.
- [x] Replace Reflection angle steering with distance-weighted velocity force and actual travel charging.
- [x] Full Java 17 verification: 127 JUnit cases (124 passed, 3 existing skipped), 12 Forge GameTests passed, including external rule registration.
- [ ] In-client visual acceptance of the new boomerang movement.

These requirements supersede older constant-speed Reflection steering notes below.

## Scope

Current approved requirements are in findings.md. Module attachment/storage
migration is implemented; motion redesign and visual acceptance remain pending.
Ordinary/fetus bullets share one behavior family; laser/Brimstone share another.
Preserve unrelated worktree changes and do not stage files without instruction.

## Implementation

- [x] Confirm and archive the current requirements and configurable defaults.
- [x] TrajectoryModule attachment: follow SplitModule and attach through
  TriggerModule.attachToBullet during attack preparation, before freezing.
  Migrate ability registration; replace the independent player trajectory
  table/StatManager path without duplicate injection.
- [x] Module state and propagation: positive stack merging in attachment order,
  defensive configuration/runtime copies, freeze boundaries, copied-context
  inheritance, execute-only consumption, and network/client snapshots.
  Remove player trajectory storage/API/NBT; no compatibility or migration layer.
- [ ] Shared motion/range: moving baseline plus offsets; charge baseline travel
  only; replace normal lifetime/200-tick range termination with distance.
- [x] Tiny Planet implementation: continuous player-centered ordinary orbit with launch-pitch
  height; laser completes one inclined orbit and smoothly restores the
  original 3D launch axis; laser preliminary stages are range-exempt.
- [x] Tiny Planet infrastructure: abstract TrajectoryModule, independent class,
  distance-parametric path memory, explicit range cost, packet state and curved
  collision samples. Other six formulas remain unchanged.
- [ ] Tiny Planet visual/combination acceptance in the Minecraft client.
- [x] Correct Tiny Planet launch alignment for both families and replace ordinary
  anchor translation with speed-bounded pursuit, allowing lag behind fast players.
- [x] Fix laser terminal-range rounding stall; bound the execution loop including
  free orbit travel, and test homing interruptions through real laser stepping.
- [x] Apply pitch-dependent orbit height to both families, with -60..+60 degrees
  mapped onto the 0.1..0.9 height interval.
- [x] My Reflection: ordinary continuous attraction, weak nearby and stronger
  beyond 12 blocks (smooth 12..18 ramp); no return/orbit phase. Target live owner
  when shooter=owner, otherwise own spawn origin. Horizontal yaw before pitch;
  laser symmetric U-turn and return through launch point, then reverse flight;
  laser preliminary stages are range-exempt.
- [x] My Reflection integration: separate bullet/laser registry modules attached
  by one TriggerModule, fixed launch plane, copied/networked phase, distance
  termination for ordinary bullets, and sampled laser collision paths.
- [x] Laser Planet then Reflection: order independent of attachment order;
  extend the return leg from Planet's exit to the original firing point;
  finite work budget includes both preludes and tracking pauses.
- [ ] My Reflection client visual acceptance, including moving players and
  combinations with the remaining trajectories awaiting redesign.
- [x] Hook Worm: battlement path; lateral legs neither change primary direction
  nor consume range.
- [x] Wiggle Worm: centered positive/negative sinusoidal offsets without
  shortening baseline reach.
- [x] Ring Worm: continuous helix around the current primary centerline,
  perpendicular radial plane, smooth entry, non-chargeable offsets.
- [x] Ouroboros Worm: continuous vertical-plane loops about a moving center,
  smooth entry, non-chargeable circular offsets including longitudinal motion.
- [x] Gravity: independent module with downward primary acceleration for ordinary/fetus bullets;
  laser/Brimstone remain gravity-free.
- [x] Increase ordinary Reflection turning to 0.15 radians/tick; verify reversal
  before range exhaustion and no double movement when combined with gravity.
- [x] Unify ordinary/fetus/laser spawn origin, including child projectiles and
  spawn offsets; preserve authoritative origins when reconstructing snapshots.
- [x] Expose current/root ResourceLocation IDs on AttackContext from AttackType declarations
  before attachment; preserve copies/freeze and remove sequence-index classification.
- [ ] Composition: combine primary modifiers and offsets in the current frame;
  laser Planet completes before Reflection, then range charging begins.
- [ ] Tracking/control: suspend all trajectories during active steering;
  charge steering movement; smoothly resume preserved phase/stage from the
  current position/direction.
- [ ] Collision: sweep every actual movement, including offset-only and
  range-exempt segments.

## Acceptance

- [x] Actual module lifecycle tests: obtain/remove, stacks, attachment,
  freezing, copies, inheritance, direct execution, isolated runtime state,
  and no duplicate injection. Java 17: JUnit 84 passed / 3 existing skipped;
  all 3 Forge GameTests passed. Special extra-attack preparation and split
  call sites also checked in source; full in-client scenarios remain below.
- [ ] All seven modules: ordinary/laser paths with horizontal, pitched, and
  vertical launches; fetus/Brimstone routing checks.
- [ ] Range accounting: baseline reach, pure lateral legs, longitudinal
  circular offsets, primary curvature, free laser stages, and combination order.
- [ ] Continuity/composition: player movement, later view changes, tracking
  and control transitions, preserved phase, current-frame offsets, no teleport.
- [ ] Collision and client synchronization: server sampling plus client
  observation; endpoint-only checks do not establish correct full paths.

Check an item only after its work and relevant verification are complete.

Latest verification (2026-09-10): Java 17 `test runGameTestServer --offline`
passed: 121 JUnit cases, 118 passed / 3 existing skipped; all 10 Forge GameTests
passed. This validates current automated coverage, not client visual acceptance.

## Current task: attack identity (2026-09-20)

- [x] Preserve intentional mutable trigger/split access and damage-based Builder scale derivation; document their boundaries.
- [x] Delete both type enums; AttackType defines concrete/root ResourceLocation IDs, available before Prepare listeners.
- [x] Migrate runtime snapshots, network identity, trajectory dispatch/rules and split execution without duplicate attachment.
- [x] Verify external roots/derived attacks, freeze/copies, packet round trips; run Java 17 JUnit and Forge GameTest.

Identity migration verification: Java 17 `test runGameTestServer --offline` passed;
147 JUnit cases (144 passed, 3 existing skips), all 21 Forge GameTests passed.
See `build/attack-identity-final.log`. Client visuals were not checked in this task.

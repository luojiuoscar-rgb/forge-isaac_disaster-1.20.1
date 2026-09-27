# Current contract (2026-09-20)

## Attack identity and preserved context contracts

- Verified commit `daa72a3`: mutable `getTrigger()` and `getSplitSequence()` were deliberately added back after the older freeze refactor. Retain both; freeze guards controlled context mutations, not these collection accessors. Copies/runtime consumers own independent containers.
- The bullet-size plan explicitly requires Builder derivation to recalculate size from child damage and the inherited modifier. Preserve that behavior. Snapshot copies and type-only rebinding retain finalized scale; remove the write-only override flag.
- AttackType owns `getId()` (concrete registered identity) and `getRootId()` (behavior family, defaulting to self). AttackContext stores one definition, exposing `getTypeId()` / `getRootTypeId()`. No independent context root setter or closed type enum remains.
- Attack IDs are declared in ModAttackTypes.register(...); implementations and root-family checks read RegistryObject.getId(). Technology 2 and Shoop Da Whoop now retain their own concrete IDs, both under the laser root. Delegating attacks still prepare the actual emitted attack.
- Plain JUnit now initializes ModAttackTypes when querying attack IDs and fails without Minecraft/Forge bootstrap (Not bootstrapped, then dependent NoClassDefFoundError). A temporary standard Bootstrap.bootStrap probe also requires Forge event-class transformation. Keep this test-environment limitation explicit; production identity continues to come from registration. Real registry/identity validation runs in Forge GameTest; no JUnit cases were removed or disabled.
- The pipeline binds identity before publishing Prepare, including highest-priority listeners. Direct execution and split requests bind the executing type without attaching player modules again. Parasite preserves derived laser definitions; changing a child's backend updates its context identity too.
- Projectile configuration and runtime IDs agree. Spawn packets transmit concrete/root ResourceLocations; client prediction preserves arbitrary addon IDs. Existing fetus rendering/tracking and Brimstone sequence behavior remain explicit specializations.
- TrajectoryAttachment maps supported root IDs to modules; unknown roots do not fall back to ordinary bullets. Addons can supply another mapping. Rewrite rules/cache keys use ResourceLocation value equality.
- Direct/debug constructors must bind an attack type before spawning. No compatibility aliases or old-save conversion were introduced; no staged files were changed.

## Runtime ownership

AttackContext retains TrajectorySequence plus an optional immutable
inheritedTrajectorySnapshot. It no longer owns loose anchor/origin/axis/seed/distance/state/residual
fields or their builder setters. TriggerModule attachment and freeze-time rules stay
in attack preparation. No compatibility layer was added.

Every ordinary bullet and laser owns one stable TrajectoryRuntime, exposed through
IBulletObject.getTrajectoryRuntime(). It contains immutable launch-frame semantics,
the current shooter anchor, trajectory clock, per-module memory and composition
residuals. Shared worm kinematics is a lazily allocated runtime field, not a fake
module entry. The unused seed pipeline was removed; Planet slot direction remains.

TrajectoryRuntime.forSpawn uses the final actual spawn and firing direction. Split
children inherit phase/stage/progress with independent state and rebind path geometry
to their own launch frame. They resume through the existing recovery path. Stored
projectile AttackContext copies discard consumed parent snapshots. Snapshot.restore
is the separate network operation and preserves the original shot's launch frame.
Spawn/correction packets use one immutable snapshot and the runtime's shared codec.
Corrections restore clock and phase together and clear removed state entries.

Anchor updates use the actual shooter on both server and client. Shooter entity ID
is included in the runtime snapshot; clients keep the last supplied anchor when the
shooter is unavailable instead of substituting owner position.

Verified with Java 17: 147 JUnit cases (144 passed, 3 existing skipped), all 21 Forge
GameTests passed. This includes prepared launch-axis changes, child frame/state
isolation, network restoration, existing combinations, collisions and laser stress.
Client visual acceptance remains separate. Latest log: build/attack-identity-final.log.


## Four worms: implementation contract

Hook/Wiggle/Ring/Ouroboros are independent OffsetTrajectoryModule subclasses,
registered under their existing IDs and attached by their existing triggers.
All four variants share the same 3D formulas. BuiltinTrajectory is removed.
Wiggle/Ring/Ouroboros start at zero offset and ramp over 2 primary blocks.
Wiggle repeats every 4 primary blocks; Ring/Ouroboros repeat every 8. There is no 3-block straight delay
or finite Ring exit. Hook starts with 2 forward blocks, then crosses to +1;
subsequent forward legs are 2 blocks and crossings alternate between +/-1.
Only default straight motion pauses for Hook crossings. Other primary modules
continue, and all non-Hook phases advance only with actual primary path length.

TrajectoryKinematics separates primary position/velocity/acceleration from the
actual offset/tangent. It is stored directly in TrajectoryRuntime, without a
reserved module ID. Runtime snapshots deep-copy and serialize it, including
frame, primary distance, primary parameter clock, and recovery. Hook has its own
lateral offset/phase. Bullet lifetime no longer truncates any attached trajectory.
Offset speed is extra; primary motion keeps its normal speed/range rules.
Worm offsets use the fixed per-projectile launch frame with stable vertical fallback.
Primary steering remains independent. Child creation rebinds its launch frame.
Tracking/control pauses all phases; resumption rebases at actual position and
uses a 2-primary-block offset ramp with preserved phase.

Final paths sample primary segments and Hook corners with amplitude-dependent
subdivision (at most 0.05 parameter blocks, finer at larger amplitudes).
Per-segment range costs distinguish pure lateral motion and laser free preludes;
ordinary truncation and laser collision consume those costs. Laser iteration
budgets include Hook's bounded lateral distance, and substep loops guard no progress.
Server telemetry includes primaryPosition, primaryVelocity, primaryDistance,
trajectoryOffset and modulePhases separately from traveled and actual velocity.

These replace the historical fixed-frame and horizontalized worm assumptions.

Concrete/root attack identities are ResourceLocations declared by AttackType.
Planet and Reflection each retain one TriggerModule which chooses a module from
an explicit root-ID mapping before creation. Closed identity enums have been removed.

TrajectoryRule is a pure rewrite of required ResourceLocation IDs and stack counts.
All Forge registry entries (including addons) are indexed after registration.
Lower priority first, then lexicographic registry ID. Each rule fires at most once;
new outputs enable previously unmatched rules without unbounded cyclic rewriting.
Replace at the first consumed position; unconsumed order stays intact. Empty output
deletes inputs. Default output keeps its own input stacks, otherwise takes minimum
matched stacks; colliding unconsumed output stacks merge. Custom rules may choose
output stacks explicitly. Rules must not depend on world/player mutable state.
Cache has 1024 immutable ordered-input/root entries. Freeze resolves after all
attachments, before creation. Copies preserve resolved status; new attachments
invalidate it. Removed module runtime states are excluded from snapshots.

Ordinary Planet covers ordinary Reflection only, retaining Planet stacks. Laser
combinations retain both modules and their existing prelude sequence.

Reflection now integrates velocity under attraction instead of imposing a turn angle.
Acceleration ramps from zero to 10% by 12 blocks and full strength by 18 blocks;
full strength is 0.18 * baseSpeed^2 blocks/tick^2, vertical force weight 0.15.
Velocity is capped, not renormalized: outgoing bullets slow, reverse, and accelerate
back. Actual substep distance consumes range. No scripted orbit or return phase.
Self-fired targets use live owner position; other shooters use own spawn origin.
This supersedes previous constant-speed/angular-rate Reflection requirements.

# Current Trajectory Requirements

## Authority and Scope

Approved requirements for the redesign. Attachment/storage migration is
implemented. Tiny Planet now has an independent implementation and automated
geometry/runtime coverage; client visual and full combination acceptance remain
pending. My Reflection now has separate ordinary/laser implementations;
Gravity and all four worms also have independent implementations; client visual acceptance remains pending.
The current uncommitted motion code
may be replaced substantially; only two families need distinct design:
ordinary bullets (including fetus) and lasers (including Brimstone).

## TrajectoryModule Attachment

- Each projectile owns its actual initial spawn position, including ordinary/fetus
  spawn-center offsets. Child projectiles establish a new origin; client snapshot
  reconstruction explicitly restores the authoritative origin. Laser construction
  no longer reuses the parent context's trajectory origin.
- AttackType declares concrete and root ResourceLocation IDs, available on the
  context before any Prepare listener. Context copies and freeze retain the definition.
  Third-party roots are allowed; unsupported roots do not select ordinary trajectories.
  Laser sequence index does not identify the projectile family.

- Reference SplitModule. Abilities/items/trinkets register and unregister
  TriggerModules; these attach TrajectoryModules through
  TriggerModule.attachToBullet during attack preparation, before AttackContext
  freezes. The user's "attackBullet" refers to this lifecycle.
- PlayerAbility trajectory fields/API/NBT and StatManager.addTrajectory are
  removed. TriggerModule is the sole player configuration source. This is an
  unpublished mod: no old API aliases, save conversion, or compatibility layer.
- Follow SplitModule conventions for registry identity, entries, stack merging,
  copying, and freeze boundaries. Runtime state belongs independently to each
  projectile and remains available for motion, recovery, and synchronization.
- TrajectorySequence merges positive stacks by ID in first-attachment order.
  AttackContext.addTrajectoryModule obeys freezing; reads/builders/copies isolate
  mutable containers. TrajectorySpec snapshots use amplifier = stacks - 1.
- Split children inherit copied contexts; execute-only attacks consume their
  prepared modules without another player attachment. Current runtime state is
  retained and independently copied. Network snapshots carry module specs.
- Attachment and execution are separate: TriggerModule attaches; the trajectory
  runtime evaluates motion, composition, and chargeable distance.

## Motion, Range, and Composition

Primary modules and four independent worm classes extend TrajectoryModule.
Worms run in a dedicated offset pass after primary evaluation. Per-projectile
path memory and kinematics are copied and serialized centrally.
Hermite entry/exit/rejoin curves have matching endpoint tangents. Orbit progress
is distance / radius, so one laser revolution spans 2 * pi * radius. Explicit
rangeCost is separate from progressRate; zero cost does not stop path progress.
All ordinary bullets with attached trajectories use range rather than lifetime termination.
Ordinary curved sweeps are sampled at 0.05 parameter-distance intervals; laser
steps are limited to 0.1. Complete combination redesign remains future work.

Actual position = moving baseline point + trajectory offsets.
Charge the baseline's traveled path, not actual-path length or projection onto
the initial direction.

- Planet, Reflection, Gravity, tracking, and control change primary motion.
- Hook, Wiggle, Ring, and Ouroboros produce non-chargeable offsets. Offsets
  may include forward/backward components: circle center O moving costs range,
  point A rotating about O does not.
- Ordinary bullets terminate by distance like lasers. Normal range must not
  be cut short by lifetime or the old 200-tick cap.
- Offsets follow the CURRENT primary direction with continuous frame and phase.
  Retain initial direction only where a module explicitly needs it.
- Multiple primary effects act together; combined shapes may differ from
  standalone shapes. Multiple offsets share the current frame.
- Laser Planet plus Reflection is sequential: complete Planet and its exit,
  then Reflection and its return, then begin range charging.
- All actual path segments collide, including sideways, circular, and
  range-exempt movement.

## Tracking and Control

Active tracking or active player control immediately suspends ALL trajectory
effects, including offsets. Merely possessing either capability is insufficient.

Steering movement consumes range, including when the suspended stage was
range-exempt. Preserve trajectory phases/stages. On steering end, smoothly
resume from current position/direction with preserved phase/stage, without
teleporting or returning to an old absolute path. Planet remains player-centered.

## Seven Trajectories

| Module | Required behavior |
|---|---|
| Tiny Planet, ordinary | Launch along the actual 3D firing direction, then enter the orbit in front of the player. Pursue the player's CURRENT orbit through velocity changes bounded by bulletSpeed, never by translating the bullet with the player. A faster player may outrun the bullet. Continue until range is exhausted. |
| Tiny Planet, height | Both families sample each shot's pitch once: upwardDegrees = degrees(asin(launchDirection.y)); height ratio = clamp(0.5 + 0.4 * upwardDegrees / 60, 0.1, 0.9), relative to player feet and height. Moving players change the target height/center only; ordinary projectiles approach within their speed budget. Later view changes affect only new shots. |
| Tiny Planet, laser | Launch along the actual 3D firing direction, enter the horizontal orbit in front of the player, complete one revolution, then smoothly return to the original launch axis AND original 3D direction. Launch/entry/orbit/exit are free; charge only after exit. |
| My Reflection, ordinary | Continuous velocity force, no scripted orbit. Self-fired shots target live owner; other shooters target their own spawn. Weak within 12 blocks, full pull by 18; horizontal-dominant force slows outgoing velocity and accelerates the return. Speed is capped and actual travel consumes range. |
| My Reflection, laser | In the plane of initial direction and player left/right: smooth left shift, short parallel segment, forward half-circle/half-ellipse U-turn into the right return lane, symmetric return through launch point, then exact reverse-axis flight. All preliminary motion is free; charge after returning and joining reverse flight. |
| Hook Worm | Battlement path. Forward legs cost range; pure sideways legs do not advance the baseline or change primary direction. Preserve baseline reach. |
| Wiggle Worm | Centered positive/negative trigonometric wave, no persistent one-sided bias. Offset motion does not shorten baseline reach. |
| Ring Worm | Smoothly enter continuous circling in the plane perpendicular to current primary direction while the center advances. The helix centerline is the primary path; no one-revolution limit or offset range cost. |
| Ouroboros Worm | Smoothly enter continuous circling in the vertical plane containing primary direction while the center advances. Center movement costs range; rotation, including longitudinal offset, does not. Distinct from Hook's battlement path. |
| Gravity | Downward primary acceleration for ordinary/fetus bullets; curved primary travel may reduce horizontal reach. Laser/Brimstone remain gravity-free. |

## Module-local Scalar Defaults

Keep named scalar parameters together in each module for tuning, not scattered
formula/caller literals. Behavior takes precedence over defaults. An additional
level means a level above the first effective stack.

| Module | Parameter | Default |
|---|---|---|
| Planet | Radius / additional level | 3 blocks / +1 block |
| Planet | Entry / laser exit scale | 2 blocks / 2 blocks |
| Planet, both families | Height midpoint / amplitude / pitch span / limits | 0.5 / 0.4 / -60..+60 degrees upward / 0.1..0.9 |
| Reflection ordinary | Maximum pull / vertical weight | 0.18 * baseSpeed^2 blocks/tick^2 / 0.15 |
| Reflection laser | Left/right lane offsets | 1.5 blocks each |
| Reflection laser | Lateral transition / parallel leg | 2 blocks / 3 blocks |
| Reflection laser | U-turn radius | 1.5 blocks, symmetric return |
| Hook | Forward leg / lateral amplitude | 2 blocks / 1 block either side |
| Hook | Sideways speed | Equal to primary-motion speed |
| Wiggle | Amplitude / additional level | 0.5 blocks / +0.25 blocks |
| Wiggle | Wavelength | 8 blocks of primary advance |
| Ring/Ouroboros | Radius / additional level | 1 block / +0.5 blocks |
| Ring/Ouroboros | Period | One revolution per 8 blocks of primary advance |
| Gravity | Downward acceleration | 0.05 blocks/tick squared |
| Shared recovery | Smooth rejoin scale | 2 blocks |

Ring/Ouroboros require smooth entry. Transition scales describe geometry,
not permission to charge otherwise range-exempt stages.

## Evidence

Gravity implementation (2026-09-10): GravityTrajectoryModule
replaces the BuiltinTrajectory constant under the existing gravity registry ID
and Ipecac trigger, preserving explosion effects. Ordinary/fetus shots accumulate
-0.05 Y velocity per tick with semi-implicit integration. Curved movement consumes
range and clips the final step; normal termination uses range. Laser/Brimstone
produce no gravity intent, preserving existing laser paths. Gravity executes
after ordinary Reflection. Relative-controller composition subtracts the prior
step once, preventing doubled advancement. Full worm frame/offset redesign is
still pending; this does not complete the global motion-composition contract.
Final Java 17 verification: 116 JUnit cases (113 passed, 3 existing skipped),
all 8 Forge GameTests passed. Client visual/feel acceptance remains pending.

My Reflection (2026-09-10): removed BuiltinTrajectory.MY_REFLECTION and its old
fixed reversal formula. One player trigger attaches my_reflection_bullet or
my_reflection_laser through the prepared attack family. No legacy alias.
The former ordinary fixed-turn-rate implementation was superseded on 2026-09-11
by velocity integration under distance-weighted force; see the current contract.

Laser motion uses C1 quintic lateral transitions, straight lanes and a
semicircle in the immutable launch-direction/player-left plane. Prelude length
is 2*(2+3)+pi*1.5. After Planet, an additional radius+2 return distance brings
the laser back to its original firing point, not only the Planet exit. The
evaluator serializes these two preludes regardless of attachment order and
clips the Planet exit step. All intermediate collision samples are consumed.
Path state persists the fixed plane, return length and phase through copies
and network buffers. Tracking suspends progress and normal movement costs
range; recovery uses the current position and heading over a 2-block scale.

Two new regressions failed on the old formula (no weak ordinary turn and
charged laser entry) before implementation. Java 17 final full verification:
111 JUnit cases, 108 passed and 3 existing skipped; 7 Forge GameTests passed.
Coverage includes real attack-family preparation, EXECUTE_ONLY, serialized
phase, reverse 3D axis, boundary tangents, both Planet attachment orders,
repeated homing interruptions, terminal floating residue and off-axis block
collision. Client visual observation remains pending. Old Planet fixtures were
updated from removed IDs and horizontal-only laser assertions; no tests were
deleted to obtain this result.

Laser termination: subtracting two large cumulative path distances can round
the final charge to zero while traveled remains below range. The regression
uses range 16 with Math.nextDown(16) already consumed and reproduces the stall.
Charge is now computed from the local step budget, and the laser loop has both
a no-progress guard and a finite step budget including module-declared free
travel. Active homing charges range and preserves suspended orbit progress.
The reported client remained responsive while world interaction and shutdown
stalled; logs contain no crash stack and the game had been forcibly closed.
This is consistent with a stuck integrated-server thread, but its live stack
was unavailable, so the reproduced loop is not claimed as the sole proven cause.

Use the acceptance checklist in task_plan.md. Exercise real module attachment
paths, server samples, and client observation/synchronization. Existing reports
and sparse endpoint assertions cannot establish acceptance of this redesign.

## 2026-09-12 worm correction

The prior ring tuning used a 4-block phase period and doubled radius, which made coarse visual segments appear polygonal and was applied to the wrong worm. Ring Worm now uses the original radius/period; higher amplitude/frequency belong to Wiggle Worm. Worm offset axes are fixed from each projectile's launch direction and are independent of later primary steering or pitch sign representation, preventing xRot crossing flips.
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
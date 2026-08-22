# Findings: C-Section + Laser Performance Diagnosis

## Scope
Current source of the Forge 1.20.1 workspace. This is an analysis record, not a proposed patch.

## Evidence Log
- `TearBullet.tick()` posts `BulletTickEvent` once per server tick, then performs a block ray cast and entity collision search. Its current trajectory accessor parses the synchronized string into a newly allocated `HashMap` on every call/tick.
- `FetusBullet` inherits the full tear tick and is always configured as homing by `CSectionAttack`. It searches nearby living entities every fourth tick through `level.getEntitiesOfClass(...)` over a 12x12x12 box.
- The Laser module attaches `LASER_PLUS_FETUS` to every bullet's `BULLET_TICK` trigger. `LaserPlusFetus` runs for `FetusBullet` every fourth tick and starts a new `LaserAttack` with the fetus position, trajectories, color, and copied trigger state.
- `LaserAttack.shoot()` completes the whole beam synchronously: it loops `S = ceil(R / max(0.5, 2W))` steps in one server tick, where `R` is range (clamped to 1..64) and `W` is beam width. Every step emits dust particles, clips blocks, and calls `getEntitiesOfClass` for collision.
- The secondary `AttackContext` is given `bullet.getTriggers()` directly. Its constructor retains that object instead of copying it. `BULLET_ONLY` still raises `AttackContextPrepareEvent`, whose module dispatch calls `Laser.attachToBullet`, appending one `BULLET_TICK -> LASER_PLUS_FETUS` trigger and one hit trigger to the original fetus trigger list for every emitted beam.
- Therefore this is a direct state-mutation recurrence, not merely a periodic cost: one fetus starts with one LaserPlusFetus tick trigger; the first laser burst appends one more, the next burst invokes two laser effects and appends two more, and so on. Beam count per fetus per burst is `1, 2, 4, 8, ...`. The laser itself does not post `BulletTickEvent`; the growth happens because its prepare stage mutates the parent bullet's shared trigger object.
- Same-tick C-section shots share `tickCount`, so all active fetus bullets trigger their 4-tick laser on the same tick. The user-visible stutter is therefore the peak, not merely the four-tick average.
- Each fetus is a tracked Forge entity (`updateInterval(2)`) rendered as a full `PlayerModel`; this adds client draw and entity-tracking cost, but does not explain the sharp laser-only multiplier as strongly as the server beam loop and particle packet work.

## Cost Model
Let `N` be active fetus/tear entities, `M` the attached bullet triggers relevant to a dispatched event, `R` the beam range, `W` its width, `S = ceil(R / max(0.5, 2W))`, `E` the candidate-entity/chunk-query cost in an AABB, `B` a block clip, `P` particle/network work per beam step, and `H` the entities hit by a beam.

- Ordinary tears, per server tick: average `Theta(N * (B + E + M + T))`, with `T` trajectory parsing/application. Worst case remains linear in `N` and local entities because each tear only resolves one collision path per tick: `O(N * (B + E + M + T))` before secondary effects.
- One laser activation: `Theta(S * (B + E + P + T) + H * M)` in a single tick. Its time is not amortized across its visible length.
- C-section plus Laser, if trigger state were isolated: four-tick average would be `Theta(N * (B + E + M + T) + (N/4) * (S * (B + E + P + T) + H * M))`, with a synchronized burst peak of the same laser term multiplied by `N`.
- C-section plus Laser as currently implemented: at the `k`-th laser burst after each fetus is created, the LaserPlusFetus trigger and beam count are `2^k`. The burst cost is `Theta(N * (B + E + M + T + 2^k * (S * (B + E + P + T) + H * M)))`; the strict lifetime worst case is exponential in burst count, until the bullet dies or the server fails. Other repeatable child-spawn modules can make the general system bound even larger, so their branching factors must be explicitly capped.

## Fix Applied
- `AttackContext` now copies supplied `CompositeTrigger` state in both public constructors. Secondary attack preparation therefore cannot append triggers to a parent bullet.
- `AttackContextIsolationTest` was red against the original aliasing behavior and passes after the fix.

## Follow-up: Strict Secondary Laser Inheritance
- The constructor snapshot prevents mutation of the parent trigger, but does not prevent `BULLET_ONLY` from running `AttackContextPrepareEvent` and attaching a second, local set of player modules to the laser context.
- `LaserPlusFetus` must use `RAW`: it executes the snapshot supplied by the fetus bullet and bypasses all pipeline events, including player trigger attachment.

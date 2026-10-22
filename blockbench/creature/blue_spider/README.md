# Blue spider

`blue_spider.bbmodel` is the approved editable source, including idle, forward,
left and right animation references. The runtime keeps the 2x2x2 blue body,
four bent black legs and two folded, double-sided flat fangs.

## Runtime familiar

The entity id remains `isaac_disaster:blue_spider`. Its implementation now lives
in `entity/familiar/impl/spider`, with shared behavior in
`AbstractSpiderFamiliarEntity`. It uses the existing owner-managed familiar
lifecycle rather than the original standalone no-AI mob.

Use `/isd familiar @s add isaac_disaster:blue_spider 1` to test ownership, roaming,
following and animation. Use `64` instead of `1` to check a full group. Remove it
with `/isd familiar @s remove isaac_disaster:blue_spider 1`.
Both the blue-spider type and the shared `spiders` group have a cap of 64;
this quota is independent from `flies`. Naked `/summon` creates an unowned
instance which is cleaned up after the familiar validity interval.

Spiders have gravity and terrain collision, with entity pushing and movement
sound/game-event emissions disabled. They reject entity collision in both
directions, including physical obstruction by boats. Vanilla entity collision
queries still run; contact damage is handled separately. Every roaming, follow
and attack route uses the same movement attempt, with a random 6-9 block
horizontal path-length budget.
A route ends on arrival, budget exhaustion or blockage, then pauses 6-14 ticks.
Each wander attempt has an equal chance to turn in place or travel. A turn-only
attempt smoothly reaches its chosen heading without horizontal movement, then
uses the same recovery and wandering delays as a completed route. Following and
attack attempts always travel after turning.
Roam goals stay within eight blocks; beyond ten blocks from the owner, follow
goals are chosen within three blocks of the owner. Follow bursts obey the same
6-9 block limit. Each roaming burst samples a speed between 0.2 and 0.3
blocks/tick (4-6 blocks/second) and keeps it for that entire burst. Following
and attack movement use the maximum 0.5 blocks/tick (10 blocks/second).

The runtime chooses a short horizontal burst and moves through ordinary block
collision. Roaming, following and attacking can all leave unsupported ledges
and fall under gravity. Removing support or releasing a wall also causes a
natural fall.

Each burst selects forward, left or right travel relative to the body. The body
first turns toward the corresponding heading, at most 27 degrees per tick, while
horizontal movement pauses. Only after aligning does it travel along that single
body axis. The destination, body heading and chosen axis stay fixed throughout
the segment, including attack movement. An enemy's new position can be selected
for a later segment. Turning spends no travel budget and keeps gravity and
contact damage active. Backward and diagonal
body-relative movement are disabled; a world-diagonal destination is reached by
turning the body first.

Clients accept packet yaw immediately and interpolate a separate pair of render
headings between client ticks. Position interpolation stays separate, so
position-only packets cannot interrupt an unfinished body turn and leave the
visible body misaligned with its movement axis.

A horizontal collision starts the original-style wall climb. While climbing it
moves straight upward at 2 blocks/second and does not select a new route. Once
per second it has a 10% chance to release the wall and fall. Losing the wall,
hitting a ceiling, or releasing the wall ends the climb. Entity pushing and
movement sound/game-event emissions remain disabled.

Movement has two idle phases. After a burst, WAITING lasts the normal 6-14 tick
pause. PENDING then waits a random 10-40 ticks before choosing a new wander
point. A follow or attack need can start movement as soon as WAITING ends.
Follow and attack bursts still use the shared 6-9 block movement budget and
terrain collision and gravity; unsupported ledges do not stop movement. The
shared sixteen-block familiar recall lifecycle remains active.

AI behavior and physical phases are server-local. SpiderMotionController owns
movement decisions, complete movement attempts, recovery/wandering delays,
gravity and wall climbing. Each attempt stores its goal, direction, speed,
turning flags, target and remaining horizontal budget together. Its Delay is an
internal implementation detail. AbstractSpiderFamiliarEntity owns the familiar
lifecycle, contact effects, client interpolation and synchronization.

Clients receive only a typed gait (idle/forward/left/right), encoded as one byte.
Travel speed is not synchronized: each movement gait advances its authored
clock at the fixed 1.5x rate. SpiderOwnerFrame owns shared targeting;
SpiderTerrain contains only local terrain/collision queries and spawn placement.
There is no ground pathfinder, ledge veto, jump behavior or compatibility adapter.

A shared owner frame assigns eligible enemies to nearby spiders of every
variant using TrackingTargetRules.eligibleFlyOrSpider, the target policy shared
by fly and spider families. AbstractSpiderFamiliarEntity owns the common movement
and contact-consumption behavior; variants supply getBodyColor(), getContactDamage()
and optional onContactHit() effects. Blue contact damage uses the owner's current
attack-damage attribute. Contact consumes the familiar request once, even if
the attempt is blocked. Different fly variants likewise share one attack queue.

## Animation maintenance

The runtime plays all four approved clips from Java data without GeckoLib.
The three movement clips retain the approved 0.4-second cycle and exaggerated
stride/lift. Forward points toward the fangs (source -Z); left is source -X.
Actual horizontal displacement selects a separate forward/left/right clip
relative to body yaw. The authored clips stay unchanged; all three movement
clocks play at a fixed 1.5x rate, so each 0.4-second source cycle takes about
0.267 seconds regardless of roaming, following or attack movement speed.
Stopped or turning spiders play idle and pause the movement
clock. The existing upward climb uses the forward gait at the same 1.5x rate.
Animation does not add horizontal root motion. The renderer compensates for the
model's 24-unit Java living-model origin.
The runtime spider.png has a neutral white body and the approved black legs
and fangs. AbstractSpiderFamiliarEntity.getBodyColor() supplies only the body's
RGB tint; BlueSpiderEntity returns #4077E0. The model renders the appendages
without that tint. New color variants reuse this single texture and override
getBodyColor(), as fly variants already do. The approved Blockbench source
texture remains blue for editing; conversion neutralizes its body pixels.

All fly and spider variants have the hostile-view flash. Their family renderers
alternate each variant's code-supplied body color and #D8D8D8 eight full cycles
per second; owners and allies keep the ordinary body color. This behavior is
scoped to fly and spider families, with no generic familiar flash helper and
no color-specific flash texture.

Each model caches its reset-part array and binds animation tracks to bones once.
Runtime sampling reuses a per-model vector and normalizes clip time once per
pose, preserving the authored interpolation without per-track allocations.

Editing the Blockbench file does not automatically update the Java geometry,
animation tracks or `textures/entity/creature/spider.png`. Keep the source and
runtime conversion synchronized and run the geometry/animation comparison tests
after edits. Validation results are reported separately for JUnit, server
GameTests and any actual client visual checks.

## Smooth height transitions

Grass-to-path descent remains ordinary gravity/collision movement. Ground-state
changes cause vanilla ServerEntity to send ClientboundTeleportEntityPacket even
for a 1/16-block step. Those absolute packets now use the same client position
interpolation window as relative packets. Only absolute corrections farther
than eight blocks, such as a distant recall, snap and cancel the old target.
Client interpolation has no local ground snapping or collision-driven correction.

At the top of a wall, CRESTING schedules a horizontal velocity into the platform
at the existing climb speed (0.1 blocks/tick). Reaching the top never adds an
instant horizontal transfer. Subsequent ticks integrate that velocity through
normal block collision, checking swept contact damage and accounting for the
remaining burst budget. The brief exit holds the height until the feet clear
the edge, then resumes gravity. An obstruction releases the velocity; recall
clears the exit state through the shared familiar lifecycle. No new movement
attempt is selected during this exit.

Tests cover grass/path movement in both directions with real tracking packets,
nearby correction interpolation, repeated corrections, far recall, multi-tick
platform entry, obstruction during entry, and recall during entry. Client visual
checks are reported separately from JUnit and server GameTests.

## Attack interception

Attack attempts predict a bounded horizontal interception rather than aiming at
the enemy's old position. SpiderOwnerFrame samples actual target displacement
once per owner tick from the existing four-tick candidate scan; teleports, stops
and stale samples reset the estimate. No additional world scan is introduced.
Each attempt chooses a stable timing variation and a small sideways spread.
Prediction includes turning time, looks ahead at most 16 ticks and leads by at
most three blocks, plus at most 0.25 blocks of sideways spread. Slow or already
touching targets use their current position without spread.
The goal is calibrated once after the initial turn. Departure locks the goal
and body heading; small enemy motion does not steer the running spider. A sharp
turn, a large deviation from the observed trajectory, or passing the interception
point ends the attempt early and enters the normal recovery. Wall climbing still
finishes before another movement decision. Burst budgets, speeds, the three
body-axis directions and the fixed animation rate are unchanged.

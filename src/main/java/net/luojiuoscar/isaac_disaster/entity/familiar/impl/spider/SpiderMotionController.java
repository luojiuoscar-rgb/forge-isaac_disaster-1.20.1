package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/** Server movement, including complete burst sessions, waiting, gravity and wall climbing. */
final class SpiderMotionController {
    enum Behavior { IDLE, ROAM, FOLLOW, ATTACK }
    enum Phase { WAITING, PENDING, TURNING, MOVING, CLIMBING, CRESTING, FALLING }

    private static final double ARRIVAL_DISTANCE = 0.035;
    private static final double MOVEMENT_PIECE = 0.2;
    private final AbstractSpiderFamiliarEntity entity;
    private final RandomSource random;
    private final Delay delay = new Delay();
    private Attempt attempt;
    private Phase movementPhase = Phase.WAITING;
    private SpiderOwnerFrame ownerFrame;
    private Vec3 climbDirection = Vec3.ZERO;
    private int climbTicks;
    private double crestRemaining;
    private double fallingVelocity;

    SpiderMotionController(AbstractSpiderFamiliarEntity entity, RandomSource random) {
        this.entity = entity;
        this.random = random;
        resetTiming();
    }

    Behavior behavior() { return attempt == null ? Behavior.IDLE : attempt.behavior; }
    Phase phase() { return movementPhase; }
    boolean isClimbing() { return movementPhase == Phase.CLIMBING; }

    private boolean moving() { return attempt != null && !attempt.finished(); }

    private void resetTiming() {
        delay.reset(SpiderMovement.pauseTicks(random), SpiderMovement.randomMoveDelayTicks(random));
    }

    private void finishBurst() {
        attempt = null;
        resetTiming();
        movementPhase = entity.onGround() ? Phase.WAITING : Phase.FALLING;
    }

    void recalled() {
        finishBurst();
        if (ownerFrame != null) ownerFrame.remove(entity);
        ownerFrame = null;
        climbDirection = Vec3.ZERO;
        climbTicks = 0;
        crestRemaining = 0;
        fallingVelocity = 0;
        movementPhase = Phase.WAITING;
        entity.setMovementAnimation(SpiderMovement.Gait.IDLE);
    }

    void tick(ServerPlayer owner) {
        ownerFrame = owner == null ? null : SpiderOwnerFrame.get(owner);

        // Wall movement and falling never start new movement attempts.
        if (movementPhase == Phase.CRESTING) {
            tickCresting(owner);
            return;
        }
        if (isClimbing()) {
            tickClimbing(owner);
            return;
        }
        if (moving() && behavior() == Behavior.ATTACK) {
            LivingEntity target = attempt.activeTarget;
            if (owner == null || target == null || !ownerFrame.validForTick(owner, target)
                    || attempt.attack != null && attempt.attack.obsolete(entity.position(), target.position(),
                    ownerFrame.targetVelocity(target), entity.level().getGameTime(), contactRadius(target))) finishBurst();
        }
        if (!entity.onGround()) {
            tickFalling(owner);
            return;
        }
        fallingVelocity = 0;
        if (owner == null) { settle(null); return; }

        if (moving() && behavior() == Behavior.FOLLOW
                && attempt.followAnchor != null && owner.position().subtract(attempt.followAnchor).horizontalDistanceSqr() > 9) {
            finishBurst();
        }
        if (moving() && reachedGoal()) finishBurst();

        if (!moving()) {
            LivingEntity target = ownerFrame.attackTarget(owner, entity, entity.level().getGameTime());
            boolean following = entity.distanceToSqr(owner) > 100;
            if (delay.tick(target != null || following)) chooseMovement(owner, target, following);
            else movementPhase = delay.waiting() ? Phase.WAITING : Phase.PENDING;
        }
        if (!moving()) {
            entity.updateMovementAnimation(Vec3.ZERO);
            settle(owner);
            return;
        }
        movementPhase = Phase.MOVING;
        moveBurst(owner, false);
    }

    private boolean reachedGoal() {
        return attempt != null && attempt.goal.subtract(entity.position()).horizontalDistanceSqr() <= ARRIVAL_DISTANCE * ARRIVAL_DISTANCE;
    }

    private void settle(ServerPlayer owner) {
        Vec3 from = entity.position();
        entity.move(MoverType.SELF, new Vec3(0, -0.08, 0));
        if (!entity.onGround()) movementPhase = Phase.FALLING;
        entity.tryContact(owner, from, entity.position());
    }

    private void chooseMovement(ServerPlayer owner, LivingEntity target, boolean following) {
        if (target != null) {
            startBurst(target.position(), Behavior.ATTACK, owner);
            attempt.activeTarget = target;
            attempt.attack = new SpiderAttackPrediction(random);
            updateAttackGoal();
            return;
        }
        Vec3 center = following ? owner.position() : entity.position();
        double angle = random.nextDouble() * Math.PI * 2;
        double radius = following ? 3 * Math.sqrt(random.nextDouble()) : 1 + 7 * random.nextDouble();
        // Select a horizontal goal; terrain collision and gravity handle the actual route.
        Vec3 destination = center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
        startBurst(destination, following ? Behavior.FOLLOW : Behavior.ROAM, owner);
        // Wandering may spend this attempt turning in place; urgent movement always travels.
        attempt.turnOnly = !following && random.nextBoolean();
    }

    private double contactRadius(LivingEntity target) {
        return (entity.getBbWidth() + target.getBbWidth()) * 0.5;
    }

    private void updateAttackGoal() {
        LivingEntity target = attempt.activeTarget;
        Vec3 delta = target.position().subtract(entity.position());
        double turnTicks = Math.ceil(Math.abs(Mth.wrapDegrees(
                SpiderMovement.targetYaw(delta, attempt.travelMode) - entity.getYRot())) / SpiderMovement.TURN_DEGREES_PER_TICK);
        attempt.goal = attempt.attack.goal(entity.position(), target.position(), ownerFrame.targetVelocity(target),
                turnTicks, contactRadius(target));
    }

    /** All three decisions enter the same capped 6-9 block attempt. */
    private void startBurst(Vec3 target, Behavior behavior, ServerPlayer owner) {
        attempt = new Attempt(random, target, behavior, owner.position());
        movementPhase = Phase.TURNING;
    }

    private void tickFalling(ServerPlayer owner) {
        fallingVelocity = Math.max(-0.8, (fallingVelocity - 0.08) * 0.98);
        movementPhase = Phase.FALLING;
        if (moving()) moveBurst(owner, true);
        else {
            Vec3 from = entity.position();
            entity.move(MoverType.SELF, new Vec3(0, fallingVelocity, 0));
            if (entity.tryContact(owner, from, entity.position())) return;
            entity.updateMovementAnimation(Vec3.ZERO);
        }
        if (entity.onGround()) {
            fallingVelocity = 0;
            movementPhase = moving() ? Phase.MOVING : delay.waiting() ? Phase.WAITING : Phase.PENDING;
        }
        entity.setDeltaMovement(0, fallingVelocity, 0);
    }

    private void moveBurst(ServerPlayer owner, boolean airborne) {
        Vec3 start = entity.position();
        if (attempt.behavior == Behavior.ATTACK && attempt.attack == null) {
            attempt.attack = new SpiderAttackPrediction(random);
            updateAttackGoal();
        }
        Vec3 delta = attempt.goal.subtract(start);
        if (!attempt.travelStarted && attempt.attack != null && !attempt.attack.calibrated
                && SpiderMovement.aligned(delta, entity.getYRot(), attempt.travelMode)) {
            // Recheck once after the initial turn, then finish any small correction before departure.
            updateAttackGoal();
            attempt.attack.calibrated = true;
            delta = attempt.goal.subtract(start);
        }
        if (!attempt.travelStarted && (attempt.turnOnly || !SpiderMovement.aligned(delta, entity.getYRot(), attempt.travelMode))) {
            entity.setYRot(SpiderMovement.turnYaw(entity.getYRot(), SpiderMovement.targetYaw(delta, attempt.travelMode)));
            movementPhase = airborne ? Phase.FALLING : Phase.TURNING;
            if (attempt.turnOnly && SpiderMovement.aligned(delta, entity.getYRot(), attempt.travelMode)) finishBurst();
            // Turning consumes no horizontal travel budget and never suspends gravity or contact damage.
            if (airborne) {
                entity.move(MoverType.SELF, new Vec3(0, fallingVelocity, 0));
                if (entity.tryContact(owner, start, entity.position())) return;
            } else settle(owner);
            entity.setDeltaMovement(0, airborne ? fallingVelocity : 0, 0);
            if (!entity.isRemoved()) entity.updateMovementAnimation(Vec3.ZERO);
            return;
        }
        // A departed attack may end early, but never bends or turns within the running segment.
        if (!attempt.travelStarted && attempt.attack != null) {
            attempt.attack.depart(start, attempt.goal, attempt.activeTarget.position(),
                    ownerFrame.targetVelocity(attempt.activeTarget), entity.level().getGameTime());
        }
        attempt.travelStarted = true;
        Vec3 heading = SpiderMovement.direction(entity.getYRot(), attempt.travelMode);
        double remaining = Math.min(attempt.burstSpeed, attempt.remainingDistance());
        int pieces = Math.max(1, (int) Math.ceil(remaining / MOVEMENT_PIECE));
        double vertical = airborne ? fallingVelocity : -0.08;
        for (int piece = 0; piece < pieces && moving(); piece++) {
            Vec3 step = attempt.step(heading.scale(attempt.goal.subtract(entity.position()).horizontalDistance()),
                    Math.min(MOVEMENT_PIECE, remaining));
            Vec3 from = entity.position();
            entity.move(MoverType.SELF, new Vec3(step.x, vertical / pieces, step.z));
            Vec3 actual = entity.position().subtract(from);
            attempt.record(actual);
            remaining -= step.horizontalDistance();
            if (entity.tryContact(owner, from, entity.position())) return;
            if (!airborne && entity.horizontalCollision && step.horizontalDistanceSqr() > 1e-10) {
                startClimbing(step, actual);
                return;
            }
            if (reachedGoal() || attempt.finished() || attempt.attack != null && attempt.attack.passedGoal(entity.position())) {
                finishBurst();
                break;
            }
            if (actual.horizontalDistanceSqr() < 1e-10 || remaining <= 1e-9) break;
        }
        entity.setDeltaMovement(0, airborne ? fallingVelocity : 0, 0);
        if (!entity.isRemoved()) entity.updateMovementAnimation(entity.position().subtract(start));
    }

    private void startClimbing(Vec3 requested, Vec3 actual) {
        boolean blockedX = Math.abs(requested.x - actual.x) > 1e-6;
        climbDirection = blockedX ? new Vec3(Math.signum(requested.x), 0, 0)
                : new Vec3(0, 0, Math.signum(requested.z));
        entity.setDeltaMovement(Vec3.ZERO);
        entity.updateMovementAnimation(Vec3.ZERO);
        if (!touchesWall()) {
            finishBurst();
            return;
        }
        climbTicks = 0;
        movementPhase = Phase.CLIMBING;
    }

    private boolean touchesWall() {
        return !SpiderTerrain.clearSweep(entity, entity.position(), entity.position().add(climbDirection.scale(0.04)));
    }

    private void tickClimbing(ServerPlayer owner) {
        if (!touchesWall()) {
            endClimb(true);
            return;
        }
        Vec3 from = entity.position();
        entity.move(MoverType.SELF, new Vec3(0, SpiderMovement.CLIMB_SPEED, 0));
        if (entity.tryContact(owner, from, entity.position())) return;
        climbTicks++;
        // The random draw happens exactly once per complete second of continuous climbing.
        if (SpiderMovement.climbDropCheck(random, climbTicks) || entity.verticalCollision) endClimb(false);
        else if (!touchesWall()) endClimb(true);
        else {
            entity.setMovementAnimation(entity.getY() > from.y ? SpiderMovement.Gait.FORWARD : SpiderMovement.Gait.IDLE);
        }
    }

    private void endClimb(boolean reachedEdge) {
        if (reachedEdge && moving()) {
            double clearance = entity.getBbWidth() + 0.04;
            Vec3 to = entity.position().add(climbDirection.scale(clearance));
            Vec3 landing = SpiderTerrain.groundPosition(entity, to, 1);
            if (landing != null && Math.abs(landing.y - entity.getY()) < 0.12
                    && SpiderTerrain.clearSweep(entity, entity.position(), to)) {
                // Keep moving into the platform on subsequent ticks. Finishing the climb
                // never adds an extra horizontal displacement in this tick.
                crestRemaining = Math.min(clearance, attempt.remainingDistance());
                movementPhase = Phase.CRESTING;
                entity.setDeltaMovement(climbDirection.scale(Math.min(SpiderMovement.CLIMB_SPEED, crestRemaining)));
                entity.setMovementAnimation(SpiderMovement.Gait.FORWARD);
                return;
            }
        }
        releaseWall();
    }

    private void tickCresting(ServerPlayer owner) {
        Vec3 velocity = entity.getDeltaMovement();
        double speed = velocity.horizontalDistance();
        double distance = Math.min(crestRemaining, Math.min(speed, attempt.remainingDistance()));
        if (distance <= 1e-8) {
            releaseWall();
            return;
        }
        // The body just cleared the wall. Keep the climb height for this brief exit;
        // resuming gravity before the feet cross the edge would drop it back behind the wall.
        entity.setDeltaMovement(velocity.scale(distance / speed));
        Vec3 from = entity.position();
        entity.move(MoverType.SELF, entity.getDeltaMovement());
        Vec3 actual = entity.position().subtract(from);
        attempt.record(actual);
        crestRemaining = Math.max(0, crestRemaining - actual.horizontalDistance());
        if (entity.tryContact(owner, from, entity.position())) return;
        if (entity.horizontalCollision || actual.horizontalDistanceSqr() < 1e-10
                || crestRemaining <= 1e-8 || attempt.finished()) {
            releaseWall();
        } else {
            entity.setDeltaMovement(velocity);
            entity.setMovementAnimation(SpiderMovement.Gait.FORWARD);
        }
    }

    private void releaseWall() {
        crestRemaining = 0;
        finishBurst();
        fallingVelocity = -0.08;
        movementPhase = Phase.FALLING;
        entity.setDeltaMovement(0, fallingVelocity, 0);
        entity.updateMovementAnimation(Vec3.ZERO);
    }

    /** Every field required to start, run or finish one movement attempt lives together. */
    static final class Attempt {
        final Behavior behavior;
        Vec3 goal;
        final Vec3 followAnchor;
        LivingEntity activeTarget;
        SpiderAttackPrediction attack;
        double burstSpeed;
        SpiderMovement.Gait travelMode;
        boolean travelStarted;
        boolean turnOnly;
        private double remaining;

        Attempt(RandomSource random, Vec3 goal, Behavior behavior, Vec3 ownerPosition) {
            this.goal = goal;
            this.behavior = behavior;
            remaining = 6 + random.nextDouble() * 3;
            burstSpeed = behavior == Behavior.ROAM ? SpiderMovement.randomRoamSpeed(random) : SpiderMovement.MAX_SPEED;
            followAnchor = behavior == Behavior.FOLLOW ? ownerPosition : null;
            travelMode = SpiderMovement.randomTravelMode(random);
        }

        double remainingDistance() { return remaining; }
        boolean finished() { return remaining<=1e-8; }
        Vec3 step(Vec3 targetDelta,double speed) {
            double distance=targetDelta.horizontalDistance();
            if(finished()||distance<1e-10)return Vec3.ZERO;
            double scale=Math.min(distance,Math.min(remaining,Math.min(SpiderMovement.MAX_SPEED,Math.max(0,speed))))/distance;
            return new Vec3(targetDelta.x*scale,0,targetDelta.z*scale);
        }
        void record(Vec3 movement) { remaining=Math.max(0,remaining-movement.horizontalDistance()); }
    }

    static final class Delay {
        private int recovery;
        private int wanderDelay;

        void reset(int recovery, int wanderDelay) {
            this.recovery = Math.max(0, recovery);
            this.wanderDelay = Math.max(0, wanderDelay);
        }

        boolean waiting() { return recovery > 0; }

        boolean tick(boolean urgent) {
            if (recovery > 0) {
                recovery--;
                return recovery == 0 && urgent;
            }
            if (urgent) return true;
            if (wanderDelay > 0) wanderDelay--;
            return wanderDelay == 0;
        }
    }
}

package net.luojiuoscar.isaac_disaster.entity.familiar.base.chain;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

final class ChainFamiliarMotion {
    private static final double REST_DISTANCE = 0.0001;
    private static final double RESPONSE = 0.35;
    private static final double DECAY = Math.exp(-RESPONSE);
    private static final double MAX_SPEED = 1.5;
    private static final double REST_SPEED = 0.005;
    private Vec3 velocity = Vec3.ZERO;
    private Vec3 positionTarget;
    private int remainingSteps;
    private boolean yawInitialized;
    private float previousYaw;
    private float yaw;

    Vec3 follow(Vec3 current, Vec3 target) {
        Vec3 offset = current.subtract(target);
        double distance = offset.length();
        // Inside the follow area only damp existing momentum; never push away from the leader.
        Vec3 error = distance > FamiliarChain.SPACING
                ? offset.scale((distance - FamiliarChain.SPACING) / distance) : Vec3.ZERO;
        if (distance <= FamiliarChain.SPACING + REST_DISTANCE
                && velocity.lengthSqr() < REST_SPEED * REST_SPEED) {
            velocity = Vec3.ZERO;
            return current;
        }
        return step(current, error);
    }

    private Vec3 step(Vec3 current, Vec3 error) {
        Vec3 spring = velocity.add(error.scale(RESPONSE));
        Vec3 movement = error.add(spring).scale(DECAY).subtract(error);
        velocity = velocity.subtract(spring.scale(RESPONSE)).scale(DECAY);
        movement = movement.lengthSqr() > MAX_SPEED * MAX_SPEED
                ? movement.normalize().scale(MAX_SPEED) : movement;
        velocity = velocity.lengthSqr() > MAX_SPEED * MAX_SPEED
                ? velocity.normalize().scale(MAX_SPEED) : velocity;
        Vec3 next = current.add(movement);
        return next;
    }

    /** Absolute packets include the periodic 400-tick resync, not just actual teleports. */
    boolean acceptPosition(Vec3 current, Vec3 target, int steps, boolean absolute) {
        if (absolute && current.distanceToSqr(target) > 8 * 8) {
            reset();
            return true;
        }
        if (remainingSteps > 0 && target.equals(positionTarget)) return false;
        positionTarget = target;
        remainingSteps = Math.max(1, steps);
        return false;
    }

    Vec3 tickPosition(Vec3 current) {
        if (remainingSteps <= 0) return current;
        Vec3 next = remainingSteps == 1 ? positionTarget : current.lerp(positionTarget, 1.0 / remainingSteps);
        remainingSteps--;
        return next;
    }

    void turnTo(float targetYaw) {
        if (!yawInitialized) {
            previousYaw = targetYaw;
            yaw = targetYaw;
            yawInitialized = true;
            return;
        }
        previousYaw = yaw;
        yaw += Mth.clamp(Mth.wrapDegrees(targetYaw - yaw), -18, 18);
    }

    float renderYaw(float partialTick) {
        return Mth.rotLerp(partialTick, previousYaw, yaw);
    }

    void reset() {
        velocity = Vec3.ZERO;
        positionTarget = null;
        remainingSteps = 0;
        yawInitialized = false;
    }
}

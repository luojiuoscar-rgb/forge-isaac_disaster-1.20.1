package net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Formation placement, movement and display heading without entity or packet ownership. */
final class MomKnifeMotion {
    static final float ATTACK_PITCH_STEP = 180.0F / MomKnifeAttack.MIN_AIM_TICKS;
    private static final double FOLLOW_RESPONSE = 0.35;
    private static final double FOLLOW_MAX_SPEED = 1.5;

    private Vec3 positionTarget;
    private int remainingSteps;
    private boolean yawInitialized;
    private float previousYaw;
    private float yaw;
    private float previousPitch;
    private float pitch;
    private float previousTwist;
    private float twist;

    /** Each arc holds 5, 9, 13, ... knives; a partially filled arc still counts as a layer. */
    static int formationLayers(int knifeCount) {
        int remaining = Math.max(1, knifeCount);
        int layers = 1;
        for (int capacity = 5; remaining > capacity; capacity += 4) {
            remaining -= capacity;
            layers++;
        }
        return layers;
    }

    /** Places each knife in a stable slot on concentric arcs behind its owner. */
    static Vec3 formationPosition(Vec3 ownerPosition, double width, double height, float yaw, int index) {
        int slot = Math.max(0, index);
        int ring = 0;
        int capacity = 5;
        while (slot >= capacity) {
            slot -= capacity;
            ring++;
            capacity += 4;
        }

        double angle = 0;
        if (slot > 0) {
            double angleStep = 80.0 / Math.max(1, (capacity - 1) / 2);
            int offset = (slot + 1) / 2;
            angle = angleStep * offset * (slot % 2 == 1 ? -1 : 1);
        }
        double radius = Math.max(width * 0.5 + 0.65, 0.65) + ring * 0.55;
        double radians = Math.toRadians(yaw + angle);
        return ownerPosition.add(Math.sin(radians) * radius, height * 0.7, -Math.cos(radians) * radius);
    }

    static float facingYaw(Vec3 from, Vec3 target, float fallback) {
        Vec3 direction = target.subtract(from);
        return direction.horizontalDistanceSqr() < 1e-8 ? fallback
                : (float) (Mth.atan2(-direction.x, direction.z) * Mth.RAD_TO_DEG);
    }

    static Vec3 follow(Vec3 current, Vec3 target) {
        return fly(current, target, Math.min(current.distanceTo(target) * FOLLOW_RESPONSE, FOLLOW_MAX_SPEED));
    }

    static Vec3 fly(Vec3 current, Vec3 target, double speed) {
        Vec3 delta = target.subtract(current);
        double distance = delta.length();
        if (distance == 0) {
            return current;
        }
        if (distance <= speed) {
            return target;
        }
        return current.add(delta.scale(speed / distance));
    }

    void acceptPosition(Vec3 target, int steps) {
        if (remainingSteps > 0 && target.equals(positionTarget)) {
            return;
        }
        positionTarget = target;
        remainingSteps = Math.max(1, steps);
    }

    Vec3 tickPosition(Vec3 current) {
        if (remainingSteps <= 0) {
            return current;
        }
        Vec3 next = remainingSteps == 1 ? positionTarget : current.lerp(positionTarget, 1.0 / remainingSteps);
        remainingSteps--;
        return next;
    }

    void turnTo(float targetYaw, float maxStep) {
        if (!yawInitialized) {
            previousYaw = targetYaw;
            yaw = targetYaw;
            yawInitialized = true;
            return;
        }
        previousYaw = yaw;
        yaw += Mth.clamp(Mth.wrapDegrees(targetYaw - yaw), -maxStep, maxStep);
    }

    float renderYaw(float partialTick) {
        return Mth.rotLerp(partialTick, previousYaw, yaw);
    }

    void turnPitchTo(float targetPitch, float maxStep) {
        previousPitch = pitch;
        pitch += Mth.clamp(targetPitch - pitch, -maxStep, maxStep);
    }

    float renderPitch(float partialTick) {
        return Mth.lerp(partialTick, previousPitch, pitch);
    }

    void turnTwistTo(float targetTwist, float maxStep) {
        previousTwist = twist;
        twist += Mth.clamp(Mth.wrapDegrees(targetTwist - twist), -maxStep, maxStep);
    }

    float renderTwist(float partialTick) {
        return Mth.rotLerp(partialTick, previousTwist, twist);
    }

    void reset() {
        positionTarget = null;
        remainingSteps = 0;
        yawInitialized = false;
        previousPitch = 0;
        pitch = 0;
        previousTwist = 0;
        twist = 0;
    }
}

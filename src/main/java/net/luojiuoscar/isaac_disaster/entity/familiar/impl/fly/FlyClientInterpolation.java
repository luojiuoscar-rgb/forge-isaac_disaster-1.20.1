package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Client motion between server samples, including periodic absolute position corrections. */
final class FlyClientInterpolation {
    private static final double MAX_SMOOTH_CORRECTION_SQR = 4.0 * 4.0;
    record Pose(Vec3 position, float yaw, float pitch) { }

    private Vec3 target;
    private float yaw;
    private float pitch;
    private int remaining;

    boolean update(Vec3 current, Vec3 target, float yaw, float pitch, int steps, boolean absolute) {
        // Vanilla also sends absolute packets periodically during ordinary movement.
        if (absolute && current.distanceToSqr(target) > MAX_SMOOTH_CORRECTION_SQR) {
            clear();
            return false;
        }
        this.target = target;
        this.yaw = yaw;
        this.pitch = pitch;
        remaining = Math.max(1, steps);
        return true;
    }

    Pose tick(Vec3 current, float currentYaw, float currentPitch) {
        if (remaining <= 0) return null;
        Pose result = new Pose(current.lerp(target, 1.0 / remaining),
                currentYaw + Mth.wrapDegrees(yaw - currentYaw) / remaining,
                currentPitch + (pitch - currentPitch) / remaining);
        remaining--;
        return result;
    }

    void clear() {
        remaining = 0;
    }
}

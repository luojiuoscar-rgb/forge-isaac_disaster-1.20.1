package net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife;

import net.minecraft.world.phys.Vec3;

/** Shared handle-tail pivot and blade axis used by rendering and server collision. */
public record MomKnifePose(float yaw, float pitch, float twist) {
    public static final double BLADE_BASE_DISTANCE = 0.10D;
    public static final double BLADE_TIP_DISTANCE = 0.84D;
    public static final double BLADE_RADIUS = 0.08D;

    /** Twist rotates the knife around this axis without moving its blade centerline. */
    public Vec3 bladePoint(Vec3 anchor, double distance) {
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        double horizontal = distance * Math.sin(pitchRadians);
        return anchor.add(-Math.sin(yawRadians) * horizontal,
                -distance * Math.cos(pitchRadians), Math.cos(yawRadians) * horizontal);
    }
}

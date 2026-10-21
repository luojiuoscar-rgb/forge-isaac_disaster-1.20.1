package net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife;

import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Small, world-independent steps for rising, aiming and a fixed-range thrust. */
final class MomKnifeAttack {
    static final int RISE_TICKS = 3;
    static final int MAX_RISE_WAIT_TICKS = 6;
    static final int MIN_AIM_TICKS = 2;
    static final int MAX_AIM_TICKS = 6;
    static final int PAUSE_TICKS = 5;
    static final int RETURN_TICKS = 8;
    static final int MIN_COOLDOWN = 20;
    static final int MAX_COOLDOWN = 30;
    static final double MIN_RISE_HEIGHT = 0.5;
    static final double MAX_RISE_HEIGHT = 2.0;
    static final double THRUST_SPEED = 1.25;
    static final double RETURN_SPEED = 0.2;

    private MomKnifeAttack() {}

    static double thrustDistance(int layerCount) {
        return 6.0 + 0.5 * Math.max(1, layerCount);
    }

    static double riseHeight(double randomFraction) {
        return Mth.lerp(Mth.clamp(randomFraction, 0, 1), MIN_RISE_HEIGHT, MAX_RISE_HEIGHT);
    }

    static Vec3 rise(Vec3 origin, double height, double age) {
        return origin.add(0, height * Mth.clamp(age / RISE_TICKS, 0, 1), 0);
    }

    /** Lift backward from the follow pose, keeping the cutting edge below the blade. */
    static MomKnifePose aim(Vec3 anchor, Vec3 target, MomKnifePose start, double age, int durationTicks) {
        Vec3 direction = target.subtract(anchor);
        double length = direction.length();
        if (length < 1e-8) return start;
        float yaw = MomKnifeMotion.facingYaw(anchor, target, start.yaw() - 180.0F) + 180.0F;
        float pitch = (float) -Math.toDegrees(Math.acos(Mth.clamp(-direction.y / length, -1, 1)));
        float progress = (float) smooth(age / Math.max(1, durationTicks));
        if (progress <= 0) return start;
        return new MomKnifePose(Mth.rotLerp(progress, start.yaw(), yaw),
                Mth.lerp(progress, start.pitch(), pitch),
                0.0F);
    }

    /** Advances the handle along its locked launch direction without stopping at contact. */
    static Vec3 advance(Vec3 anchor, MomKnifePose pose, double remainingDistance) {
        Vec3 direction = pose.bladePoint(Vec3.ZERO, 1);
        double travel = Math.min(THRUST_SPEED, Math.max(0, remainingDistance));
        return anchor.add(direction.scale(travel));
    }

    /** Reports whether the swept blade intersects the target during one movement step. */
    static boolean contacts(Vec3 current, Vec3 next, MomKnifePose pose, AABB target) {
        AABB expanded = target.inflate(MomKnifePose.BLADE_RADIUS);
        Vec3 currentBase = pose.bladePoint(current, MomKnifePose.BLADE_BASE_DISTANCE);
        Vec3 currentTip = pose.bladePoint(current, MomKnifePose.BLADE_TIP_DISTANCE);
        Vec3 nextBase = pose.bladePoint(next, MomKnifePose.BLADE_BASE_DISTANCE);
        Vec3 nextTip = pose.bladePoint(next, MomKnifePose.BLADE_TIP_DISTANCE);
        return SweptCollision.segmentAabb(currentBase, nextTip, expanded).hit()
                || SweptCollision.segmentAabb(currentTip, nextBase, expanded).hit();
    }

    /** Retraces the launch line at thrust speed, clamping the last step to the original anchor. */
    static Vec3 retract(Vec3 current, Vec3 launchOrigin) {
        return MomKnifeMotion.fly(current, launchOrigin, THRUST_SPEED);
    }

    static MomKnifePose returnPose(float targetYaw, MomKnifePose start, double age) {
        float progress = (float) smooth(age / RETURN_TICKS);
        return new MomKnifePose(Mth.rotLerp(progress, start.yaw(), targetYaw),
                start.pitch() * (1 - progress), start.twist() * (1 - progress));
    }

    private static double smooth(double progress) {
        double clamped = Mth.clamp(progress, 0, 1);
        return clamped * clamped * (3 - 2 * clamped);
    }
}

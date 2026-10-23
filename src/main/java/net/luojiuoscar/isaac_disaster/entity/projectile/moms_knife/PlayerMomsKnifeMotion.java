package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifePose;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Radial flight and blade geometry shared by authoritative collision and client presentation. */
public final class PlayerMomsKnifeMotion {
    private PlayerMomsKnifeMotion() {}

    public static double range(double playerRange, double chargeFraction) {
        if (!Double.isFinite(playerRange) || !Double.isFinite(chargeFraction)) return 0;
        return Math.min(Math.max(0, playerRange) / 2, 8) * Mth.clamp(chargeFraction, 0, 1);
    }

    public static double duration(double range, double speed) {
        return range > 0 && Double.isFinite(range) && speed > 0 && Double.isFinite(speed)
                ? 4 * range / speed : 0;
    }

    /** Constant inward acceleration starts at launch and continues through the turnaround. */
    public static double distance(double range, double speed, double age) {
        double duration = duration(range, speed);
        if (duration <= 0 || !Double.isFinite(age)) return 0;
        double fraction = Mth.clamp(age / duration, 0, 1);
        return 4 * range * fraction * (1 - fraction);
    }

    public static Vec3 idlePosition(Player owner, float partialTick) {
        return owner.getEyePosition(partialTick).add(0, -0.15 * owner.getBbHeight(), 0)
                .add(owner.getViewVector(partialTick).scale(Math.max(0.6, owner.getBbWidth())));
    }

    public static MomKnifePose pose(float yaw, float pitch) {
        return new MomKnifePose(yaw + 180, pitch - 90, 0);
    }

    public static Blade blade(Vec3 idle, float yaw, float pitch, double distance) {
        MomKnifePose pose = pose(yaw, pitch);
        Vec3 anchor = idle.add(pose.bladePoint(Vec3.ZERO, 1).scale(distance));
        return new Blade(anchor, pose.bladePoint(anchor, MomKnifePose.BLADE_BASE_DISTANCE),
                pose.bladePoint(anchor, MomKnifePose.BLADE_TIP_DISTANCE));
    }

    /** Samples along the blade as it sweeps, rather than testing only its tip or two diagonals. */
    public static boolean contacts(Blade from, Blade to, AABB target) {
        AABB expanded = target.inflate(MomKnifePose.BLADE_RADIUS);
        if (SweptCollision.segmentAabb(from.base(), from.tip(), expanded).hit()
                || SweptCollision.segmentAabb(to.base(), to.tip(), expanded).hit()) return true;
        int samples = (int) Math.ceil((MomKnifePose.BLADE_TIP_DISTANCE
                - MomKnifePose.BLADE_BASE_DISTANCE) / MomKnifePose.BLADE_RADIUS);
        for (int i = 0; i <= samples; i++) {
            double fraction = (double) i / samples;
            if (SweptCollision.segmentAabb(from.base().lerp(from.tip(), fraction),
                    to.base().lerp(to.tip(), fraction), expanded).hit()) return true;
        }
        return false;
    }

    public record Blade(Vec3 anchor, Vec3 base, Vec3 tip) {
        public AABB bounds() {
            return new AABB(base, tip).inflate(MomKnifePose.BLADE_RADIUS);
        }
    }
}

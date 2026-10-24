package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.entity.familiar.impl.mom_knife.MomKnifePose;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** One attached blade's facing and per-tick contacts, owned by its lightweight fetus. */
public final class FetusMomsKnifeState {
    private Vec3 direction;
    private long damageTick = Long.MIN_VALUE;
    private final Set<UUID> hitTargets = new HashSet<>();

    public FetusMomsKnifeState(Vec3 velocity) {
        direction = PlayerMomsKnifeMotion.unit(velocity);
    }

    public static double modelScale(double renderScale) {
        return Double.isFinite(renderScale) ? Math.max(0, renderScale) * 0.35 : 0;
    }

    /** The young humanoid occupies [-1.5, -0.375] vertically after the model's X flip. */
    public static double modelCenterOffset(double renderScale) {
        return 0.9375 * modelScale(renderScale);
    }

    /** The handle-to-tip length matches the visible young humanoid's height. */
    public static double scale(double renderScale) {
        return 1.125 * modelScale(renderScale) / MomKnifePose.BLADE_TIP_DISTANCE;
    }

    public Vec3 facing(Vec3 velocity) {
        if (Double.isFinite(velocity.lengthSqr()) && velocity.lengthSqr() > 1.0E-12) direction = velocity.normalize();
        return direction;
    }

    public static PlayerMomsKnifeMotion.Blade blade(Vec3 center, Vec3 direction, double renderScale, double width) {
        double scale = scale(renderScale);
        Vec3 anchor = center.add(PlayerMomsKnifeMotion.unit(direction).scale(Math.max(width / 2, 0.3 * modelScale(renderScale))));
        return PlayerMomsKnifeMotion.blade(anchor, direction, scale);
    }

    public List<PlayerMomsKnifeMotion.Blade> sweep(Vec3 start, Vec3 end, Vec3 velocity, double renderScale, double width) {
        Vec3 previous = direction;
        Vec3 next = facing(velocity);
        double angle = Math.toDegrees(Math.acos(Mth.clamp(previous.dot(next), -1, 1)));
        int steps = Math.max(1, (int) Math.ceil(angle / 5));
        List<PlayerMomsKnifeMotion.Blade> path = new ArrayList<>(steps + 1);
        for (int i = 0; i <= steps; i++) {
            double fraction = (double) i / steps;
            path.add(blade(start.lerp(end, fraction),
                    PlayerMomsKnifeMotion.interpolateDirection(previous, next, fraction), renderScale, width));
        }
        return path;
    }

    public boolean markHit(long tick, UUID target) {
        if (damageTick != tick) {
            damageTick = tick;
            hitTargets.clear();
        }
        return hitTargets.add(target);
    }
}

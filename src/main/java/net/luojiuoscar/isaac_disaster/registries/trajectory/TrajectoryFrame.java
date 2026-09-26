package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.minecraft.world.phys.Vec3;

/** Immutable launch reference for primary paths. Offset frames live in TrajectoryKinematics. */
public final class TrajectoryFrame {
    private final Vec3 origin;
    private final Vec3 mainAxis;
    private final boolean laser;

    public TrajectoryFrame(IBulletObject bullet, Vec3 origin, Vec3 mainAxis) {
        this.origin = finite(origin) ? origin : bullet.getPosition();
        this.mainAxis = safeDirection(mainAxis, bullet.getVelocity());
        this.laser = ModAttackTypes.LASER.getId().equals(bullet.getRootTypeId());
    }

    public boolean laser() {
        return laser;
    }

    public Vec3 baseline(double distance) {
        return origin.add(mainAxis.scale(Math.max(0, distance)));
    }

    public static double smootherstep(double value) {
        double t = clamp(value, 0, 1);
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Vec3 safeDirection(Vec3 preferred, Vec3 fallback) {
        Vec3 value = finite(preferred) && preferred.lengthSqr() > 1e-10 ? preferred : fallback;
        return finite(value) && value.lengthSqr() > 1e-10 ? value.normalize() : new Vec3(1, 0, 0);
    }

    private static boolean finite(Vec3 v) {
        return v != null && Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z);
    }
}

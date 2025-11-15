package net.luojiuoscar.isaac_disaster.manager.attack.managers;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum AttackTrajectory {

    WIGGLE_WORM(ctx -> {
        double phaseScale = 1;
        double t0 = ctx.distance * phaseScale;
        double t1 = (ctx.distance + ctx.deltaDistance) * phaseScale;

        double y0 = (t0 < Math.PI) ? Math.cos(t0) - 1 : 2 * Math.cos(t0);
        double y1 = (t1 < Math.PI) ? Math.cos(t1) - 1 : 2 * Math.cos(t1);

        double deltaY = y1 - y0;

        Vec3 dir = ctx.dir;
        Vec3 right = new Vec3(-dir.z, 0, dir.x).normalize();
        double offsetScale = 0.5 + ctx.amplifier * 0.25;

        Vec3 posOffset = right.scale(deltaY * offsetScale);
        Vec3 velOffset = right.scale(deltaY * 0.05);

        // 默认返回原速度
        return new TrajectoryResult(posOffset, velOffset);
    }),
    TINY_PLANET(ctx -> {
        LivingEntity owner = ctx.owner;
        if (owner == null || ctx.pos == null) return TrajectoryResult.ZERO;

        Vec3 bulletPos = ctx.pos;

        double targetRadius = 3.0 + ctx.amplifier;
        double angularSpeed = 0.35;
        double yAmplitude = 0.2;
        double yFrequency = 1.2;

        Vec3 ownerCenter = owner.position().add(0, owner.getBbHeight() * 0.5, 0);
        Vec3 rel = bulletPos.subtract(ownerCenter);

        double currentR = Math.max(0.001, Math.sqrt(rel.x * rel.x + rel.z * rel.z));
        rel = rel.scale(targetRadius / currentR);

        double angle = Math.atan2(rel.z, rel.x);
        double nextAngle = angle + angularSpeed;

        Vec3 desiredRel = new Vec3(
                Math.cos(nextAngle) * targetRadius,
                Math.sin(ctx.distance * yFrequency) * yAmplitude,
                Math.sin(nextAngle) * targetRadius
        );

        Vec3 desiredPos = ownerCenter.add(desiredRel);

        Vec3 desiredVel = desiredPos.subtract(bulletPos).normalize().scale(ctx.deltaDistance);
        Vec3 currentVel = ctx.dir.scale(ctx.deltaDistance);
        Vec3 velOffset = desiredVel.subtract(currentVel);

        // 明确返回子弹速度
        return new TrajectoryResult(Vec3.ZERO, velOffset);
    });

    private static final Map<Integer, AttackTrajectory> BY_ID = new HashMap<>();

    static {
        for (AttackTrajectory t : values()) BY_ID.put(t.getId(), t);
    }

    private final Function<TrajectoryContext, TrajectoryResult> offsetSupplier;

    AttackTrajectory(Function<TrajectoryContext, TrajectoryResult> off) {
        this.offsetSupplier = off;
    }

    public int getId() { return ordinal(); }

    public TrajectoryResult getResult(TrajectoryContext ctx) {
        return offsetSupplier.apply(ctx);
    }

    public static AttackTrajectory byId(int id) {
        return BY_ID.getOrDefault(id, WIGGLE_WORM);
    }

    // ================================
    // 轨迹上下文
    // ================================
    public static class TrajectoryContext {
        public final Vec3 dir;
        public final double distance;
        public final double deltaDistance;
        public final LivingEntity owner;
        public final Vec3 pos;
        public final int amplifier;

        public TrajectoryContext(Vec3 dir, double distance, double deltaDistance,
                                 @Nullable LivingEntity owner, Vec3 pos, int amplifier) {
            this.dir = dir;
            this.distance = distance;
            this.deltaDistance = deltaDistance;
            this.owner = owner;
            this.pos = pos;
            this.amplifier = Math.max(0, amplifier);
        }
    }

    public record TrajectoryResult(Vec3 positionOffset, Vec3 velocityOffset, double speedDelta) {

        public static final TrajectoryResult ZERO = new TrajectoryResult(Vec3.ZERO, Vec3.ZERO, 0.0);

        // 简化构造函数，默认 speedDelta = 0
        public TrajectoryResult(Vec3 positionOffset, Vec3 velocityOffset) {
            this(positionOffset, velocityOffset, 0.0);
        }

    }

}

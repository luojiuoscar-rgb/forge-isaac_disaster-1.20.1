package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryModulePriority;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryMotion;
import net.minecraft.world.phys.Vec3;

/** Semi-implicit gravity acting on the current primary velocity. */
public final class GravityTrajectoryModule extends TrajectoryModule {
    @Override
    public int priority() {
        return TrajectoryModulePriority.GRAVITY.priority();
    }

    @Override
    public RangeCostPolicy rangeCostPolicy() {
        return RangeCostPolicy.REPLACE;
    }

    public static final double ACCELERATION = 0.05;

    @Override
    public boolean appliesTo(IBulletObject bullet) {
        return ModAttackTypes.BULLET.getId().equals(bullet.getRootTypeId()) && !bullet.noGravity();
    }

    @Override
    public TrajectoryMotion apply(TrajectoryContext ctx) {
        if (ctx.frame.laser() || ctx.bulletObject.noGravity()) return null;
        double dt = Math.max(0, ctx.deltaTicks);
        Vec3 acceleration = new Vec3(0, -ACCELERATION, 0);
        Vec3 velocity = ctx.velocity.add(acceleration.scale(dt));
        Vec3 movement = velocity.scale(dt);
        double cost = movement.length();
        if (!ctx.composed) {
            double remaining = Math.max(0, ctx.bulletObject.getRange() - ctx.bulletObject.getTraveled());
            if (cost > remaining) {
                movement = movement.scale(remaining / cost);
                cost = remaining;
            }
        }

        ctx.runtimeState.path().resume();
        return new TrajectoryMotion(
            ctx.position.add(movement),
            velocity,
            acceleration,
            velocity,
            ctx.baseVelocity.length(),
            TrajectoryMotion.ControlMode.INTENT,
            TrajectoryMotion.CompositionMode.RELATIVE,
            cost);
    }
}

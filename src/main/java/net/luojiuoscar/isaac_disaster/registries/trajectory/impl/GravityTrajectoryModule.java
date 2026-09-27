package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryModulePriority;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryMotion;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/** Semi-implicit gravity acting on the current primary velocity. */
public final class GravityTrajectoryModule extends TrajectoryModule<GravityTrajectoryModule.State> {
    public static final double ACCELERATION = 0.05;

    public static final class State implements TrajectoryState {
        public static final State INSTANCE = new State();

        private State() {}

        @Override
        public State copy() {
            return INSTANCE;
        }
    }

    @Override
    public int priority() {
        return TrajectoryModulePriority.GRAVITY.priority();
    }

    @Override
    public RangeCostPolicy rangeCostPolicy() {
        return RangeCostPolicy.REPLACE;
    }

    @Override
    public State createState() {
        return State.INSTANCE;
    }

    @Override
    protected Class<State> stateClass() {
        return State.class;
    }

    @Override
    public void writeState(FriendlyByteBuf buffer, State state) {}

    @Override
    public State readState(FriendlyByteBuf buffer) {
        return State.INSTANCE;
    }

    @Override
    public boolean appliesTo(IBulletObject bullet) {
        return ModAttackTypes.BULLET.getId().equals(bullet.getRootTypeId()) && !bullet.noGravity();
    }

    @Override
    protected TrajectoryMotion applyTyped(TrajectoryContext ctx, State state) {
        if (ctx.frame.laser() || ctx.bulletObject.noGravity()) return null;
        double dt = Math.max(0, ctx.input.deltaTicks());
        Vec3 acceleration = new Vec3(0, -ACCELERATION, 0);
        Vec3 velocity = ctx.input.velocity().add(acceleration.scale(dt));
        Vec3 movement = velocity.scale(dt);
        double cost = movement.length();
        if (!ctx.composed) {
            double remaining =
                    Math.max(0, ctx.bulletObject.getRange() - ctx.bulletObject.getTraveled());
            if (cost > remaining) {
                movement = movement.scale(remaining / cost);
                cost = remaining;
            }
        }

        state.resume();
        return new TrajectoryMotion(
                ctx.input.position().add(movement),
                velocity,
                acceleration,
                ctx.input.baseVelocity().length(),
                TrajectoryMotion.CompositionMode.RELATIVE,
                cost);
    }
}

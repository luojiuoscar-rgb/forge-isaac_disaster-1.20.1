package net.luojiuoscar.isaac_disaster.registries.trajectory.impl;

import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeEntity;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ReturningKnifeTrajectory;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryModulePriority;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryMotion;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class TinyPlanetReturningMomsKnifeTrajectoryModule
        extends TrajectoryModule<TinyPlanetReturningMomsKnifeTrajectoryModule.State>
        implements ReturningKnifeTrajectory {
    public static final class State implements TrajectoryState {
        public State copy() { return new State(); }
    }

    @Override
    public State createState() { return new State(); }

    @Override
    protected Class<State> stateClass() { return State.class; }

    @Override
    public void writeState(FriendlyByteBuf buffer, State state) {}

    @Override
    public State readState(FriendlyByteBuf buffer) { return new State(); }

    @Override
    public int priority() { return TrajectoryModulePriority.TINY_PLANET_RETURNING_MOMS_KNIFE.priority(); }

    @Override
    public boolean appliesTo(IBulletObject bullet) {
        return bullet instanceof PlayerMomsKnifeEntity knife
                && knife.getMode() == PlayerMomsKnifeEntity.Mode.RETURNING && knife.isInFlight();
    }

    @Override
    public double duration(double straightDuration) {
        return Double.isFinite(straightDuration) && straightDuration > 0
                ? 5 * Math.max(1, Math.ceil(straightDuration / 3)) : 0;
    }

    @Override
    public boolean suppressesHoming() { return true; }

    private static double progress(double age, double duration) {
        return duration > 0 && Double.isFinite(duration) && Double.isFinite(age)
                ? Mth.clamp(age / duration, 0, 1) : 0;
    }

    private static double turns(double duration) {
        return Double.isFinite(duration) ? Math.max(0, duration / 5) : 0;
    }

    @Override
    public int sweepSegments(double fromAge, double toAge, double duration) {
        double span = Math.abs(progress(toAge, duration) - progress(fromAge, duration));
        return Math.max(1, (int) Math.ceil(span * turns(duration) * 72));
    }

    @Override
    public PlayerMomsKnifeMotion.Blade sampleLocal(Vec3 heldPosition, Vec3 direction,
                                                  double range, double duration, double age) {
        double u = progress(age, duration);
        double distance = 4 * Math.max(0, range) * u * (1 - u);
        // Exact end poses avoid accumulated phase error and always dock at the original slot.
        double angle = 2 * Math.PI * turns(duration) * Math.min(u, 1 - u);
        Vec3 facing = PlayerMomsKnifeMotion.unit(direction);
        Vec3 radial = heldPosition.add(facing.scale(distance));
        double angularSpeed = 2 * Math.PI * turns(duration) * (u < 0.5 ? 1 : -1);
        Vec3 tangent = facing.scale(4 * Math.max(0, range) * (1 - 2 * u))
                .add(new Vec3(radial.z, 0, -radial.x).scale(angularSpeed));
        return PlayerMomsKnifeMotion.blade(rotate(radial, angle), rotate(PlayerMomsKnifeMotion.unit(tangent), angle));
    }

    private static Vec3 rotate(Vec3 vector, double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Vec3(vector.x * c + vector.z * s, vector.y, vector.z * c - vector.x * s);
    }

    @Override
    protected TrajectoryMotion applyTyped(TrajectoryContext context, State state) {
        if (!(context.bulletObject instanceof PlayerMomsKnifeEntity knife) || knife.getOwner() == null) return null;
        Vec3 position = knife.getRenderAnchor(knife.getOwner(), 0);
        Vec3 velocity = position.subtract(context.input.position());
        return new TrajectoryMotion(position, velocity, Vec3.ZERO, velocity.length());
    }
}

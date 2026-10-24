package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletCollisionState;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerCounts;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySequence;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/** One throw's interface state, without the ordinary bullet simulation or split lifecycle. */
final class PlayerMomsKnifeAttackState {
    @Nullable final AttackContext context;
    final CompositeTrigger triggers;
    final SplitSequence splits = new SplitSequence();
    final SplitTriggerCounts splitCounts = new SplitTriggerCounts();
    final BulletCollisionState collisions = new BulletCollisionState(Set.of());
    final TrajectoryRuntime trajectory;
    final double range;
    Vec3 velocity;
    Vec3 acceleration = Vec3.ZERO;
    double traveled;
    int age;
    private long lastFrame = Long.MIN_VALUE;

    PlayerMomsKnifeAttackState(@Nullable AttackContext source, Vec3 origin, Vec3 direction, double range, Vec3 velocity) {
        this.range = range;
        this.velocity = velocity;
        triggers = source == null ? new CompositeTrigger() : source.getTrigger().copy();
        if (source == null) context = null;
        else {
            triggers.removeAll(ModExecutableEffects.BULLET_BOUNCE_ON_BLOCK.get());
            triggers.removeAll(ModExecutableEffects.BULLET_BOUNCE_ON_ENTITY.get());
            context = source.toBuilder().attackType(ModAttackTypes.MOMS_KNIFE.get())
                    .position(origin).direction(direction).trigger(triggers)
                    .splitSequence(new SplitSequence()).trajectorySequence(new TrajectorySequence()).build();
            context.freeze();
        }
        trajectory = new TrajectoryRuntime(origin, direction, List.of());
        trajectory.kinematics().position(origin);
        trajectory.kinematics().velocity(velocity);
    }

    void advance(List<PlayerMomsKnifeMotion.Blade> path, long now) {
        if (lastFrame == now) return;
        lastFrame = now;
        age++;
        collisions.damagedEntities().clear();
        Vec3 end = path.get(path.size() - 1).anchor();
        Vec3 next = end.subtract(path.get(0).anchor());
        acceleration = next.subtract(velocity);
        velocity = next;
        double distance = 0;
        for (int i = 1; i < path.size(); i++) distance += path.get(i).anchor().distanceTo(path.get(i - 1).anchor());
        traveled += distance;
        trajectory.advance(distance);
        trajectory.kinematics().position(end);
        trajectory.kinematics().velocity(velocity);
    }

    void setVelocity(Vec3 value) {
        acceleration = value.subtract(velocity);
        velocity = value;
        trajectory.kinematics().velocity(value);
    }
}

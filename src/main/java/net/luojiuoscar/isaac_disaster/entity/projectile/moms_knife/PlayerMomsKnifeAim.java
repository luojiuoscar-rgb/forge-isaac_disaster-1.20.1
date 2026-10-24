package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.luojiuoscar.isaac_disaster.helper.TrackingTargetRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Each detached shot converges from its scattered origin onto the current eye ray. */
public final class PlayerMomsKnifeAim {
    private PlayerMomsKnifeAim() {}

    public static Vec3 point(ServerPlayer owner) {
        Vec3 eye = owner.getEyePosition();
        Vec3 look = PlayerMomsKnifeMotion.unit(owner.getLookAngle());
        Vec3 end = eye.add(look.scale(24));
        var targets = owner.serverLevel().getEntitiesOfClass(LivingEntity.class,
                new AABB(eye, end).inflate(1.0E-7), candidate -> !candidate.isRemoved() && !candidate.isSpectator()
                        && TrackingTargetRules.eligible(owner.serverLevel(), owner, candidate));
        return firstHit(eye, end, targets.stream().map(Entity::getBoundingBox).toList())
                .orElseGet(() -> eye.add(look.scale(12)));
    }

    static Optional<Vec3> firstHit(Vec3 start, Vec3 end, Iterable<AABB> targets) {
        Vec3 closest = null;
        double distance = Double.POSITIVE_INFINITY;
        for (AABB target : targets) {
            Optional<Vec3> hit = target.contains(start) ? Optional.of(start) : target.clip(start, end);
            if (hit.isPresent() && start.distanceToSqr(hit.get()) < distance) {
                closest = hit.get();
                distance = start.distanceToSqr(closest);
            }
        }
        return Optional.ofNullable(closest);
    }
}

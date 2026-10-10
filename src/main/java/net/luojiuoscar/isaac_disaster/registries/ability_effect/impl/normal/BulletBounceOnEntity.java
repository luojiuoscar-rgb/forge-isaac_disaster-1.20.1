package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class BulletBounceOnEntity implements IAbilityEffect {
    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.BULLET_HIT_ENTITY_AFTER;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof IsaacAttackAfterHitEvent event)) return false;

        IBulletObject bullet = event.getBulletObject();
        if (bullet == null) return false;
        if (bullet.isPiercing()) return true;

        double speed = bullet.getVelocity().length();
        Vec3 contact = event.getHit().getLocation();

        // 50% 概率弹向最近敌对生物
        if (Math.random() < 0.5) {
            LivingEntity target = EntityHelper.findNearestTrackingTarget(
                    bullet.getBulletLevel(),
                    bullet.getOwner(),
                    bullet.getCenter(),
                    bullet.getHomingRange(),
                    e -> !bullet.getDamagedEntities().contains(e.getUUID())
            );

            if (target != null) {
                Vec3 dir = target.getEyePosition().subtract(bullet.getCenter()).normalize();
                bullet.setCenter(offsetFromContact(contact, dir));
                bullet.redirectTrajectory(dir.scale(speed));
                event.setCanceled(true);
                return true;
            }
        }

        // 否则随机弹射
        double theta = Math.random() * 2 * Math.PI;
        double phi = Math.acos(2 * Math.random() - 1);
        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.sin(phi) * Math.sin(theta);
        double z = Math.cos(phi);
        Vec3 randomDir = new Vec3(x, y, z).normalize();

        // 清空除了当前生物以外的所有生物
        UUID lastHit = bullet.getDamagedEntities().last();
        bullet.getDamagedEntities().clear();
        if (lastHit != null) bullet.getDamagedEntities().add(lastHit);

        bullet.redirectTrajectory(randomDir.scale(speed));
        bullet.setCenter(offsetFromContact(contact, randomDir));
        event.setCanceled(true);

        return true;
    }

    /** Offsets a bounced bullet slightly along its outgoing direction before the next sweep. */
    private static Vec3 offsetFromContact(Vec3 contact, Vec3 direction) {
        return contact.add(direction.lengthSqr() > 1.0E-12D ? direction.normalize().scale(1.0E-4D) : Vec3.ZERO);
    }

}

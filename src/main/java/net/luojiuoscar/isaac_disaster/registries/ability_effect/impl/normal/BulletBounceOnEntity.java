package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class BulletBounceOnEntity implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof IsaacAttackAfterHitEvent event)) return false;

        if (!(event.getBulletObject() instanceof TearBullet bullet)) return false;
        if (bullet.isPiercing) return true;

        double speed = bullet.getVelocity().length();

        // 50% 概率弹向最近敌对生物
        if (Math.random() < 0.5) {
            LivingEntity target = EntityHelper.findNearestTrackingTarget(
                    bullet.level(),
                    bullet.getOwner(),
                    bullet.position(),
                    bullet.getHomingRange(),
                    e -> !bullet.getDamagedEntities().contains(e.getUUID())
            );

            if (target != null) {
                Vec3 dir = target.getEyePosition().subtract(bullet.position()).normalize();
                bullet.setVelocity(dir.scale(speed));
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
        bullet.getDamagedEntities().add(lastHit);

        bullet.setVelocity(randomDir.scale(speed));
        event.setCanceled(true);

        return true;
    }
}

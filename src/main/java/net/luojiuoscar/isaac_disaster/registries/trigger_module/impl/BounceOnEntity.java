package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.PlayerPerformAttackEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class BounceOnEntity implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(
                TriggerCategory.ON_SHOOT,
                TriggerCategory.BULLET_HIT_ENTITY_AFTER
                );
    }

    @Override
    public void onShoot(PlayerPerformAttackEvent event, int stacks, TriggerModuleQueue queue) {
        event.getContext().addTriggerModule(ModTriggerModule.BOUNCE_ON_ENTITY.getId(), 1);
    }

    @Override
    public double getPriority(){
        return 100;
    }

    @Override
    public void afterBulletHitEntity(IsaacAttackAfterHitEvent event, int stacks, TriggerModuleQueue queue){
        if (!(event.getBulletObject() instanceof TearBullet bullet)) return;
        if (bullet.isPiercing) return;

        double speed = bullet.getVelocity().length();

        // 50% 概率弹向最近敌对生物
        if (Math.random() < 0.5) {
            LivingEntity target = EntityHelper.findNearestTrackingTarget(
                    bullet.level(),
                    bullet.getOwner(),
                    bullet.position(),
                    TearBullet.HOMING_RANGE,
                    e -> !bullet.getDamagedEntities().contains(e.getUUID())
            );

            if (target != null) {
                Vec3 dir = target.getEyePosition().subtract(bullet.position()).normalize();
                bullet.setVelocity(dir.scale(speed));
                event.setCanceled(true);
                return;
            }
        }

        // 否则随机弹射
        double theta = Math.random() * 2 * Math.PI;
        double phi = Math.acos(2 * Math.random() - 1);
        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.sin(phi) * Math.sin(theta);
        double z = Math.cos(phi);
        Vec3 randomDir = new Vec3(x, y, z).normalize();

        bullet.setVelocity(randomDir.scale(speed));
        event.setCanceled(true);
    }
}

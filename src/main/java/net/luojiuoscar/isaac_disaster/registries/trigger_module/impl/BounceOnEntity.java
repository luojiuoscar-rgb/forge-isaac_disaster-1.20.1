package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class BounceOnEntity implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(
                TriggerCategory.GET_ATTACK_CONTEXT,
                TriggerCategory.BULLET_HIT_ENTITY_AFTER
                );
    }

    @Override
    public void getAttackContext(GetAttackContextEvent event, int stacks, TriggerModuleQueue queue) {
        for (AttackContext context : event.getContexts()){
            context.addTriggerModule(ModTriggerModule.BOUNCE_ON_ENTITY.getId(), 1);
        }
    }

    @Override
    public double getPriority(){
        return TriggerModulePriority.HIGHEST.priority;
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
                    bullet.getHomingRange(),
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

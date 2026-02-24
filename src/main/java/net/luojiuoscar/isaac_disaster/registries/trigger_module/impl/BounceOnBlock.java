package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.GetAttackContextEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.*;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class BounceOnBlock implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(
                TriggerCategory.GET_ATTACK_CONTEXT,
                TriggerCategory.BULLET_HIT_BLOCK
                );
    }

    @Override
    public void getAttackContext(GetAttackContextEvent event, int stacks, TriggerModuleQueue queue) {
        for (AttackContext context : event.getContexts()){
            context.addTriggerModule(ModTriggerModule.BOUNCE_ON_BLOCK.getId(), stacks);
        }
    }

    @Override
    public double getPriority(){
        return TriggerModulePriority.HIGHEST.priority;
    }

    @Override
    public void onBulletHitBlock(IsaacAttackHitBlockEvent event, int stacks, TriggerModuleQueue queue){
        BlockHitResult hit = event.getHitResult();
        Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal()).normalize();

        // ---------------- TearBullet ----------------
        if (event.getBulletObject() instanceof TearBullet bullet) {
            Vec3 motion = bullet.getVelocity();

            double speed = motion.length();
            if (speed < 1e-6) return;

            Vec3 reflected = motion.subtract(normal.scale(2 * motion.dot(normal)));
            bullet.setDeltaMovement(reflected);
            bullet.move(MoverType.SELF, reflected.scale(1));
        }
        // ---------------- LaserProjectile ----------------
        else if (event.getBulletObject() instanceof LaserAttack.LaserProjectile laser) {
            if (laser.isSpectral()) return;

            Vec3 direction = laser.direction;

            Vec3 reflected = direction.subtract(normal.scale(2 * direction.dot(normal)));
            laser.direction = reflected;
            laser.position = laser.position.add(reflected.scale(laser.step));
        }

        event.setCanceled(true);
    }
}

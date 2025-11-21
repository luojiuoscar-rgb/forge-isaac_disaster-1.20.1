package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.PlayerPerformAttackEvent;
import net.luojiuoscar.isaac_disaster.manager.attack.type.LaserAttack;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class BounceOnBlock implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(
                TriggerCategory.ON_SHOOT,
                TriggerCategory.BULLET_HIT_BLOCK
                );
    }

    @Override
    public void onShoot(PlayerPerformAttackEvent event, int stacks, TriggerModuleQueue queue) {
        event.getContext().addTriggerModule(ModTriggerModule.BOUNCE_ON_BLOCK.getId(), 1);
    }

    @Override
    public double getPriority(){
        return 100;
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
            bullet.setVelocity(reflected);
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

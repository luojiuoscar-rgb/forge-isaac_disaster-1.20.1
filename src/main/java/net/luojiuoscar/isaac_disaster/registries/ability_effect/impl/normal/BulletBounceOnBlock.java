package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BulletBounceOnBlock implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof IsaacAttackHitBlockEvent event)) return false;

        BlockHitResult hit = event.getHitResult();
        Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal()).normalize();
        IBulletObject b = event.getBulletObject();

        // ---------------- TearBullet ----------------
        if (b instanceof TearBullet bullet) {
            Vec3 motion = bullet.getVelocity();

            double speed = motion.length();
            if (speed < 1e-6) return true;

            Vec3 reflected = motion.subtract(normal.scale(2 * motion.dot(normal)));
            bullet.setVelocity(reflected);
            bullet.setDeltaMovement(reflected);

            // 清空被伤害过的实体
            bullet.getDamagedEntities().clear();
        }
        // ---------------- LaserProjectile ----------------
        else if (b instanceof LaserAttack.LaserProjectile laser) {
            if (laser.isSpectral()) return true;

            Vec3 direction = laser.direction;

            Vec3 reflected = direction.subtract(normal.scale(2 * direction.dot(normal)));
            laser.direction = reflected;
            laser.position = laser.position.add(reflected.scale(laser.step));
        }


        event.setCanceled(true);
        return true;
    }
}

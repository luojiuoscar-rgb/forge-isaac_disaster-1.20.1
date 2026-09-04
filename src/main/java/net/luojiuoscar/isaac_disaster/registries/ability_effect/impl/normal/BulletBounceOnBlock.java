package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BulletBounceOnBlock implements IAbilityEffect {
    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.BULLET_HIT_BLOCK;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof IsaacAttackHitBlockEvent event)) return false;

        BlockHitResult hit = event.getHitResult();
        Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal()).normalize();
        IBulletObject b = event.getBulletObject();

        if (b == null) return false;
        if (b instanceof LaserAttack.LaserProjectile laser && laser.isSpectral()) return true;

        Vec3 motion = b.getVelocity();

        double speed = motion.length();
        if (speed < 1e-6) return true;

        Vec3 reflected = motion.subtract(normal.scale(2 * motion.dot(normal)));
        // BulletManager resolves the optimized state to a non-overlapping center
        // before dispatching the event. Legacy entities still need this nudge here.
        b.pushOutOfBlock(normal);
        b.setVelocity(reflected);

        // 清空被伤害过的实体
        b.getDamagedEntities().clear();

        event.setCanceled(true);
        return true;
    }
}

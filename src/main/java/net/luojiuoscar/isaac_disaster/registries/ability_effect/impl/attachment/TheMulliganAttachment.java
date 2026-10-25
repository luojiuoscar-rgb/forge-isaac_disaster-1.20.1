package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.server.level.ServerPlayer;

public final class TheMulliganAttachment extends AttackPreparationEffect {
    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        if (!(context.getEntity() instanceof ServerPlayer player) || attackContext.getOwner() != player
                || attackContext.getShooter() != player) return;
        if (!ModAttackTypes.BULLET.getId().equals(attackContext.getRootTypeId())
                && !attackContext.isLaserAttack()) return;
        if (!attackContext.getTrigger().contains(ModExecutableEffects.THE_MULLIGAN.get())) {
            attackContext.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_AFTER,
                    ModExecutableEffects.THE_MULLIGAN));
        }
    }
}

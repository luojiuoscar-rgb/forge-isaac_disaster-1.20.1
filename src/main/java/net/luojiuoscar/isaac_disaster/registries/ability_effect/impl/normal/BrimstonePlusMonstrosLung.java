package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackPlanEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackOrigin;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.ViewPlaneRandomAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.server.level.ServerPlayer;

public final class BrimstonePlusMonstrosLung implements IAbilityEffect {
    private static final ViewPlaneRandomAttackPattern PATTERN = new ViewPlaneRandomAttackPattern();

    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.ATTACK_PLAN;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (!(context.get(ContextKeys.EVENT) instanceof AttackPlanEvent event)) return false;
        if (event.getOrigin() != AttackOrigin.PLAYER_PRIMARY
                || !ModAttackTypes.BRIMSTONE.getId().equals(event.getAttackType().getId())) return true;

        AttackContext reference = event.getAttackType().createAttackContext(player, player);
        if (reference == null) return false;
        int count = 3 + player.getRandom().nextInt(3);
        reference.useFixedLaunchTransform();
        event.appendExtraContexts(PATTERN.generate(new AttackPatternContext(reference, count)));
        return true;
    }
}

package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectPriority;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.luojiuoscar.isaac_disaster.system.heart.EternalHeartState;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class EternalHeartPunish implements IAbilityEffect {
    @Override
    public double getPriority() {
        return ExecutableEffectPriority.HIGHEST.priority;
    }

    @Override
    public TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.ON_HURT_NEGATIVE;
    }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof LivingHurtEvent event)) return false;
        if (event.isCanceled() || event.getAmount() <= 0) return true;

        if (!(event.getEntity() instanceof Player player)) return true;

        if (!EternalHeartState.consumeForDamage(player)) return true;
        event.setAmount(0.0f);

        return true;
    }
}

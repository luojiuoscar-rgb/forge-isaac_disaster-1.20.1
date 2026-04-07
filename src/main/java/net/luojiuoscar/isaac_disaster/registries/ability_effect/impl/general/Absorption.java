package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.player.Player;

public class Absorption implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (context.getEntity() instanceof Player player){
            StatManager.gainAbsorption(player, context.getOrDefault(ContextKeys.AMPLIFIER, 1.).floatValue());
        }
        return true;
    }
}

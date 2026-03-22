package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.player.Player;

public class Heal implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        if (context.getEntity() instanceof Player player){
            StatManager.healHealth(player, context.getOrDefault(ContextKeys.AMPLIFIER, 1));
        }
    }
}
package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.player.Player;

public class SwallowTrinkets implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        if (context.getEntity() instanceof Player player){
            PlayerHelper.swallowAllTrinkets(player);
        }
    }
}
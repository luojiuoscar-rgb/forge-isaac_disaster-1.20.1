package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class GiveItem implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        if (context.getEntity() instanceof Player player){
            Item item = context.get(ContextKeys.ITEM);
            int count = context.get(ContextKeys.AMPLIFIER);
            if (item == null || count < 1) return;

            PlayerHelper.giveItem(player, item, count);
        }
    }
}
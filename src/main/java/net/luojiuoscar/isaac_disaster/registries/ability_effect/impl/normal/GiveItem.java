package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class GiveItem implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (context.getEntity() instanceof Player player){
            Item item = context.get(ContextKeys.ITEM);
            int count = context.get(ContextKeys.AMPLIFIER);
            if (item == null) return false;
            if (count < 1) count = 1;

            PlayerHelper.giveItem(player, item, count);
        }
        return true;
    }
}
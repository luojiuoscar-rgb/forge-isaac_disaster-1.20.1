package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public class DoubleItem implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        Item item = context.get(ContextKeys.ITEM);
        if (item == null) return false;

        int value = PlayerHelper.countInvItem(player, item);

        if (value == 0){
            PlayerHelper.giveItem(player, item, 2);
        }else{
            PlayerHelper.giveItem(player, item, value);
        }
        return true;
    }
}

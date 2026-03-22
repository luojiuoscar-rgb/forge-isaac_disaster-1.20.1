package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModActiveItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class CrookedPenny implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) return;

        RandomSource random = player.getRandom();

        if (random.nextDouble() < 0.5){
            ModAbilityEffects.DIPLOPIA.get().apply(context);
        }else{
            // 清空背包并给予1块钱，返还弯币
            Inventory inv = player.getInventory();
            inv.items.clear();
            inv.offhand.clear();
            inv.armor.clear();
            PlayerHelper.giveItem(player, ModActiveItems.CROOKED_PENNY.get(), 1);
            PlayerHelper.giveMoney(player, 1);
        }
    }
}
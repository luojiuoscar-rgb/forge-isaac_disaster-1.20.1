package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Diplopia implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) return;
        ItemStack stack = context.getOrDefault(ContextKeys.ITEM_STACK, ItemStack.EMPTY);

        // 遍历背包并生成掉落，除了自身
        Inventory inv = player.getInventory();
        List<ItemStack> invItems = new ArrayList<>();
        invItems.addAll(inv.items);
        invItems.addAll(inv.offhand);

        if (stack != ItemStack.EMPTY){
            invItems.remove(stack);
        }

        for (ItemStack s : invItems) {
            if (!s.isEmpty()) {
                // 跳过不可被复制的物品
                if (s.is(TagManager.ITEM_CANNOT_BE_DUPLICATED)) continue;

                ItemStack copy = s.copy();
                PlayerHelper.giveItem(player, copy);
            }
        }
    }
}
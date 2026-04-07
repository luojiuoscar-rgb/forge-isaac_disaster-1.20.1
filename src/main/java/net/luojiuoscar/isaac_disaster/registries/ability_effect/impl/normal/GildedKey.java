package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.block.IsaacChestBlockItem;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GildedKey implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {

        if (!(context.getEntity() instanceof Player player)) return false;
        // 左断手大于等于自身时不触发
        RecursiveModuleQueue queue = context.get(ContextKeys.RECURSIVE_MODULE_QUEUE);
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
        if (queue != null && queue.get(ModRecursiveModule.GILDED_KEY.getId()).stacks >= amplifier) return true;
        // 如果queue不存在，则绕过判定直接尝试触发

        List<ItemStack> items = new ArrayList<>();
        Inventory inv = player.getInventory();
        items.addAll(inv.items);
        items.addAll(inv.offhand);

        for (int i = 0; i < items.size(); i++){
            ItemStack oldStack = items.get(i);
            if (oldStack.getItem() instanceof IsaacChestBlockItem item && !(item == ModItems.LOCKED_CHEST_ITEM.get())){
                ItemStack newStack = new ItemStack(ModItems.LOCKED_CHEST_ITEM.get());
                newStack.setCount(oldStack.getCount());
                newStack.setTag(oldStack.getTag());

                player.getInventory().setItem(i, newStack);
            }
        }

        player.getInventory().setChanged();

        return true;
    }
}

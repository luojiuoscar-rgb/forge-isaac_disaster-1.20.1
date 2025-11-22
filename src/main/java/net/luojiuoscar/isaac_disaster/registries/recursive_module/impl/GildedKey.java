package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.block.IsaacChestBlockItem;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GildedKey implements IRecursiveModule {
    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 20;
    }

    @Override
    public void recursiveEffect(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        if (!(entity instanceof Player player)) return;
        // 左断手大于等于自身时不触发
        if (queue.get(ModRecursiveModule.GILDED_KEY.getId()).stacks >= stacks) return;

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
    }
}

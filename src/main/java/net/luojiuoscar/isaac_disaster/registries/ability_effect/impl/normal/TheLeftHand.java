package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.block.IsaacChestBlockItem;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TheLeftHand implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof Player player)) return false;
        // 镀金钥匙大于自身时不触发
        RecursiveModuleQueue queue = context.get(ContextKeys.RECURSIVE_MODULE_QUEUE);
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();

        if (queue != null && queue.get(ModRecursiveModule.GILDED_KEY.getId()).stacks > amplifier) return true;

        List<ItemStack> items = new ArrayList<>();
        Inventory inv = player.getInventory();
        items.addAll(inv.items);
        items.addAll(inv.offhand);

        for (int i = 0; i < items.size(); i++){
            ItemStack oldStack = items.get(i);
            if (oldStack.getItem() instanceof IsaacChestBlockItem item && !(item == ModItems.RED_CHEST_ITEM.get())){
                ItemStack newStack = new ItemStack(ModItems.RED_CHEST_ITEM.get());
                newStack.setCount(oldStack.getCount());
                newStack.setTag(oldStack.getTag());

                player.getInventory().setItem(i, newStack);
            }
        }

        player.getInventory().setChanged();

        return false;
    }
}

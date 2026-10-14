package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.manager.CurioSlotManager;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public interface IIsaacCuriosItem extends ICurioItem {

    @Override
    default void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        CurioSlotManager.onEquip(slotContext, prevStack, stack);
    }

    @Override
    default void onUnequip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        CurioSlotManager.onUnequip(slotContext, prevStack, stack);
    }

    void tryUnequip(SlotContext slotContext, ItemStack prevStack, ItemStack stack);
    void tryEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack);


}

package net.luojiuoscar.isaac_disaster.item.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public interface IIsaacCuriosItem extends ICurioItem {
    ResourceLocation SCHEDULE_TYPE = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "adjust_curios_slot");

    String ON_CURIOS = "on_curios";

    static boolean onCurios(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(ON_CURIOS);
    }

    static void setOnCurios(ItemStack stack, boolean b) {
        stack.getOrCreateTag().putBoolean(ON_CURIOS, b);
    }

    @Override
    default void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (onCurios(stack)) return; // 防止因为playerClone等方法导致其被重复调用
        setOnCurios(stack, true);

        tryEquip(slotContext, prevStack, stack);
    }

    @Override
    default void onUnequip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        tryUnequip(slotContext, prevStack, stack);
    }



    void tryUnequip(SlotContext slotContext, ItemStack prevStack, ItemStack stack);
    void tryEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack);


}

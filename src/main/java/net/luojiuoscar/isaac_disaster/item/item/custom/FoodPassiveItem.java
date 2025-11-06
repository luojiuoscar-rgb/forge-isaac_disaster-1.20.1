package net.luojiuoscar.isaac_disaster.item.item.custom;

import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.minecraft.world.item.ItemStack;

public class FoodPassiveItem extends PassiveItem {
    private static final String BINGE_EATER = "BingeEater";

    public FoodPassiveItem(Properties properties, int itemLevel, int itemId, boolean hasSpecialEffect, boolean useOriginalColor) {
        super(properties, itemLevel, itemId, hasSpecialEffect, useOriginalColor);
    }

    public static void setBingeEater(ItemStack stack, boolean has_binge_eater) {
        stack.getOrCreateTag().putBoolean(BINGE_EATER, has_binge_eater);
    }
    public static boolean hasBingeEater(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(BINGE_EATER);
    }
}

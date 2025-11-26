package net.luojiuoscar.isaac_disaster.item.item.custom;

import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

public class FoodPassiveItem extends PassiveItem {
    private static final String BINGE_EATER = "BingeEater";

    public FoodPassiveItem(Properties properties, RegistryObject<PassiveAbility> abilityRegistryObject) {
        super(properties, abilityRegistryObject);
    }

    public static void setBingeEater(ItemStack stack, boolean has_binge_eater) {
        stack.getOrCreateTag().putBoolean(BINGE_EATER, has_binge_eater);
    }
    public static boolean hasBingeEater(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(BINGE_EATER);
    }
}

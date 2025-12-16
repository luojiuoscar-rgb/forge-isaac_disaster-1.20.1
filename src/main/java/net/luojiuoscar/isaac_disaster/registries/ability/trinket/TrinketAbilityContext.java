package net.luojiuoscar.isaac_disaster.registries.ability.trinket;

import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class TrinketAbilityContext {
    public final ItemStack stack;
    public final boolean isEnchanted;

    public TrinketAbilityContext(ItemStack stack){
        this.stack = stack;
        this.isEnchanted = Trinket.isEnchanted(stack);
    }

    public TrinketAbilityContext(@Nullable ItemStack stack, boolean isEnchanted) {
        this.stack = stack;
        this.isEnchanted = isEnchanted;
    }
}

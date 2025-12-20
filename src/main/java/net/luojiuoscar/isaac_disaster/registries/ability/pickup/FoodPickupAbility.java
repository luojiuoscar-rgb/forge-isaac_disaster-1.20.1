package net.luojiuoscar.isaac_disaster.registries.ability.pickup;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class FoodPickupAbility extends PickupAbility {
    @Override
    public void shrinkAfterUse(@NotNull ItemStack stack){
        // will not shrink after use
    }
}

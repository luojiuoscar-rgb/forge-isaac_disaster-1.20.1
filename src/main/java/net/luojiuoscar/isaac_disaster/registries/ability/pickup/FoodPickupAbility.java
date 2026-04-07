package net.luojiuoscar.isaac_disaster.registries.ability.pickup;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class FoodPickupAbility extends PickupAbility {
    protected FoodPickupAbility(CompositeTrigger triggers) {
        super(triggers);
    }

    @Override
    public void shrinkAfterUse(@NotNull ItemStack stack){
        // will not shrink after use
    }
}

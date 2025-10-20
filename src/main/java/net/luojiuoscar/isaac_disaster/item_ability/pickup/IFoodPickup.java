package net.luojiuoscar.isaac_disaster.item_ability.pickup;

import net.minecraft.world.item.ItemStack;

public interface IFoodPickup extends IPickup{
    @Override
    default void shrinkAfterUse(ItemStack stack){
        // 食物类不会删除
    }
}

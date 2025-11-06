package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.item.custom.FoodPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IFoodPassiveItem {
    default void onFoodObtain(Player player, @Nullable ItemStack stack){
        if (PlayerHelper.hasItem(ItemId.BINGE_EATER.getId(), (ServerPlayer) player) && stack != null){
            FoodPassiveItem.setBingeEater(stack, true);
            onFoodObtainEffect(player, stack);
        }
    }

    default void onFoodRemove(Player player, @Nullable ItemStack stack){
        if (stack != null && FoodPassiveItem.hasBingeEater(stack)){
            FoodPassiveItem.setBingeEater(stack, false);
            onFoodRemoveEffect(player, stack);
        }
    }


    void onFoodObtainEffect(Player player, @Nullable ItemStack stack);
    void onFoodRemoveEffect(Player player, @Nullable ItemStack stack);
}

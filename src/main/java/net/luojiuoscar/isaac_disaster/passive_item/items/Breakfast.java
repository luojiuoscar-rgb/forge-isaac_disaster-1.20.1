package net.luojiuoscar.isaac_disaster.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.StatController;
import net.luojiuoscar.isaac_disaster.passive_item.PassiveItem;
import net.luojiuoscar.isaac_disaster.manager.ItemIdManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Breakfast implements PassiveItem {

    @Override
    public int getItemId() {
        return ItemIdManager.BREAKFAST;
    }

    @Override
    public void obtainEffect(Player player) {
    }

    @Override
    public void directObtainEffect(Player player) {
        StatController.modifyMaxHealth(player, 1, 1);
    }

    @Override
    public void removeEffect(Player player) {
        StatController.modifyMaxHealth(player, -1, 0);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.BREAKFAST.get());
    }
}

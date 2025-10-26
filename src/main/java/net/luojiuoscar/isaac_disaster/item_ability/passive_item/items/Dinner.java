package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Dinner implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.DINNER.getId();
    }

    @Override
    public void onFirstObtain(Player player) {
        StatManager.healHealth(player, 1.0f);
    }

    @Override
    public void onObtain(Player player) {
        StatManager.MAX_HEALTH.apply(player, 1);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.MAX_HEALTH.apply(player, -1);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.DINNER.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.MAX_HEALTH.description(1)
        );
    }
}

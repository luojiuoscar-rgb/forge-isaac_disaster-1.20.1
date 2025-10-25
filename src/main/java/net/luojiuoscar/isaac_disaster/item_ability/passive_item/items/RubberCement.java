package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RubberCement implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.RUBBER_CEMENT.getId();
    }

    @Override
    public void onFirstObtain(Player player, boolean isPermanent) {
    }

    @Override
    public void onObtain(Player player, boolean isPermanent) {
    }

    @Override
    public void onRemove(Player player, boolean isPermanent) {
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.RUBBER_CEMENT.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.rubber_cement.lore.1")
        );
    }
}

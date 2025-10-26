package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TornPhoto implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.TORN_PHOTO.getId();
    }

    @Override
    public void onFirstObtain(Player player) {
    }

    @Override
    public void onObtain(Player player) {
        StatManager.TEARS.apply(player, 1);
        StatManager.BLOCK_BREAKING.apply(player, 1);
        StatManager.BULLET_SPEED.apply(player, 0.8);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.TEARS.apply(player, -1);
        StatManager.BLOCK_BREAKING.apply(player, -1);
        StatManager.BULLET_SPEED.apply(player, -0.8);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.TORN_PHOTO.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.TEARS.description(1),
                StatManager.BLOCK_BREAKING.description(1),
                StatManager.BULLET_SPEED.description(1)


        );
    }
}

package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CupidsArrow implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.CUPIDS_ARROW.getId();
    }

    @Override
    public void onFirstObtain(Player player) {

    }

    @Override
    public void onObtain(Player player) {
        StatManager.modifyPiercing(player, 1);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifyPiercing(player, -1);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.CUPIDS_ARROW.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.piercing_bullet")
        );
    }
}

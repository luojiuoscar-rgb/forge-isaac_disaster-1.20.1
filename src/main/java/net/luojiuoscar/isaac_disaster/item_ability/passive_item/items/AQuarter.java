package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AQuarter implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.A_QUARTER.getId();
    }

    @Override
    public void onFirstObtain(Player player, boolean isPermanent) {
        PlayerHelper.giveMoney(player, 25);
    }

    @Override
    public void onObtain(Player player, boolean isPermanent) {
    }

    @Override
    public void onRemove(Player player, boolean isPermanent) {
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.A_QUARTER.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.action.give_money", 25)
        );
    }
}

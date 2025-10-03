package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BomberBoy implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.BOMBER_BOY.getId();
    }

    @Override
    public void onObtain(Player player) {
        PlayerHelper.giveItem(player, ModItems.BOMB.get(), 5);
        PlayerHelper.giveItem(player, ModItems.GIGA_BOMB.get(), 1);
    }

    @Override
    public void onDirectObtain(Player player) {
    }

    @Override
    public void onRemove(Player player) {
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.BOMBER_BOY.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.action.give_bomb", 5),
                Component.translatable("item.isaac_disaster.action.give_giga_bomb", 1),
                Component.translatable("item.isaac_disaster.mr_mega.lore.1")
        );
    }
}

package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HotBomb implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.HOT_BOMB.getId();
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {
        PlayerHelper.giveItem(player, ModItems.BOMB.get(), 5);
    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.HOT_BOMB.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.action.give_bomb", 5),
                Component.translatable("item.isaac_disaster.hot_bomb.lore.1")
        );
    }
}

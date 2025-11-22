package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModule;
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
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
        PlayerHelper.giveItem(player, ModItems.BOMB.get(), 5);
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.addRecursiveModule(player, ModRecursiveModule.FIRE_RESISTANCE.getId(), 1);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.addRecursiveModule(player, ModRecursiveModule.FIRE_RESISTANCE.getId(), -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.action.give_bomb", 5),
                Component.translatable("item.isaac_disaster.hot_bomb.lore.1"),
                Component.translatable("item.isaac_disaster.hot_bomb.lore.2")
                );
    }
}

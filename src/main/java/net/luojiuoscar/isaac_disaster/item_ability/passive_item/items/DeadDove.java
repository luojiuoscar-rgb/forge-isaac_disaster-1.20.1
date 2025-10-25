package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DeadDove implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.DEAD_DOVE.getId();
    }

    @Override
    public void onFirstObtain(Player player, boolean isPermanent) {

    }

    @Override
    public void onObtain(Player player, boolean isPermanent) {
        StatManager.modifyFlyTimeAdder(player, 1, isPermanent);
        StatManager.modifySpectral(player, 1, isPermanent);
    }

    @Override
    public void onRemove(Player player, boolean isPermanent) {
        StatManager.modifyFlyTimeAdder(player, -1, isPermanent);
        StatManager.modifySpectral(player, -1, isPermanent);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.DEAD_DOVE.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.fly"),
                Component.translatable("item.isaac_disaster.attribute.spectral_bullet")
        );
    }
}

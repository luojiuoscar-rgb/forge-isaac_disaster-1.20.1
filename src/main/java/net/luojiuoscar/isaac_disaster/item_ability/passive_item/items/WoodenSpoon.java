package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WoodenSpoon implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.WOODEN_SPOON.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.MOVEMENT_SPEED.apply(player, 1.5);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.MOVEMENT_SPEED.apply(player, -1.5);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.MOVEMENT_SPEED.description(1.5)
        );
    }
}

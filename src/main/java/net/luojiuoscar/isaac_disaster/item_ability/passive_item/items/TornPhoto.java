package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TornPhoto implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.TORN_PHOTO.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.TEARS.apply(player, 1);
        StatManager.BLOCK_BREAKING.apply(player, 1);
        StatManager.BULLET_SPEED.apply(player, 0.8);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.TEARS.apply(player, -1);
        StatManager.BLOCK_BREAKING.apply(player, -1);
        StatManager.BULLET_SPEED.apply(player, -0.8);
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

package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Marked implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.MARKED.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.addControllable(player, 1);
        StatManager.RANGE.apply(player, 1);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.addControllable(player, -1);
        StatManager.RANGE.apply(player, -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.attribute.controllable_bullet"),
                StatManager.RANGE.description(1)
        );
    }
}

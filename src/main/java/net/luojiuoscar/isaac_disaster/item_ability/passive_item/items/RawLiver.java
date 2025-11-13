package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RawLiver implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.RAW_LIVER.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
        player.setHealth(player.getMaxHealth());
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, 2);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, -2);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.MAX_HEALTH.description(2),
                Component.translatable("item.isaac_disaster.action.full_health")
        );
    }
}

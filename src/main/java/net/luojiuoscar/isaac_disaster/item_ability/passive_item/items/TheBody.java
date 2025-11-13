package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TheBody implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.THE_BODY.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
        StatManager.healHealth(player, 3);
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, 3);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, -3);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.MAX_HEALTH.description(3),
                Component.translatable("item.isaac_disaster.action.heal_health", 3*StatManager.MAX_HEALTH.getBonus())
        );
    }
}

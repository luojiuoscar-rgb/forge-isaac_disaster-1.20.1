package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TheWafer implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.THE_WAFER.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {}
    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModule.THE_WAFER.getId(), 1);
    }
    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModule.THE_WAFER.getId(), -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.the_wafer.lore.1")
        );
    }
}

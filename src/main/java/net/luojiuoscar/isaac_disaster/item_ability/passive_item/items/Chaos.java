package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Chaos implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.CHAOS.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
        LootHelper.spawnLootAtPos(player, player.position(), LootTableManager.RANDOM_PICKUPS,
                player.getRandom().nextInt(1, 7));
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.chaos.lore.1"),
                Component.translatable("item.isaac_disaster.chaos.lore.2")
        );
    }
}

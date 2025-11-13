package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Phd implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.PHD.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {
        PlayerHelper.giveItem(player, ModItems.RED_HEART.get(), 2);
        LootHelper.spawnLootAtPos(player, player.blockPosition().getCenter(), LootTableManager.RANDOM_PILLS);
    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        StatManager.PILL_QUALITY.apply(player, 1);
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
        StatManager.PILL_QUALITY.apply(player, -1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.phd.lore.1"),
                Component.translatable("item.isaac_disaster.action.give_pill", 1),
                Component.translatable("item.isaac_disaster.action.give_red_heart", 2)

        );
    }
}

package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CaffeinePill implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.CAFFEINE_PILL.getId();
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.CAFFEINE_PILL.get());
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {
        if (player.level() instanceof ServerLevel level){
            LootHelper.spawnLootAtPos(player, player.blockPosition().getCenter(), LootTableManager.RANDOM_PILLS);
        }
    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
        StatManager.MOVEMENT_SPEED.apply(player, 1.5);
        StatManager.SCALE.apply(player, -1);
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
        StatManager.MOVEMENT_SPEED.apply(player, -1.5);
        StatManager.SCALE.apply(player, 1);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.MOVEMENT_SPEED.description(1.5),
                Component.translatable("attribute.isaac_disaster.scale_down"),
                Component.translatable("item.isaac_disaster.action.give_pill", 1)
        );
    }
}

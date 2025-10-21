package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.TextHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.LootTableNameManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CaffeinePill implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.CAFFEINE_PILL.getId();
    }

    @Override
    public void onObtain(Player player) {
        if (player.level() instanceof ServerLevel level){
            LevelHelper.spawnLootAtPos(level, player.blockPosition().getCenter(),
                    ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, LootTableNameManager.RANDOM_PILLS));
        }
    }

    @Override
    public void onDirectObtain(Player player) {
        StatManager.modifyMovementSpeedAdder(player, 1.5);
        StatManager.modifyScaleAdder(player, -1);
    }

    @Override
    public void onRemove(Player player) {
        StatManager.modifyMovementSpeedAdder(player, -1.5);
        StatManager.modifyScaleAdder(player, 1);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.CAFFEINE_PILL.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                TextHelper.formatAttribute("item.isaac_disaster.attribute.movement_speed", 1500*StatManager.getMovementSpeedBonus()),
                Component.translatable("item.isaac_disaster.attribute.scale_down"),
                Component.translatable("item.isaac_disaster.action.give_pill", 1)


        );
    }
}

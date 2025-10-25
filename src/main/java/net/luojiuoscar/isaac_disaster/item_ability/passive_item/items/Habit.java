package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IHurtTriggerPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Habit implements IHurtTriggerPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.HABIT.getId();
    }

    @Override
    public void onFirstObtain(Player player, boolean isPermanent) {
    }

    @Override
    public void onObtain(Player player, boolean isPermanent) {
    }

    @Override
    public void onRemove(Player player, boolean isPermanent) {
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.HABIT.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.habit.lore.1")
        );
    }

    @Override
    public void handleHurtEffect(Player player, Entity target) {
        if (player.level().isClientSide) return;
        PlayerHelper.chargeAll((ServerPlayer) player, 100);
    }

    @Override
    public boolean isPunishType() {
        return false;
    }

    @Override
    public double getTriggerChance(Player player) {
        return 1;
    }
}

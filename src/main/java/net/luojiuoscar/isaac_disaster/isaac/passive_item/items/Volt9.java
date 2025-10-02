package net.luojiuoscar.isaac_disaster.isaac.passive_item.items;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.isaac.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Volt9 implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.VOLT_9.getId();
    }

    @Override
    public void onObtain(Player player) {
        // 存储玩家的蓄电池数量
        AtomicInteger theBatteryCount = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> theBatteryCount.set(playerPassiveItem.getItemCount(ItemId.THE_BATTERY.getId())));

        // 遍历玩家所有物品槽位
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            // 检查物品是否为NormalActiveItem类型且不为空
            if (!stack.isEmpty() && stack.getItem() instanceof ActiveItem) {
                // 满电
                ActiveItem.fullCharge(stack, theBatteryCount.get() > 0);
            }
        }
    }

    @Override
    public void onDirectObtain(Player player) {
    }

    @Override
    public void onRemove(Player player) {
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.VOLT_9.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.volt_9.lore.1"),
                Component.translatable("item.isaac_disaster.action.full_charge")
        );
    }
}

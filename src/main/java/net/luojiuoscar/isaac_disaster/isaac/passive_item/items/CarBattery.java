package net.luojiuoscar.isaac_disaster.isaac.passive_item.items;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.isaac.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PassiveItemSyncS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CarBattery implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.CAR_BATTERY.getId();
    }

    @Override
    public void onObtain(Player player) {
    }

    @Override
    public void onDirectObtain(Player player) {
        // 车载电池需要同步数据到客户端
        AtomicInteger count = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> count.set(playerPassiveItem.getItemCount(ItemId.CAR_BATTERY.getId()))
        );
        ModMessages.sentToPlayer(new PassiveItemSyncS2CPacket(ItemId.CAR_BATTERY.getId(), count.get()), (ServerPlayer) player);
    }

    @Override
    public void onRemove(Player player) {
        // 车载电池需要同步数据到客户端
        AtomicInteger count = new AtomicInteger();
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> count.set(playerPassiveItem.getItemCount(ItemId.CAR_BATTERY.getId()))
        );
        ModMessages.sentToPlayer(new PassiveItemSyncS2CPacket(ItemId.CAR_BATTERY.getId(), count.get()), (ServerPlayer) player);
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.CAR_BATTERY.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.car_battery.lore.1"),
                Component.translatable("item.isaac_disaster.special.cannot_stack")
        );
    }
}

package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PassiveItemSyncS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.logging.Level;

public class Phd implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.PHD.getId();
    }

    @Override
    public void onObtain(Player player) {
        PlayerHelper.giveItem(player, ModItems.RED_HEART.get(), 2);
        LevelHelper.spawnLootAtPos((ServerLevel) player.level(), player.blockPosition().getCenter(),
                ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "random_pills"));
    }

    @Override
    public void onDirectObtain(Player player) {
        if (player instanceof ServerPlayer serverPlayer){
            int count = PlayerHelper.getItemCount(ItemId.PHD.getId(), serverPlayer);
            ModMessages.sentToPlayer(new PassiveItemSyncS2CPacket(ItemId.PHD.getId(), count), serverPlayer);
        }
        PlayerHelper.modifyPillQuality(player, 1);
    }

    @Override
    public void onRemove(Player player) {
        if (player instanceof ServerPlayer serverPlayer){
            int count = PlayerHelper.getItemCount(ItemId.PHD.getId(), serverPlayer);
            ModMessages.sentToPlayer(new PassiveItemSyncS2CPacket(ItemId.PHD.getId(), count), serverPlayer);
        }
        PlayerHelper.modifyPillQuality(player, -1);
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.PHD.get());
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

package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PassiveItemSyncS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TarotCloth implements IPassiveItem {

    @Override
    public int getItemId() {
        return ItemId.TAROT_CLOTH.getId();
    }

    @Override
    public void onFirstObtain(Player player, boolean isPermanent) {
    }

    @Override
    public void onObtain(Player player, boolean isPermanent) {
        if (player instanceof ServerPlayer serverPlayer){
            int count = PlayerHelper.getItemCount(ItemId.TAROT_CLOTH.getId(), serverPlayer);
            ModMessages.sentToPlayer(new PassiveItemSyncS2CPacket(ItemId.TAROT_CLOTH.getId(), count), serverPlayer);
        }
    }

    @Override
    public void onRemove(Player player, boolean isPermanent) {
        if (player instanceof ServerPlayer serverPlayer){
            int count = PlayerHelper.getItemCount(ItemId.TAROT_CLOTH.getId(), serverPlayer);
            ModMessages.sentToPlayer(new PassiveItemSyncS2CPacket(ItemId.TAROT_CLOTH.getId(), count), serverPlayer);
        }
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.TAROT_CLOTH.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.tarot_cloth.lore.1")
        );
    }
}

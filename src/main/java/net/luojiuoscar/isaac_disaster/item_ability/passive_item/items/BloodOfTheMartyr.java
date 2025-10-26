package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PassiveItemSyncS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BloodOfTheMartyr implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.BLOOD_OF_THE_MARTYR.getId();
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 1);

        // 数据需要同步数据到客户端
        if (player instanceof ServerPlayer serverPlayer){
            int count = PlayerHelper.getItemCount(ItemId.BLOOD_OF_THE_MARTYR.getId(), serverPlayer);
            ModMessages.sentToPlayer(new PassiveItemSyncS2CPacket(ItemId.BLOOD_OF_THE_MARTYR.getId(), count), serverPlayer);
        }
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, -1);

        // 数据需要同步数据到客户端
        if (player instanceof ServerPlayer serverPlayer){
            int count = PlayerHelper.getItemCount(ItemId.BLOOD_OF_THE_MARTYR.getId(), serverPlayer);
            ModMessages.sentToPlayer(new PassiveItemSyncS2CPacket(ItemId.BLOOD_OF_THE_MARTYR.getId(), count), serverPlayer);
        }
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(ModItems.BLOOD_OF_THE_MARTYR.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                StatManager.DAMAGE.description(1)
        );
    }
}

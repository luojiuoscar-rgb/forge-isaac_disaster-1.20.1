package net.luojiuoscar.isaac_disaster.item_ability.pickup;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PickupOnUseS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ITarot extends IPickup {
    @Override
    default void onUse(Player player, ItemStack stack, InteractionHand hand){

        if (player.level().isClientSide) return;

        if (PlayerHelper.hasItem(ItemId.TAROT_CLOTH.getId(), (ServerPlayer) player)){
            onUseEffectStronger(player, stack, hand);
        }else{
            onUseEffect(player, stack, hand);
        }

        // 客户端效果
        ModMessages.sentToPlayer(new PickupOnUseS2CPacket(getItemId()), (ServerPlayer) player);

        if (!player.isCreative()){
            shrinkAfterUse(stack);
        }
    }

    void onUseEffectStronger(Player player, ItemStack stack, InteractionHand hand);
}

package net.luojiuoscar.isaac_disaster.item_ability.pickup;

import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PickupOnUseS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public interface IPickup {
    int getItemId();

    default void onUse(Player player, ItemStack stack, InteractionHand hand){
        if (player.level().isClientSide) return;
        onUseEffect(player, stack, hand);

        // 客户端效果
        ModMessages.sentToPlayer(new PickupOnUseS2CPacket(getItemId()), (ServerPlayer) player);

        if (!player.isCreative()){
            shrinkAfterUse(stack);
        }
    }

    default void shrinkAfterUse(ItemStack stack){
        stack.shrink(1);
    }

    void onUseEffect(Player player, ItemStack stack, InteractionHand hand);

    void onUseSound(Player player);

    default void onUseClient(Player player){
        onUseSound(player);
    }

    /**
     * 默认为空
     */
    default List<Component> getDescription() {
        return new ArrayList<>();
    }

}

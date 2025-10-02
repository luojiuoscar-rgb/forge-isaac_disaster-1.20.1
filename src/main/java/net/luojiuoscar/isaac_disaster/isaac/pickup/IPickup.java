package net.luojiuoscar.isaac_disaster.isaac.pickup;

import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PickupOnUseS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.mojang.text2speech.Narrator.LOGGER;

public interface IPickup {
    int getItemId();

    default void onUse(Player player, ItemStack stack, InteractionHand hand){
        onUseEffect(player, stack, hand);

        // 触发客户端效果(发包)
        if(!player.level().isClientSide()){
            ModMessages.sentToPlayer(new PickupOnUseS2CPacket(getItemId()), (ServerPlayer) player);
        }
    }

    void onUseEffect(Player player, ItemStack stack, InteractionHand hand);

    void onUseSound(Player player);

    default void onUseClient(Player player){
        onUseSound(player);
    }
}

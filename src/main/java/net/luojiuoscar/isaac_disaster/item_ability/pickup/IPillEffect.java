package net.luojiuoscar.isaac_disaster.item_ability.pickup;

import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


public interface IPillEffect {

    int getPillEffectId();

    default void onUse(ServerPlayer player){


        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), false), (ServerPlayer) player);
        }else{
            onUseEffect(player);
        }
    }

    default void onUseH(ServerPlayer player){
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }else{
            onUseEffectH(player);
        }
    }

    void onUseEffect(ServerPlayer player);

    void onUseEffectH(ServerPlayer player);

    default void onUseClient(Player player){
        onUseSound(player);
    }

    default void onUseClientH(Player player){
        onUseSoundH(player);
    }

    void onUseSound(Player player);

    void onUseSoundH(Player player);

    String getDescriptionId();







}

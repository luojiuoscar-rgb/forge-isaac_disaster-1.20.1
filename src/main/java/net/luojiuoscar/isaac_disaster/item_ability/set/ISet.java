package net.luojiuoscar.isaac_disaster.item_ability.set;

import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PickupOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.SetOnObtainS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface ISet {
    int getSetId();

    // 需要多少个道具来触发套装效果
    int getRequireCount();

    default void onObtain(Player player){
        onObtainEffect(player);
        player.sendSystemMessage(onObtainDescription(player));

        // 触发客户端效果(发包)
        if(!player.level().isClientSide()){
            ModMessages.sentToPlayer(new SetOnObtainS2CPacket(getSetId()), (ServerPlayer) player);
        }
    }

    default void onRemove(Player player){
        onRemoveEffect(player);
        player.sendSystemMessage(onRemoveDescription(player));
    }




    void onObtainEffect(Player player);

    void onRemoveEffect(Player player);

    Component onObtainDescription(Player player);

    Component onRemoveDescription(Player player);

    default void onObtainSound(Player player){
        SoundEvent sound = SoundEvents.PLAYER_LEVELUP;
        player.playSound(sound, 1.0f, 1.0f);
    }

    default void onObtainClient(Player player){
        onObtainSound(player);
    }

    List<Component> getDescription();
}

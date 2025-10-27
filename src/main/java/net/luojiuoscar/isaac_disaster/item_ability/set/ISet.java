package net.luojiuoscar.isaac_disaster.item_ability.set;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerPassiveItemProvider;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.SetOnObtainS2CPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface ISet {
    int getSetId();

    // 需要多少个道具来触发套装效果
    int getRequireCount();

    default void onObtain(Player player){
        onObtainEffect(player);
        player.getCapability(PlayerPassiveItemProvider.PLAYER_PASSIVE_ITEM).ifPresent(
                playerPassiveItem -> {
                    if(!playerPassiveItem.isObtainedSet(getSetId())){
                        onFirstObtain(player);
                        playerPassiveItem.setObtainedSet(getSetId()); // 标记为已触发
                    }
                }
        );

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


    void onFirstObtain(Player player);

    void onObtainEffect(Player player);

    void onRemoveEffect(Player player);

    Component onObtainDescription(Player player);

    Component onRemoveDescription(Player player);

    default void onObtainSound(Player player){
    }

    default void onObtainClient(Player player){
        onObtainSound(player);
    }

    List<Component> getExplain();

    List<Component> getSynergyDescription();
}

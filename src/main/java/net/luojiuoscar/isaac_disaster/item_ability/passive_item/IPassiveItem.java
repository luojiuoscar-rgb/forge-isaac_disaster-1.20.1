package net.luojiuoscar.isaac_disaster.item_ability.passive_item;

import net.luojiuoscar.isaac_disaster.item.item.IsaacItem;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PassiveItemModifyCountSyncS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public interface IPassiveItem {
    /**
     * 获取道具ID
     */
    int getItemId();

    default void onObtain(Player player, @Nullable ItemStack stack, boolean withSFX){
        if (player.level().isClientSide) return;

        // effect
        handleObtain(player, stack);
        if (stack != null && !PassiveItem.isConsumed(stack)) handleFirstObtain(player, stack);

        // sfx
        if (withSFX) player.level().playSound(null, player.getX(), player.getY(), player.getZ(), getSound(),
                    SoundSource.PLAYERS, 1.0f, 1.0f);

        ModMessages.sentToPlayer(new PassiveItemModifyCountSyncS2CPacket(getItemId(), 1), (ServerPlayer) player);
    }

    default void onRemove(Player player, @Nullable ItemStack stack){
        if (player.level().isClientSide) return;

        handleRemove(player, stack);

        ModMessages.sentToPlayer(new PassiveItemModifyCountSyncS2CPacket(getItemId(), -1), (ServerPlayer) player);
    }

    /** 不会被remove的效果 */
    void handleFirstObtain(Player player, @Nullable ItemStack stack);

    void handleObtain(Player player, @Nullable ItemStack stack);

    void handleRemove(Player player, @Nullable ItemStack stack);

    default SoundEvent getSound(){
        return ModSounds.DEFAULT_OBTAIN_ITEM.get();
    };

    /**
     * 获取lore
     */
    List<Component> getDescription();

    /**
     * 获取解释性信息
     */
    default List<Component> getExplain(){
        return new ArrayList<>();
    };

    default List<Component> getSynergyDescription(){
        return new ArrayList<>();
    }

    default int getItemLevel(){
        if (ItemId.getItemById(getItemId()).get() instanceof IsaacItem item){
            return item.getItemLevel();
        }
        return 0;
    }

}

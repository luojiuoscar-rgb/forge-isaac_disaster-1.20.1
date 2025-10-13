package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;


import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.ActiveItemManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


public class FeelsLikeImWalkingOnSunshine implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.FEELS_LIKE_IM_WALKING_ON_SUNSHINE.getId();
    }

    @Override
    public void onUse(Player player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.RETRO_VISION.getId()).onUse(player);
            return;
        }

        onUseEffect(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), false), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseH(Player player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.RETRO_VISION.getId()).onUseH(player);
            return;
        }

        onUseEffectH(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }
    }


    @Override
    public void onUseEffect(Player player) {
        ActiveItemManager.getInstance().getItemFromId(ItemId.UNICORN_STUMP.getId()).onTriggeredEffect(player);
    }

    @Override
    public void onUseEffectH(Player player) {
        ActiveItemManager.getInstance().getItemFromId(ItemId.THE_GAMEKID.getId()).onTriggeredEffectStronger(player);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.FEELS_LIKE_IM_WALKING_ON_SUNSHINE.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.FEELS_LIKE_IM_WALKING_ON_SUNSHINE_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() < 0){
            return "pill.isaac_disaster.effect.retro_vision";
        }
        return "pill.isaac_disaster.effect.feels_like_im_walking_on_sunshine";
    }

}

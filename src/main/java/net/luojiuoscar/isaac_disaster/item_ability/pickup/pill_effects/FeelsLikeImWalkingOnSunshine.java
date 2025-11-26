package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;


import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbility;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


public class FeelsLikeImWalkingOnSunshine implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.FEELS_LIKE_IM_WALKING_ON_SUNSHINE.getId();
    }

    @Override
    public void onUse(ServerPlayer player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.RETRO_VISION.getId()).onUse(player);
            return;
        }


        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), false), (ServerPlayer) player);
        }else{
            onUseEffect((ServerPlayer) player);
        }
    }

    @Override
    public void onUseH(ServerPlayer player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.RETRO_VISION.getId()).onUseH(player);
            return;
        }


        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }else {
            onUseEffectH((ServerPlayer) player);
        }
    }


    @Override
    public void onUseEffect(ServerPlayer player) {
        ModActiveAbility.THE_GAMEKID.get().onTrigger(player, null);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        ModActiveAbility.THE_GAMEKID.get().onTriggerStronger(player, null);
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

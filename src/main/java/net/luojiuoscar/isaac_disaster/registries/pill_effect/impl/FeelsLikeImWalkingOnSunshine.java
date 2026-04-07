package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;


import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;


public class FeelsLikeImWalkingOnSunshine implements IPillEffect {

    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? ModPillEffect.RETRO_VISION.get() : this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        // ModActiveAbility.THE_GAMEKID.get().getTrigger().fire(player, null, null);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        // ModActiveAbility.THE_GAMEKID.get().onTriggerStronger(player, null, null);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.FEELS_LIKE_IM_WALKING_ON_SUNSHINE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.FEELS_LIKE_IM_WALKING_ON_SUNSHINE_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality){
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.retro_vision"
                : "pill.isaac_disaster.effect.feels_like_im_walking_on_sunshine";
    }

}

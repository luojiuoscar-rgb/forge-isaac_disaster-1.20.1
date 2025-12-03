package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;


public class HealthUp implements IPillEffect {

    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? ModPillEffect.HEALTH_DOWN.get() : this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        StatManager.MAX_HEALTH.apply(player, 0.5);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        StatManager.MAX_HEALTH.apply(player, 1);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.HEALTH_UP.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD_WITH_EFFECT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.HEALTH_UP_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId(int pillQuality){
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.health_down"
                : "pill.isaac_disaster.effect.health_up";
    }

}

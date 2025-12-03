package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;


public class ImExcited implements IPillEffect {
    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? ModPillEffect.IM_DROWSY.get() : this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        int duration = 600;
        int amplifier = 0;
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, amplifier));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier));
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        int duration = 1500;
        int amplifier = 2;
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, amplifier));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier));
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.IM_EXCITED.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.IM_EXCITED_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }


    @Override
    public String getDescriptionId(int pillQuality){
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.im_drowsy"
                : "pill.isaac_disaster.effect.im_excited";
    }

}

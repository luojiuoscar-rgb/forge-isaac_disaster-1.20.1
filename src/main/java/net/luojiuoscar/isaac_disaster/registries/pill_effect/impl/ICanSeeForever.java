package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;


import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;


public class ICanSeeForever implements IPillEffect {
    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? ModPillEffect.AMNESIA.get() : this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        int duration = 600;
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration));
        player.addEffect(new MobEffectInstance(ModEffects.X_RAY_VISION.get(), duration));
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.BLINDNESS);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        int duration = 1500;
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration));
        player.addEffect(new MobEffectInstance(ModEffects.X_RAY_VISION.get(), duration));
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.BLINDNESS);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.I_CAN_SEE_FOREVER.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.I_CAN_SEE_FOREVER_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality){
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.amnesia"
                : "pill.isaac_disaster.effect.i_can_see_forever";
    }

}


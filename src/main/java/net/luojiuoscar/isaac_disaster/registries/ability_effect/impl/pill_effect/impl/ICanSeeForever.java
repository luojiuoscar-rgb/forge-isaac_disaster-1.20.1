package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;


import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;


public class ICanSeeForever extends PillEffect {
    @Override
    public PillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? (PillEffect) ModExecutableEffects.AMNESIA.get() : this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        int duration = isHorse ? 1500 : 600;
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration));
        player.addEffect(new MobEffectInstance(ModEffects.X_RAY_VISION.get(), duration));
        player.removeEffect(MobEffects.DARKNESS);
        player.removeEffect(MobEffects.BLINDNESS);
        return true;
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


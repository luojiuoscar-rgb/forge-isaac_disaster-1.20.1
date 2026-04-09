package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;


import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;


public class FeelsLikeImWalkingOnSunshine extends PillEffect {

    @Override
    public PillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? (PillEffect) ModExecutableEffects.RETRO_VISION.get() : this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        ActiveAbility ability = ModActiveAbility.THE_GAMEKID.get();

        context.set(ContextKeys.AMPLIFIER, isHorse ? ability.getStrongerAmplifier() : ability.getNormalAmplifier());
        ability.getTrigger().fire(context, null);
        return true;
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

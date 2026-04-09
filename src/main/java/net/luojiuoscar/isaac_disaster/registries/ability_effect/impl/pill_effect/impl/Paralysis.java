package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.interfaces.GiveBlackHeartIfLowQuality;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;


public class Paralysis extends PillEffect implements GiveBlackHeartIfLowQuality {
    @Override
    public PillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) > 0 ? (PillEffect) ModExecutableEffects.PHEROMONES.get() : this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        int duration = isHorse ? 240 : 120;
        MobEffectInstance dizziness = new MobEffectInstance(ModEffects.DIZZINESS.get(), duration, 255);
        MobEffectInstance lacrimal_hyposecretion = new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration);
        player.addEffect(dizziness);
        player.addEffect(lacrimal_hyposecretion);
        return true;
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.PARALYSIS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_BAD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.PARALYSIS_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_BAD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality) {
        return pillQuality > 0
                ? "pill.isaac_disaster.effect.pheromones"
                : "pill.isaac_disaster.effect.paralysis";
    }

}

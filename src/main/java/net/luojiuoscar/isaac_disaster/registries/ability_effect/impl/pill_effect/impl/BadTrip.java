package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.interfaces.GiveBlackHeartIfLowQuality;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;


public class BadTrip extends PillEffect implements GiveBlackHeartIfLowQuality {

    @Override
    public PillEffect redirect(ServerPlayer player) {
        if (PlayerHelper.getPillQuality(player) > 0){
            return (PillEffect) ModExecutableEffects.BALLS_OF_STEEL.get();
        }else if(player.getHealth() <= StatManager.MAX_HEALTH.getBonus() * 2){
            return PlayerHelper.getPillQuality(player) < 0 ?
                    (PillEffect) ModExecutableEffects.I_FOUND_PILLS.get() : (PillEffect) ModExecutableEffects.FULL_HEALTH.get();
        }
        return this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        StatManager.healHealth(player, isHorse ? -2f : -1f);
        return true;
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.BAD_TRIP.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_BAD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.BAD_TRIP_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_BAD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId(int pillQuality){
        return pillQuality > 0
                ? "pill.isaac_disaster.effect.balls_of_steel"
                : "pill.isaac_disaster.effect.bad_trip";
    }
}

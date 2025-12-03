package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;


public class BadTrip implements IPillEffect {

    @Override
    public IPillEffect redirect(ServerPlayer player) {
        if (PlayerHelper.getPillQuality(player) > 0){
            return ModPillEffect.BALLS_OF_STEEL.get();
        }else if(player.getHealth() <= StatManager.MAX_HEALTH.getBonus() * 2){
            return PlayerHelper.getPillQuality(player) < 0 ?
                    ModPillEffect.I_FOUND_PILLS.get() : ModPillEffect.FULL_HEALTH.get();
        }
        return this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        if (PlayerHelper.getPillQuality(player) < 0){
            PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
        }
        StatManager.healHealth(player, -1f);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        if (PlayerHelper.getPillQuality(player) < 0){
            PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
        }
        StatManager.healHealth(player, -2f);
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

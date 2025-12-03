package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;


public class Pheromones implements IPillEffect {

    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? ModPillEffect.PARALYSIS.get() : this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        List<LivingEntity> entities = LevelHelper.selectBySphere(
                player.level(), player.getX(), player.getY(), player.getZ(), StatManager.getNearbyRange());

        MobEffectInstance charm = new MobEffectInstance(
                ModEffects.CHARM.get(),
                600,
                0,
                false,
                true,
                true
        );

        for (LivingEntity entity : entities){
            if (entity instanceof Player) continue;

            entity.addEffect(charm);
        }
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        List<LivingEntity> entities = LevelHelper.selectBySphere(
                player.level(), player.getX(), player.getY(), player.getZ(), StatManager.getNearbyRange() * 2);

        MobEffectInstance charm = new MobEffectInstance(
                ModEffects.CHARM.get(),
                1500
        );

        for (LivingEntity entity : entities){
            if (entity instanceof Player) continue;

            entity.addEffect(charm);
        }
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.PHEROMONES.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.PHEROMONES_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality) {
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.paralysis"
                : "pill.isaac_disaster.effect.pheromones";
    }

}

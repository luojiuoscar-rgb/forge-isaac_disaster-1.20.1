package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.alchemy.Potion;


public class SomethingsWrong implements IPillEffect {

    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        AreaEffectCloud cloud = new AreaEffectCloud(player.level(), player.getX(), player.getY(), player.getZ());
        cloud.setFixedColor(0x000000);
        cloud.setPotion(new Potion(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 0)));
        cloud.setDuration(600);
        cloud.setOwner(player);
        cloud.setRadius((float) StatManager.RANGE.getBonus() * 2.5f);
        player.level().addFreshEntity(cloud);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        AreaEffectCloud cloud = new AreaEffectCloud(player.level(), player.getX(), player.getY(), player.getZ());
        cloud.setFixedColor(0x000000);
        cloud.setPotion(new Potion(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 2)));
        cloud.setDuration(1500);
        cloud.setOwner(player);
        cloud.setRadius((float) StatManager.RANGE.getBonus() * 4f);
        player.level().addFreshEntity(cloud);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.SOMETHINGS_WRONG.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.SOMETHINGS_WRONG_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality){
        return "pill.isaac_disaster.effect.somethings_wrong";
    }

}


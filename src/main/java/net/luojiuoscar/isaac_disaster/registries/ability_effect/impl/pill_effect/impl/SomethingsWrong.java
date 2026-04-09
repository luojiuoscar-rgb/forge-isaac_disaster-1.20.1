package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.alchemy.Potion;


public class SomethingsWrong extends PillEffect {

    @Override
    public PillEffect redirect(ServerPlayer player) {
        return this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        AreaEffectCloud cloud = new AreaEffectCloud(player.level(), player.getX(), player.getY(), player.getZ());
        cloud.setFixedColor(0x000000);
        cloud.setPotion(new Potion(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                isHorse ? 600 : 400, isHorse ? 2 : 1)));
        cloud.setDuration(isHorse ? 1500 : 600);
        cloud.setOwner(player);
        cloud.setRadius((float) StatManager.RANGE.getBonus() * (isHorse ? 4f : 2.5f));
        player.level().addFreshEntity(cloud);
        return true;
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


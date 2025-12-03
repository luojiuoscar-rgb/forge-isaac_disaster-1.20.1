package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public class BadGas implements IPillEffect {

    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? ModPillEffect.HEALTH_DOWN.get() : this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        Level level = player.level();
        Vec3 pos = player.blockPosition().getCenter();
        MobEffectInstance effect = new MobEffectInstance(MobEffects.POISON, 300, 0);

        List<LivingEntity> entities = LevelHelper.selectBySphere(level, pos.x, pos.y, pos.z, StatManager.getNearbyRange());
        for (LivingEntity entity : entities){
            if (entity == player) continue; // 排除自己

            LevelHelper.pushEntity(level, entity, pos, 2, 0.2);
            entity.addEffect(effect);
        }

        player.level().playSound(null, BlockPos.containing(player.blockPosition().getCenter()),
                ModSounds.FART_NORMAL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        Level level = player.level();
        Vec3 pos = player.blockPosition().getCenter();
        MobEffectInstance effect = new MobEffectInstance(MobEffects.POISON, 600, 2);

        List<LivingEntity> entities = LevelHelper.selectBySphere(level, pos.x, pos.y, pos.z, StatManager.getNearbyRange());
        for (LivingEntity entity : entities){
            if (entity == player) continue; // 排除自己

            LevelHelper.pushEntity(level, entity, pos, 5.5, 0.4);
            entity.addEffect(effect);
        }

        player.level().playSound(null, BlockPos.containing(player.blockPosition().getCenter()),
                ModSounds.FART_HUGE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.BAD_GAS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.BAD_GAS_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality){
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.health_down"
                : "pill.isaac_disaster.effect.bad_gas";
    }

}

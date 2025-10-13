package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public class BadGas implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.BAD_GAS.getId();
    }

    @Override
    public void onUse(Player player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.HEALTH_DOWN.getId()).onUse(player);
            return;
        }

        onUseEffect(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), false), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseH(Player player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.HEALTH_DOWN.getId()).onUseH(player);
            return;
        }

        onUseEffectH(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseEffect(Player player) {
        Level level = player.level();
        Vec3 pos = player.blockPosition().getCenter();
        MobEffectInstance effect = new MobEffectInstance(MobEffects.POISON, 300, 0);

        List<LivingEntity> entities = LevelHelper.selectBySphere(level, pos.x, pos.y, pos.z, StatManager.getNearbyRange());
        for (LivingEntity entity : entities){
            if (entity == player) continue; // 排除自己

            LevelHelper.pushEntity(level, entity, pos, 2, 0.2);
            entity.addEffect(effect);
        }
    }

    @Override
    public void onUseEffectH(Player player) {
        Level level = player.level();
        Vec3 pos = player.blockPosition().getCenter();
        MobEffectInstance effect = new MobEffectInstance(MobEffects.POISON, 600, 2);

        List<LivingEntity> entities = LevelHelper.selectBySphere(level, pos.x, pos.y, pos.z, StatManager.getNearbyRange());
        for (LivingEntity entity : entities){
            if (entity == player) continue; // 排除自己

            LevelHelper.pushEntity(level, entity, pos, 5.5, 0.4);
            entity.addEffect(effect);
        }
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.BAD_GAS.get(), 1.0f, 1.0f);
        player.level().playSound(null, BlockPos.containing(player.blockPosition().getCenter()),
                ModSounds.FART_NORMAL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.BAD_GAS_H.get(), 1.0f, 1.0f);
        player.level().playSound(null, BlockPos.containing(player.blockPosition().getCenter()),
                ModSounds.FART_HUGE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() < 0){
            return "pill.isaac_disaster.effect.health_down";
        }
        return "pill.isaac_disaster.effect.bad_gas";
    }

}

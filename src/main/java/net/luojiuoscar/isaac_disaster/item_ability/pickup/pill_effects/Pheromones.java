package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;


public class Pheromones implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.PHEROMONES.getId();
    }

    @Override
    public void onUse(Player player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.PARALYSIS.getId()).onUse(player);
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
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.PARALYSIS.getId()).onUseH(player);
            return;
        }

        onUseEffectH(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseEffect(Player player) {
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
    public void onUseEffectH(Player player) {
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
    public void onUseSound(Player player) {
        player.playSound(ModSounds.PHEROMONES.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.PHEROMONES_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() < 0){
            return "pill.isaac_disaster.effect.paralysis";
        }
        return "pill.isaac_disaster.effect.pheromones";
    }

}

package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;


public class Paralysis implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.PARALYSIS.getId();
    }

    @Override
    public void onUse(Player player, boolean withSFX){
        if (PlayerHelper.getPillQuality(player) > 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.PHEROMONES.getId()).onUse(player, true);
            return;
        }

        onUseEffect(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), false), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseH(Player player, boolean withSFX){
        if (PlayerHelper.getPillQuality(player) > 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.PHEROMONES.getId()).onUseH(player, true);
            return;
        }

        onUseEffectH(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseEffect(Player player) {
        if (PlayerHelper.getPillQuality(player) < 0){
            PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
        }

        int duration = 120;
        MobEffectInstance dizziness = new MobEffectInstance(ModEffects.DIZZINESS.get(), duration, 255);
        MobEffectInstance lacrimal_hyposecretion = new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration);
        player.addEffect(dizziness);
        player.addEffect(lacrimal_hyposecretion);
    }

    @Override
    public void onUseEffectH(Player player) {
        if (PlayerHelper.getPillQuality(player) < 0){
            PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
        }

        int duration = 240;
        MobEffectInstance dizziness = new MobEffectInstance(ModEffects.DIZZINESS.get(), duration, 255);
        MobEffectInstance lacrimal_hyposecretion = new MobEffectInstance(ModEffects.LACRIMAL_HYPOSECRETION.get(), duration);
        player.addEffect(dizziness);
        player.addEffect(lacrimal_hyposecretion);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.PARALYSIS.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.PARALYSIS_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() > 0){
            return "pill.isaac_disaster.effect.pheromones";
        }
        return "pill.isaac_disaster.effect.paralysis";
    }

}

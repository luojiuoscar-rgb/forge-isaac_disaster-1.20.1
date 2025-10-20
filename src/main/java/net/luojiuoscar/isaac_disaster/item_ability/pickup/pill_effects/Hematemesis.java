package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.fixes.EntityHealthFix;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.system.linux.Stat;

import java.util.Random;


public class Hematemesis implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.HEMATEMESIS.getId();
    }

    @Override
    public void onUse(Player player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.BAD_TRIP.getId()).onUse(player);
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
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.BAD_TRIP.getId()).onUseH(player);
            return;
        }

        onUseEffectH(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseEffect(Player player) {
        RandomSource random = player.getRandom();

        float health = player.getHealth() - 2;
        int hearts = (int) Math.ceil(health / StatManager.getHealthBonus()) + random.nextInt(0,3);
        // 转换成红心
        PlayerHelper.spawnItem(player, ModItems.RED_HEART.get(), hearts);
        player.setHealth(2);
    }

    @Override
    public void onUseEffectH(Player player) {
        RandomSource random = player.getRandom();

        float health = player.getHealth() - 2;
        int hearts = (int) Math.ceil(health / StatManager.getHealthBonus()+ random.nextDouble() * 1.2) + random.nextInt(2,6);
        // 转换成红心
        PlayerHelper.spawnItem(player, ModItems.RED_HEART.get(), hearts);
        player.setHealth(2);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.HEMATEMESIS.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.HEMATEMESIS_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() < 0){
            return "pill.isaac_disaster.effect.bad_trip";
        }
        return "pill.isaac_disaster.effect.hematemesis";
    }

}

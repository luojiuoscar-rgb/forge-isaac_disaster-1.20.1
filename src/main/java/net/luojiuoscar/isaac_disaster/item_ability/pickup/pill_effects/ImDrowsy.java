package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;


public class ImDrowsy implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.IM_DROWSY.getId();
    }

    @Override
    public void onUse(Player player, boolean withSFX){
        if (PlayerHelper.getPillQuality(player) > 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.IM_EXCITED.getId()).onUse(player, true);
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
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.IM_EXCITED.getId()).onUseH(player, true);
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

        int duration = 600;
        int amplifier = 0;
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, amplifier));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));
    }

    @Override
    public void onUseEffectH(Player player) {
        if (PlayerHelper.getPillQuality(player) < 0){
            PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
        }

        int duration = 1500;
        int amplifier = 2;
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, amplifier));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.IM_DROWSY.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.IM_DROWSY_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() > 0){
            return "pill.isaac_disaster.effect.im_excited";
        }
        return "pill.isaac_disaster.effect.im_drowsy";
    }

}

package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;


public class Addicted implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.ADDICTED.getId();
    }

    @Override
    public void onUse(Player player){
        if (PlayerHelper.getPillQuality(player) > 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.PERCS.getId()).onUse(player);
            return;
        }

        onUseEffect(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), false), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseH(Player player){
        if (PlayerHelper.getPillQuality(player) > 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.PERCS.getId()).onUseH(player);
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

        MobEffectInstance percs = new MobEffectInstance(
                ModEffects.VULNERABLE.get(),
                600,
                1
        );
        player.addEffect(percs);
    }

    @Override
    public void onUseEffectH(Player player) {
        if (PlayerHelper.getPillQuality(player) < 0){
            PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
        }

        MobEffectInstance percs = new MobEffectInstance(
                ModEffects.VULNERABLE.get(),
                1500,
                1
        );
        player.addEffect(percs);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.ADDICTED.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.ADDICTED_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() > 0){
            return "pill.isaac_disaster.effect.percs";
        }
        return "pill.isaac_disaster.effect.addicted";
    }

}

package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;


public class RetroVision implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.RETRO_VISION.getId();
    }

    @Override
    public void onUse(Player player, boolean withSFX){
        if (PlayerHelper.getPillQuality(player) > 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.I_CAN_SEE_FOREVER.getId()).onUse(player, true);
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
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.I_CAN_SEE_FOREVER.getId()).onUseH(player, true);
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

        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 600));
    }

    @Override
    public void onUseEffectH(Player player) {
        if (PlayerHelper.getPillQuality(player) < 0){
            PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
        }

        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 1500));
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.RETRO_VISION.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.RETRO_VISION_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD_H.get(), 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() > 0){
            return "pill.isaac_disaster.effect.i_can_see_forever";
        }
        return "pill.isaac_disaster.effect.retro_vision";
    }

}

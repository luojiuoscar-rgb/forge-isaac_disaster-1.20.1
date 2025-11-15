package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;


public class QuestionPill implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.QUESTION_PILL.getId();
    }

    @Override
    public void onUse(Player player, boolean withSFX){
        if (PlayerHelper.getPillQuality(player) > 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.TELEPILLS.getId()).onUse(player, true);
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
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.TELEPILLS.getId()).onUseH(player, true);
            return;
        }

        onUseEffectH(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }
    }



    @Override
    public void onUseEffect(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.CURSE_OF_THE_MAZE.get(), 600, 0));
    }

    @Override
    public void onUseEffectH(Player player) {
        player.addEffect(new MobEffectInstance(ModEffects.CURSE_OF_THE_MAZE.get(), 1500, 0));
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.PILL_USE_BAD.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.QUESTION_PILL.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.PILL_USE_BAD_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.QUESTION_PILL_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() > 0){
            return "pill.isaac_disaster.effect.telepills";
        }
        return "pill.isaac_disaster.effect.question_pill";
    }

}

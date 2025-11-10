package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


public class OneMakesYouLarger implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.ONE_MAKES_YOU_LARGER.getId();
    }

    @Override
    public void onUse(Player player, boolean withSFX){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.RANGE_DOWN.getId()).onUse(player, true);
            return;
        }

        onUseEffect(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), false), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseH(Player player, boolean withSFX){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.RANGE_DOWN.getId()).onUseH(player, true);
            return;
        }

        onUseEffectH(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseEffect(Player player) {
        StatManager.SCALE.apply(player, 1);
    }

    @Override
    public void onUseEffectH(Player player) {
        StatManager.SCALE.apply(player, 2);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.ONE_MAKES_YOU_LARGER.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.ONE_MAKES_YOU_LARGER_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() < 0){
            return "pill.isaac_disaster.effect.range_down";
        }
        return "pill.isaac_disaster.effect.one_makes_you_larger";
    }

}

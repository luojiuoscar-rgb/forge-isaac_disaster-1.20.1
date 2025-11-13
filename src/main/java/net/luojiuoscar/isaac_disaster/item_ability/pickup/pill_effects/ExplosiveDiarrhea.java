package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;


public class ExplosiveDiarrhea implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.EXPLOSIVE_DIARRHEA.getId();
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
        ScheduledFuncHelper.schedule("explosive_diarrhea", 15, 5, false,
                () -> EntityHelper.spawnBomb(player.position(), player, player.level(), Vec3.ZERO, 1));
    }

    @Override
    public void onUseEffectH(Player player) {
        ScheduledFuncHelper.schedule("explosive_diarrhea_h", 15, 8, false,
                () -> EntityHelper.spawnBomb(player.position(), player, player.level(), Vec3.ZERO, 2));
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.EXPLOSIVE_DIARRHEA.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.EXPLOSIVE_DIARRHEA_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD_H.get(), 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId() {
        if (ClientDataManager.getInstance().getPillQuality() < 0){
            return "pill.isaac_disaster.effect.range_down";
        }
        return "pill.isaac_disaster.effect.explosive_diarrhea";
    }

}

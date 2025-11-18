package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


public class HealthDown implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.HEALTH_DOWN.getId();
    }

    @Override
    public void onUse(Player player, boolean withSFX){
        if (PlayerHelper.getPillQuality(player) > 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.HEALTH_UP.getId()).onUse(player, true);
            return;
        }
        if (player.getMaxHealth() <= StatManager.MAX_HEALTH.getBonus() * 0.5){
            if (PlayerHelper.getPillQuality(player) < 0){
                PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.I_FOUND_PILLS.getId()).onUse(player, true);
                return;
            }

            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.HEALTH_UP.getId()).onUse(player, true);
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
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.HEALTH_UP.getId()).onUseH(player, true);
            return;
        }
        if (player.getMaxHealth() <= StatManager.MAX_HEALTH.getBonus()){
            if (PlayerHelper.getPillQuality(player) < 0){
                PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.I_FOUND_PILLS.getId()).onUseH(player, true);
                return;
            }

            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.HEALTH_UP.getId()).onUseH(player, true);
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
            StatManager.DAMAGE.apply(player, 0.4);
        }

        StatManager.MAX_HEALTH.apply(player, -0.5);
    }

    @Override
    public void onUseEffectH(Player player) {
        if (PlayerHelper.getPillQuality(player) < 0){
            StatManager.DAMAGE.apply(player, 0.8);
        }
        StatManager.MAX_HEALTH.apply(player, -1);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.HEALTH_DOWN.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.HEALTH_DOWN_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_BAD_H.get(), 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() > 0){
            return "pill.isaac_disaster.effect.health_up";
        }
        return "pill.isaac_disaster.effect.health_down";
    }

}

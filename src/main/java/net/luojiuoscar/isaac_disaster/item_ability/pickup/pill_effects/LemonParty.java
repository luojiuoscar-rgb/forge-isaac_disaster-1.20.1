package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.entity.custom.LemonEffectCloud;
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


public class LemonParty implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.LEMON_PARTY.getId();
    }

    @Override
    public void onUse(Player player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.AMNESIA.getId()).onUse(player);
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
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.AMNESIA.getId()).onUseH(player);
            return;
        }

        onUseEffectH(player);
        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }
    }

    @Override
    public void onUseEffect(Player player) {
        LemonEffectCloud cloud = new LemonEffectCloud(player.level(), player.getX(), player.getY(), player.getZ(),
                player, (float) StatManager.getNearbyRange() * 0.4f, 200, 0, 10,
                (float) StatManager.DAMAGE.getBonus() * 2.5f);
        player.level().addFreshEntity(cloud);
    }

    @Override
    public void onUseEffectH(Player player) {
        LemonEffectCloud cloud = new LemonEffectCloud(player.level(), player.getX(), player.getY(), player.getZ(),
                player, (float) StatManager.getNearbyRange() * 0.8f, 400, 0, 10,
                (float) StatManager.DAMAGE.getBonus() * 4f);
        player.level().addFreshEntity(cloud);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.LEMON_PARTY.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.LEMON_PARTY_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD_H.get(), 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() < 0){
            return "pill.isaac_disaster.effect.amnesia";
        }
        return "pill.isaac_disaster.effect.lemon_party";
    }

}

package net.luojiuoscar.isaac_disaster.item_ability.pickup.pill_effects;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.IPillEffect;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.id.PillEffectId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.PillOnUseS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbility;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


public class PowerPill implements IPillEffect {
    @Override
    public int getPillEffectId() {
        return PillEffectId.POWER_PILL.getId();
    }
    @Override
    public void onUse(ServerPlayer player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.RUA_WIZARD.getId()).onUse(player);
            return;
        }


        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), false), (ServerPlayer) player);
        }else if (player instanceof ServerPlayer){
            onUseEffect((ServerPlayer) player);
        }
    }

    @Override
    public void onUseH(ServerPlayer player){
        if (PlayerHelper.getPillQuality(player) < 0){
            PillEffectManager.getInstance().getEffectFromEffectId(PillEffectId.RUA_WIZARD.getId()).onUseH(player);
            return;
        }

        if (!player.level().isClientSide){
            ModMessages.sentToPlayer(new PillOnUseS2CPacket(getPillEffectId(), true), (ServerPlayer) player);
        }else if (player instanceof ServerPlayer){
            onUseEffect((ServerPlayer) player);
        }
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        ModActiveAbility.THE_GAMEKID.get().onTrigger(player, null);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        ModActiveAbility.THE_GAMEKID.get().onTriggerStronger(player, null);
    }

    @Override
    public void onUseSound(Player player) {
        player.playSound(ModSounds.POWER_PILL.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD.get(), 1.0f, 1.0f);
    }

    @Override
    public void onUseSoundH(Player player) {
        player.playSound(ModSounds.POWER_PILL_H.get(), 1.0f, 1.0f);
        player.playSound(ModSounds.PILL_USE_GOOD_H.get(), 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId(){
        if (ClientDataManager.getInstance().getPillQuality() < 0){
            return "pill.isaac_disaster.effect.rua_wizard";
        }
        return "pill.isaac_disaster.effect.power_pill";
    }

}

package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModPills;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;


public class Vurp implements IPillEffect {
    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        player.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                playerItemUseRecord -> {
                    var pills = playerItemUseRecord.getPillRecords();
                    if (pills.isEmpty()){
                        PlayerHelper.giveItem(player, new ItemStack(ModPills.getGoldenPill(false).get()));
                    }else{
                        int id = pills.get(0).id();
                        PlayerHelper.giveItem(player, new ItemStack(ModPills.getPillById(id, false).get()));
                    }
                }
        );
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        player.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                playerItemUseRecord -> {
                    var pills = playerItemUseRecord.getPillRecords();
                    if (pills.isEmpty()){
                        PlayerHelper.giveItem(player, new ItemStack(ModPills.getGoldenPill(true).get()));
                    }else{
                        int id = pills.get(0).id();
                        PlayerHelper.giveItem(player, new ItemStack(ModPills.getPillById(id, true).get()));
                    }
                }
        );
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.VURP.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.VURP_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality){
        return "pill.isaac_disaster.effect.vurp";
    }

}

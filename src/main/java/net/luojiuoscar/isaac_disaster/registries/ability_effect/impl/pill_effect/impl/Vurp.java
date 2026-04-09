package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModPills;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;


public class Vurp extends PillEffect {
    @Override
    public PillEffect redirect(ServerPlayer player) {
        return this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        player.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                playerItemUseRecord -> {
                    var pills = playerItemUseRecord.getPillRecords();
                    if (pills.isEmpty()){
                        PlayerHelper.giveItem(player, new ItemStack(ModPills.getGoldenPill(isHorse).get()));
                    }else{
                        int id = pills.get(0).id();
                        PlayerHelper.giveItem(player, new ItemStack(ModPills.getPillById(id, isHorse).get()));
                    }
                }
        );
        return true;
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

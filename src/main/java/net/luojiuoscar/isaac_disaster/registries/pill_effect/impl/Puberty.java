package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;


public class Puberty implements IPillEffect {
    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        StatManager.modifySetWithId(player, ModSetAbility.ADULT.getId(), 1);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        StatManager.modifySetWithId(player, ModSetAbility.ADULT.getId(), 2);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.PUBERTY.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_BAD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.PUBERTY_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_BAD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId(int pillQuality){
        return "pill.isaac_disaster.effect.puberty";
    }

}

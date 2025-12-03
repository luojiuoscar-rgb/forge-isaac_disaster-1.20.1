package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;


public class OneMakesYouLarger implements IPillEffect {

    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? ModPillEffect.RANGE_DOWN.get() : this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        StatManager.SCALE.apply(player, 1);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        StatManager.SCALE.apply(player, 2);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.ONE_MAKES_YOU_LARGER.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.ONE_MAKES_YOU_LARGER_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality) {
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.range_down"
                : "pill.isaac_disaster.effect.one_makes_you_larger";
    }

}

package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.entity.custom.LemonEffectCloud;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;


public class LemonParty implements IPillEffect {

    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? ModPillEffect.AMNESIA.get() : this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        LemonEffectCloud cloud = new LemonEffectCloud(player.level(), player.getX(), player.getY(), player.getZ(),
                player, (float) StatManager.getNearbyRange() * 0.4f, 200, 0, 10,
                (float) StatManager.DAMAGE.getBonus() * 2.5f);
        player.level().addFreshEntity(cloud);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        LemonEffectCloud cloud = new LemonEffectCloud(player.level(), player.getX(), player.getY(), player.getZ(),
                player, (float) StatManager.getNearbyRange() * 0.8f, 400, 0, 10,
                (float) StatManager.DAMAGE.getBonus() * 4f);
        player.level().addFreshEntity(cloud);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.LEMON_PARTY.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.LEMON_PARTY_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality) {
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.amnesia"
                : "pill.isaac_disaster.effect.lemon_party";
    }
}

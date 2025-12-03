package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;


public class Energy48 implements IPillEffect {

    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? ModPillEffect.SPEED_DOWN.get() : this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        PlayerHelper.chargeAll(player, null);
        LootHelper.spawnItemViaLoot(player, player.position(), ModItems.SMALL_BATTERY.get(),
                player.getRandom().nextInt(1,3));
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        PlayerHelper.chargeAll(player, null, true);
        LootHelper.spawnItemViaLoot(player, player.position(), ModItems.SMALL_BATTERY.get(),
                player.getRandom().nextInt(3,5));
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.ENERGY_48.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.ENERGY_48_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality){
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.speed_down"
                : "pill.isaac_disaster.effect.48_hour_energy";
    }

}

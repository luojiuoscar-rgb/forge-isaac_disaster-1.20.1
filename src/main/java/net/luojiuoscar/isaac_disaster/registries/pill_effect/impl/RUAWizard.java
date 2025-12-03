package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;


public class RUAWizard implements IPillEffect {

    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) > 0 ? ModPillEffect.POWER_PILL.get() : this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        if (PlayerHelper.getPillQuality(player) < 0){
            PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
        }

        MobEffectInstance wizard = new MobEffectInstance(
                ModEffects.THE_WIZ.get(),
                600,
                0
        );
        player.addEffect(wizard);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        if (PlayerHelper.getPillQuality(player) < 0){
            PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
        }

        MobEffectInstance wizard = new MobEffectInstance(
                ModEffects.THE_WIZ.get(),
                1500,
                0
        );
        player.addEffect(wizard);
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.PILL_USE_BAD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.RUA_WIZARD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.PILL_USE_BAD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.RUA_WIZARD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality) {
        return pillQuality > 0
                ? "pill.isaac_disaster.effect.power_pill"
                : "pill.isaac_disaster.effect.rua_wizard";
    }
}

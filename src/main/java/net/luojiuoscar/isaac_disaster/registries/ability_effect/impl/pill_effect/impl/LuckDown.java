package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.interfaces.AddDamageIfLowQuality;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;


public class LuckDown extends PillEffect implements AddDamageIfLowQuality {

    @Override
    public PillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) > 0 ? (PillEffect) ModExecutableEffects.LUCK_UP.get() : this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        StatManager.LUCK.apply(player, isHorse ? -2 : -1);
        return true;
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.LUCK_DOWN.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_BAD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.LUCK_DOWN_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_BAD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId(int pillQuality) {
        return pillQuality > 0
                ? "pill.isaac_disaster.effect.luck_up"
                : "pill.isaac_disaster.effect.luck_down";
    }

}

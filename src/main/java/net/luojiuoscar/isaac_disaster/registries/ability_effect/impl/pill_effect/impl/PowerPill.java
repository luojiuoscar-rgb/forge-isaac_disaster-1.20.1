package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;


public class PowerPill extends PillEffect {
    @Override
    public PillEffect redirect(ServerPlayer player) {
        return PlayerHelper.getPillQuality(player) < 0 ? (PillEffect) ModExecutableEffects.RUA_WIZARD.get() : this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        ActiveAbility ability = ModActiveAbility.THE_GAMEKID.get();

        context.set(ContextKeys.AMPLIFIER, isHorse ? ability.getStrongerAmplifier() : ability.getNormalAmplifier());
        ability.getTrigger().fire(context, null);
        return true;
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.POWER_PILL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.POWER_PILL_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

    }

    @Override
    public String getDescriptionId(int pillQuality) {
        return pillQuality < 0
                ? "pill.isaac_disaster.effect.rua_wizard"
                : "pill.isaac_disaster.effect.power_pill";
    }

}

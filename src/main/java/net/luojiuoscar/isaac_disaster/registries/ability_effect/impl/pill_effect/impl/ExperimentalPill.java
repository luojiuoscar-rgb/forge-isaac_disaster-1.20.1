package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;


public class ExperimentalPill extends PillEffect {
    private final List<RegistryObject<IExecutableEffect>> effects_good = List.of(
            ModExecutableEffects.HEALTH_UP,
            ModExecutableEffects.LUCK_UP,
            ModExecutableEffects.RANGE_UP,
            ModExecutableEffects.SHOT_SPEED_UP,
            ModExecutableEffects.TEARS_UP,
            ModExecutableEffects.SPEED_UP
    );

    private final List<RegistryObject<IExecutableEffect>> effects_bad = List.of(
            ModExecutableEffects.HEALTH_DOWN,
            ModExecutableEffects.LUCK_DOWN,
            ModExecutableEffects.RANGE_DOWN,
            ModExecutableEffects.SHOT_SPEED_DOWN,
            ModExecutableEffects.TEARS_DOWN,
            ModExecutableEffects.SPEED_DOWN
    );

    @Override
    public PillEffect redirect(ServerPlayer player) {
        return this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        RandomSource r = player.getRandom();

        int ig = r.nextInt(effects_good.size());
        int ib = r.nextInt(effects_bad.size());
        if (ig == ib) ib = (ib + 1) % effects_bad.size();

        effects_good.get(ig).get().apply(context);
        effects_bad.get(ib).get().apply(context);
        return true;
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.EXPERIMENTAL_PILL.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.EXPERIMENTAL_PILL_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        player.playNotifySound(ModSounds.PILL_USE_GOOD_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality){
        return "pill.isaac_disaster.effect.experimental_pill";
    }

}

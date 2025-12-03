package net.luojiuoscar.isaac_disaster.registries.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;


public class ExperimentalPill implements IPillEffect {
    private final List<RegistryObject<IPillEffect>> effects_good = List.of(
            ModPillEffect.HEALTH_UP,
            ModPillEffect.LUCK_UP,
            ModPillEffect.RANGE_UP,
            ModPillEffect.SHOT_SPEED_UP,
            ModPillEffect.TEARS_UP,
            ModPillEffect.SPEED_UP
    );

    private final List<RegistryObject<IPillEffect>> effects_bad = List.of(
            ModPillEffect.HEALTH_DOWN,
            ModPillEffect.LUCK_DOWN,
            ModPillEffect.RANGE_DOWN,
            ModPillEffect.SHOT_SPEED_DOWN,
            ModPillEffect.TEARS_DOWN,
            ModPillEffect.SPEED_DOWN
    );

    @Override
    public IPillEffect redirect(ServerPlayer player) {
        return this;
    }

    @Override
    public void onUseEffect(ServerPlayer player) {
        RandomSource r = player.getRandom();

        int ig = r.nextInt(effects_good.size());
        int ib = r.nextInt(effects_bad.size());
        if (ig == ib) ib = (ib + 1) % effects_bad.size();

        effects_good.get(ig).get().onUseEffect(player);
        effects_bad.get(ib).get().onUseEffect(player);
    }

    @Override
    public void onUseEffectH(ServerPlayer player) {
        RandomSource r = player.getRandom();

        int ig = r.nextInt(effects_good.size());
        int ib = r.nextInt(effects_bad.size());
        if (ig == ib) ib = (ib + 1) % effects_bad.size();

        effects_good.get(ig).get().onUseEffectH(player);
        effects_bad.get(ib).get().onUseEffectH(player);
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

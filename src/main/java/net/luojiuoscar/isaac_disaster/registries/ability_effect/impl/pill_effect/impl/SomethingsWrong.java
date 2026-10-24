package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.ModGroundEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.world.effect.MobEffects;
import java.util.List;


public class SomethingsWrong extends PillEffect {

    @Override
    public PillEffect redirect(ServerPlayer player) {
        return this;
    }

    @Override
    protected boolean pillActive(ServerPlayer player, boolean isHorse, ExecutableEffectContext context) {
        var puddle = GroundEffectRuntime.INSTANCE.spawnOnGround(ModGroundEffects.POTION.get(), player, player.position(),
                (float) StatManager.RANGE.getBonus() * (isHorse ? 4f : 2.5f), isHorse ? 1500 : 600,
                0, 10);
        if (puddle == null) return false;
        // AreaEffectCloud quartered base-potion durations: preserve the old applied effect, not its input duration.
        puddle.setColor(0x000000);
        puddle.setPotions(List.of(new PotionProfile(MobEffects.MOVEMENT_SLOWDOWN,
                isHorse ? 150 : 100, isHorse ? 2 : 1)));
        return true;
    }

    @Override
    public void makeSound(ServerPlayer player) {
        player.playNotifySound(ModSounds.SOMETHINGS_WRONG.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void makeSoundH(ServerPlayer player) {
        player.playNotifySound(ModSounds.SOMETHINGS_WRONG_H.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public String getDescriptionId(int pillQuality){
        return "pill.isaac_disaster.effect.somethings_wrong";
    }

}


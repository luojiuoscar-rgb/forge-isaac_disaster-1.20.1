package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Fart implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        Vec3 pos = context.getOrDefault(ContextKeys.TARGET_POSITION, entity.position());

        List<Double> nums = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        nums = new ArrayList<>(nums);
        if (nums.size() < 2) nums = List.of(120., 0.);

        int duration = nums.get(0).intValue();
        int amplifier = nums.get(1).intValue();

        if (duration < 20) duration = 20;
        amplifier = Mth.clamp(amplifier, 0, 255);

        context.set(ContextKeys.POTIONS, List.of(
                new PotionProfile(
                        MobEffects.POISON,
                        duration,
                        amplifier
                )
        ));

        if (context.get(ContextKeys.BOOLEAN) == null)
            context.set(ContextKeys.BOOLEAN, List.of(true));

        // 施加中毒效果
        ModExecutableEffects.APPLY_EFFECT_TO_NEARBY.get().apply(context);

        if (amplifier > 1){
            entity.level().playSound(null, pos.x, pos.y, pos.z,
                    ModSounds.FART_NORMAL.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        }else {
            entity.level().playSound(null, pos.x, pos.y, pos.z,
                    ModSounds.FART_HUGE.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        }
        return true;
    }
}

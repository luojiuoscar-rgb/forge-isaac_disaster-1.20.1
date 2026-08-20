package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.revive;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

public class SimpleReviveEffect extends ReviveExecutableEffect {
    private static final int INVINCIBLE_DURATION = 100;
    private static final float HEAL_AMOUNT = 1.0F;
    private static final int FIRE_RESISTANCE_DURATION = 800;

    @Override
    protected void applyReviveEffect(ExecutableEffectContext context) {
        ExecutableEffectContext potionContext = context.copy(null);
        potionContext.set(ContextKeys.POTIONS, List.of(
                new PotionProfile(ModEffects.INVINCIBLE.get(), INVINCIBLE_DURATION, 0),
                new PotionProfile(MobEffects.FIRE_RESISTANCE, FIRE_RESISTANCE_DURATION, 0)
        ));
        ModExecutableEffects.POTIONS.get().apply(potionContext);

        ExecutableEffectContext healContext = context.copy(null);
        healContext.set(ContextKeys.AMPLIFIER, (double) HEAL_AMOUNT);
        ModExecutableEffects.HEAL.get().apply(healContext);
    }
}

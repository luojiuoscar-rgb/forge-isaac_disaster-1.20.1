package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.revive;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

public class TotemOfUndyingReviveEffect extends ReviveExecutableEffect {
    private static final int REGENERATION_DURATION = 900;
    private static final int ABSORPTION_DURATION = 100;
    private static final int FIRE_RESISTANCE_DURATION = 800;

    @Override
    protected void applyReviveEffect(ExecutableEffectContext context) {
        context.getEntity().removeAllEffects();

        ExecutableEffectContext potionContext = context.copy(null);
        potionContext.set(ContextKeys.POTIONS, List.of(
                new PotionProfile(MobEffects.REGENERATION, REGENERATION_DURATION, 1),
                new PotionProfile(MobEffects.ABSORPTION, ABSORPTION_DURATION, 1),
                new PotionProfile(MobEffects.FIRE_RESISTANCE, FIRE_RESISTANCE_DURATION, 0)
        ));
        ModExecutableEffects.POTIONS.get().apply(potionContext);
    }
}

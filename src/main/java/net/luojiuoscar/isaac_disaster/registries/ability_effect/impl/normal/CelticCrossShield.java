package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import java.util.List;

public final class CelticCrossShield implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        var entity = context.getEntity();
        var existing = entity.getEffect(ModEffects.INVINCIBLE.get());
        if (existing != null && existing.isInfiniteDuration()) return true;
        int duration = 140 + (existing == null ? 0 : existing.getDuration());
        context.set(ContextKeys.AMPLIFIER, 1.0);
        context.set(ContextKeys.POTIONS, List.of(new PotionProfile(ModEffects.INVINCIBLE.get(), duration, 0, 0, 0, true)));
        ModExecutableEffects.POTIONS.get().apply(context);
        return true;
    }
}

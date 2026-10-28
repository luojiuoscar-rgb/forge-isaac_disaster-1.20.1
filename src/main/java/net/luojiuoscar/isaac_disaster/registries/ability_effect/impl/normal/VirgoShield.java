package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public final class VirgoShield implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        var entity = context.getEntity();
        if (entity == null || !(entity.level() instanceof ServerLevel)) return false;
        var existing = entity.getEffect(ModEffects.INVINCIBLE.get());
        if (existing != null && existing.isInfiniteDuration()) return true;
        int duration = (int) Math.min(Integer.MAX_VALUE,
                200L + (existing == null ? 0 : Math.max(0, existing.getDuration())));
        var target = context.copy(entity);
        target.set(ContextKeys.AMPLIFIER, 1.0);
        target.set(ContextKeys.POTIONS, List.of(new PotionProfile(
                ModEffects.INVINCIBLE.get(), duration, existing == null ? 0 : existing.getAmplifier(), 0, 0, true)));
        return ModExecutableEffects.POTIONS.get() instanceof IAbilityEffect effect && effect.applyEffect(target);
    }
}

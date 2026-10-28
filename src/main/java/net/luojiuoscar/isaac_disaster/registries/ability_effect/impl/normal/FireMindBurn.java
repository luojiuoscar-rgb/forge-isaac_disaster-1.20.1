package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.custom.BurningEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;

public final class FireMindBurn implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        var caster = context.getEntity();
        var targets = context.get(ContextKeys.SECONDARY_LIVING_ENTITIES);
        if (caster == null || !(caster.level() instanceof ServerLevel)
                || targets == null || targets.isEmpty()) return false;
        var attribute = caster.getAttribute(Attributes.ATTACK_DAMAGE);
        int amplifier = BurningEffect.amplifierForDamage(attribute == null ? 0 : attribute.getValue());
        for (var target : targets) {
            if (target == null || target.level() != caster.level()) return false;
            if (!target.isAlive() || target.fireImmune()) continue;
            var existing = target.getEffect(ModEffects.BURNING.get());
            int strength = existing == null ? amplifier : Math.max(amplifier, existing.getAmplifier());
            var targetContext = context.copy(target);
            targetContext.set(ContextKeys.AMPLIFIER, 1.0);
            targetContext.set(ContextKeys.POTIONS, List.of(new PotionProfile(
                    ModEffects.BURNING.get(), BurningEffect.DURATION, strength)));
            if (!(ModExecutableEffects.POTIONS.get() instanceof IAbilityEffect effect)
                    || !effect.applyEffect(targetContext)) return false;
        }
        return true;
    }
}

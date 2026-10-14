package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.effect.custom.DeadlyPoisonEffect;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.List;

public final class ScorpioPoison implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        var targets = context.get(ContextKeys.SECONDARY_LIVING_ENTITIES);
        if (targets == null || targets.isEmpty()) return false;
        var attribute = context.getEntity().getAttribute(Attributes.ATTACK_DAMAGE);
        int amplifier = DeadlyPoisonEffect.amplifierForDamage(attribute == null ? 0 : attribute.getValue());
        var potion = new PotionProfile(ModEffects.DEADLY_POISON.get(), 40, amplifier);
        for (var target : targets) {
            var copy = context.copy(target);
            copy.set(ContextKeys.AMPLIFIER, 1.0);
            copy.set(ContextKeys.POTIONS, List.of(potion));
            ModExecutableEffects.POTIONS.get().apply(copy);
        }
        return true;
    }
}

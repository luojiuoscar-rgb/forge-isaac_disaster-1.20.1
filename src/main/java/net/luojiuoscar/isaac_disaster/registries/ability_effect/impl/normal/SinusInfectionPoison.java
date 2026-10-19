package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import java.util.List;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.effect.custom.DeadlyPoisonEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;

public final class SinusInfectionPoison implements IAbilityEffect {
    @Override
    public TriggerType getRequiredTriggerType() { return ModTriggerTypes.BULLET_STICK_START; }

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        var bullet = context.get(ContextKeys.BULLET);
        var stick = context.get(ContextKeys.BULLET_STICK);
        if (bullet == null || stick == null || stick.bullet() != bullet) return false;
        var target = stick.target();
        if (target == null || !target.isAlive() || target.isRemoved()
                || target.level() != bullet.getBulletLevel()) return false;
        int amplifier = DeadlyPoisonEffect.amplifierForDamage(bullet.getDamage());
        var targetContext = context.copy(target);
        targetContext.set(ContextKeys.AMPLIFIER, 1.0D);
        targetContext.set(ContextKeys.POTIONS,
                List.of(new PotionProfile(ModEffects.DEADLY_POISON.get(), 200, amplifier)));
        if (!(ModExecutableEffects.POTIONS.get() instanceof IAbilityEffect potions)) return false;
        return potions.applyEffect(targetContext);
    }
}

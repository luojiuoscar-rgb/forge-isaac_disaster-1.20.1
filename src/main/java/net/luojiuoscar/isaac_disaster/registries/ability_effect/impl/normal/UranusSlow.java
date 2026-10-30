package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;

import java.util.List;

public final class UranusSlow implements IAbilityEffect {
    public static final int DURATION = 80;

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        var source = context.getEntity();
        var targets = context.get(ContextKeys.SECONDARY_LIVING_ENTITIES);
        var event = context.get(ContextKeys.EVENT);
        if (source == null || !(source.level() instanceof ServerLevel)
                || targets == null || targets.isEmpty() || event != null && event.isCancelable() && event.isCanceled()) return false;
        for (var target : targets) {
            if (target == null || target.level() != source.level()) return false;
            if (!target.isAlive()) continue;
            var targetContext = context.copy(target);
            targetContext.set(ContextKeys.AMPLIFIER, 1.0);
            targetContext.set(ContextKeys.POTIONS, List.of(new PotionProfile(MobEffects.MOVEMENT_SLOWDOWN, DURATION, 0)));
            if (!(ModExecutableEffects.POTIONS.get() instanceof IAbilityEffect effect)
                    || !effect.applyEffect(targetContext)) return false;
        }
        return true;
    }
}

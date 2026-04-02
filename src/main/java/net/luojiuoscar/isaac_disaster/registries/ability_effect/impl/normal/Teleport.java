package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;

import java.util.List;

public class Teleport implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        List<Double> radius = context.getOrDefault(ContextKeys.DOUBLE, List.of());
        if (radius.isEmpty()) radius = List.of(64.);

        EntityHelper.teleportToRandomLocation(context.getEntity(), radius.get(0));
        return true;
    }
}
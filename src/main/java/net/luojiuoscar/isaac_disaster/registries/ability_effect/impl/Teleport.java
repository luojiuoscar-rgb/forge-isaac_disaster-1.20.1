package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;

public class Teleport implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        EntityHelper.teleportToRandomLocation(context.getEntity(), 64);
    }
}
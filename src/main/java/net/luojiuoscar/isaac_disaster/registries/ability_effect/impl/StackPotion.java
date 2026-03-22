package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class StackPotion implements IAbilityEffect {
    @Override
    public void apply(AbilityEffectContext context) {
        var potions = context.get(ContextKeys.POTIONS);
        if (potions.isEmpty()) return;

        int multiplier = context.getOrDefault(ContextKeys.AMPLIFIER, 1);
        LivingEntity entity = context.getEntity();


        List<Boolean> booleans = context.getOrDefault(ContextKeys.BOOLEAN, List.of());
        while (booleans.size() < 2) {
            booleans.add(false);
        }
        boolean stack_duration = booleans.get(0);
        boolean stack_amplifier = booleans.get(1);

        for (var potion : potions){
            EntityHelper.applyOrStackEffect(
                    entity,
                    potion.effect,
                    potion.duration + potion.duration_increment * multiplier,
                    potion.duration + potion.amplifier_increment * multiplier,
                    stack_duration,
                    stack_amplifier);
        }
    }
}
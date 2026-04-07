package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * booleans: stack_duration? stack_amplifier?
 *
 */
public class StackPotion implements IAbilityEffect {
    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        var potions = context.get(ContextKeys.POTIONS);
        if (potions.isEmpty()) return false;

        int multiplier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
        LivingEntity entity = context.getEntity();

        List<Boolean> booleans = context.getOrDefault(ContextKeys.BOOLEAN, List.of());

        booleans = new ArrayList<>(booleans);
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
        return true;
    }
}
package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general;

import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * booleans: stack_duration? stack_amplifier?
 *
 */
public class StackPotion implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        var potions = context.get(ContextKeys.POTIONS);
        if (potions == null || potions.isEmpty()) return false;

        int multiplier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue() - 1;
        LivingEntity entity = context.getEntity();

        List<Boolean> booleans = context.getOrDefault(ContextKeys.BOOLEAN, List.of());

        booleans = new ArrayList<>(booleans);
        while (booleans.size() < 2) {
            booleans.add(false);
        }
        boolean stack_duration = booleans.get(0);
        boolean stack_amplifier = booleans.get(1);

        for (var potion : potions){
            int finalDuration = Mth.clamp(potion.duration + potion.duration_increment * multiplier, 0, 1000000);
            int finalAmplifier = Mth.clamp(potion.amplifier + potion.amplifier_increment * multiplier, 0, 255);

            EntityHelper.applyOrStackEffect(
                    entity,
                    potion.effect,
                    finalDuration,
                    finalAmplifier,
                    stack_duration,
                    stack_amplifier);
        }
        return true;
    }
}
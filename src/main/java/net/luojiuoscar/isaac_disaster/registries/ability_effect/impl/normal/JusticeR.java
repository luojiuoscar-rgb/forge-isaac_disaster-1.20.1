package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class JusticeR implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        LivingEntity entity = context.getEntity();
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();

        int a = amplifier;
        int b = 5 * amplifier - 5;
        if (b < a) b = a + 2;

        int num = entity.getRandom().nextInt(a, b);

        context.set(ContextKeys.ITEM, ModItems.LOCKED_CHEST_ITEM.get());
        context.set(ContextKeys.DOUBLE, List.of((double) num));

        // 转发
        ModExecutableEffects.GIVE_ITEM.get().apply(context);
        return true;
    }
}

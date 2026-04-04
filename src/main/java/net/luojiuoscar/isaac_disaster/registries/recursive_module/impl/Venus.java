package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.profile.PotionProfile;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.SimpleTrigger;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class Venus implements IRecursiveModule {
    private static final List<SimpleTrigger> triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModAbilityEffects.APPLY_EFFECT_TO_NEARBY)
    );

    @Override
    public List<SimpleTrigger> getTriggers() {
        return triggers;
    }

    @Override
    public void modifyContext(AbilityEffectContext context) {
        context.set(ContextKeys.ABILITY_EFFECT, ModAbilityEffects.POTION.get());
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER ,1);
        context.set(ContextKeys.POTIONS, List.of(
                new PotionProfile(
                        ModEffects.CHARM.get(),
                        Mth.clamp(amplifier * 60 + 120, 120, 240),
                        0
                )
        ));
        context.set(ContextKeys.BOOLEAN, List.of(true));
        context.set(ContextKeys.DOUBLE, List.of(0.75));
    }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 20;
    }
}

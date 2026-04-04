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
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class TheSoul implements IRecursiveModule {
    private static final List<SimpleTrigger> triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModAbilityEffects.POTION)
    );

    @Override
    public List<SimpleTrigger> getTriggers() {
        return triggers;
    }

    @Override
    public void modifyContext(AbilityEffectContext context) {
        context.set(ContextKeys.POTIONS, List.of(
                new PotionProfile(
                        ModEffects.SOUL_STATE.get(),
                        120,
                        0,
                        0,
                        0,
                        false
                )
        ));
    }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return entity.getRandom().nextInt(100, 220);
    }
}

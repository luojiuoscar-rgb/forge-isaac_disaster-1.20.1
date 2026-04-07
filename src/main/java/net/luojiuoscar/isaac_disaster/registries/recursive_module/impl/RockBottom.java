package net.luojiuoscar.isaac_disaster.registries.recursive_module.impl;

import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.RecursiveModuleQueue;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.List;

import static net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.RockBottom.*;

public class RockBottom implements IRecursiveModule {
    private static final CompositeTrigger triggers = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.EMTPY, ModAbilityEffects.ROCK_BOTTOM)
    ));

    @Override
    public CompositeTrigger getTriggers() {
        return triggers;
    }

    @Override
    public int getTickInterval(LivingEntity entity, int stacks, RecursiveModuleQueue queue) {
        return 20;
    }

    @Override
    public void handleRemove(LivingEntity entity) {
        entity.getCapability(EffectModulesProvider.EFFECT_MODULES).ifPresent(
                effectModules -> {
                    var recursiveModuleInstance = effectModules.getRecursiveModuleQueue().get(ModRecursiveModule.ROCK_BOTTOM.getId());

                    // 如果不存在或者为0
                    if (recursiveModuleInstance == null || recursiveModuleInstance.stacks == 0){

                        entity.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(
                                extraData -> {
                                    // 移除所有补正记录
                                    for (ResourceLocation id : ATTR_DATA_RESOURCE){
                                        extraData.removeDouble(id);
                                    }
                                }
                        );
                    }

                }
        );

        // 移除所有的Modifier
        if (entity instanceof ServerPlayer player){
            for (Attribute targetAttribute : TARGET_ATTRIBUTES) {
                StatManager.removeModifier(player, player.getAttribute(targetAttribute),
                        ROCK_BOTTOM_ATTR_MODIFIER_UUID
                );
            }
        }

    }
}

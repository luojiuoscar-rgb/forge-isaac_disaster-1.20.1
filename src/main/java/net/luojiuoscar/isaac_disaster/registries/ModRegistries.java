package net.luojiuoscar.isaac_disaster.registries;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.ModPickupAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.ModTrinketAbilities;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.ModAttackPatterns;
import net.luojiuoscar.isaac_disaster.registries.attack_type.combination.ModCombinationRules;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.luojiuoscar.isaac_disaster.registries.charge_bar.ModChargeBars;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModules;
import net.luojiuoscar.isaac_disaster.registries.revive_module.ModReviveModules;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.registries.trajectory.rule.ModTrajectoryRules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.rule.ModTriggerModuleRules;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;

public class ModRegistries {
    public static void register(IEventBus modEventBus) {
        IsaacDisaster.LOGGER.info("Initializing Registries...");

        registerRegistry(ModChargeBars.CHARGE_BAR_REGISTRY, ModChargeBars.CHARGE_BAR_KEY.location(), modEventBus);
        registerRegistry(ModTrajectoryModules.TRAJECTORY_MODULE_REGISTRY, ModTrajectoryModules.TRAJECTORY_MODULE_KEY.location(), modEventBus);
        registerRegistry(ModTrajectoryRules.TRAJECTORY_RULE_REGISTRY, ModTrajectoryRules.TRAJECTORY_RULE_KEY.location(), modEventBus);
        registerRegistry(ModBulletColors.BULLET_COLOR_REGISTRY, ModBulletColors.BULLET_COLOR_KEY.location(), modEventBus);
        registerRegistry(ModBulletVisuals.BULLET_VISUAL_REGISTRY, ModBulletVisuals.BULLET_VISUAL_KEY.location(), modEventBus);
        registerRegistry(ModTriggerModules.TRIGGER_MODULE_REGISTRY, ModTriggerModules.TRIGGER_MODULE_KEY.location(), modEventBus);
        registerRegistry(ModTriggerModuleRules.TRIGGER_MODULE_RULE_REGISTRY, ModTriggerModuleRules.TRIGGER_MODULE_RULE_KEY.location(), modEventBus);
        registerRegistry(ModVisualLayers.VISUAL_LAYER_REGISTRY, ModVisualLayers.VISUAL_LAYER_KEY.location(), modEventBus);
        registerRegistry(ModRecursiveModules.RECURSIVE_MODULE_REGISTRY, ModRecursiveModules.RECURSIVE_MODULE_KEY.location(), modEventBus);
        registerRegistry(ModReviveModules.REVIVE_MODULE_REGISTRY, ModRecursiveModules.RECURSIVE_MODULE_KEY.location(), modEventBus);
        registerRegistry(ModSplitModules.SPLIT_MODULE_REGISTRY, ModSplitModules.SPLIT_MODULE_KEY.location(), modEventBus);
        registerRegistry(ModPassiveAbilities.PASSIVE_ABILITY_REGISTRY, ModPassiveAbilities.PASSIVE_ABILITY_KEY.location(), modEventBus);
        registerRegistry(ModActiveAbilities.ACTIVE_ABILITY_REGISTRY, ModActiveAbilities.ACTIVE_ABILITY_KEY.location(), modEventBus);
        registerRegistry(ModTrinketAbilities.TRINKET_ABILITY_REGISTRY, ModTrinketAbilities.TRINKET_ABILITY_KEY.location(), modEventBus);
        registerRegistry(ModSetAbilities.SET_ABILITY_REGISTRY, ModSetAbilities.SET_ABILITY_KEY.location(), modEventBus);
        registerRegistry(ModPickupAbilities.PICKUP_ABILITY_REGISTRY, ModPickupAbilities.PICKUP_ABILITY_KEY.location(), modEventBus);
        registerRegistry(ModAttackTypes.ATTACK_TYPE_REGISTER, ModAttackTypes.ATTACK_TYPE_KEY.location(), modEventBus);
        registerRegistry(ModAttackPatterns.ATTACK_PATTERN_REGISTRY, ModAttackPatterns.ATTACK_PATTERN_KEY.location(), modEventBus);
        registerRegistry(ModCombinationRules.ATTACK_COMBINATION_RULE_REGISTRY, ModCombinationRules.ATTACK_COMBINATION_RULE_KEY.location(), modEventBus);
        registerRegistry(ModFamiliarEntities.FAMILIAR_ENTITY_REGISTRY, ModFamiliarEntities.FAMILIAR_ENTITY_KEY.location(), modEventBus);
        registerRegistry(ModExecutableEffects.EXECUTABLE_EFFECT_REGISTRY, ModExecutableEffects.EXECUTABLE_EFFECT_KEY.location(), modEventBus);
    }

    private static <T> void registerRegistry(DeferredRegister<T> registry, ResourceLocation name,
                                            IEventBus modEventBus) {
        registry.makeRegistry(() -> new RegistryBuilder<T>().setName(name));
        registry.register(modEventBus);
    }
}

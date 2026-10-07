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

        registerRegistry(ModChargeBars.CHARGE_BAR_REGISTRY,
                ModChargeBars.CHARGE_BAR_KEY.location(), modEventBus);

        registerRegistry(ModTrajectoryModules.TRAJECTORY_MODULE_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trajectory_module"), modEventBus);

        registerRegistry(ModTrajectoryRules.TRAJECTORY_RULE_REGISTRY, ModTrajectoryRules.TRAJECTORY_RULE_KEY.location(), modEventBus);

        registerRegistry(ModBulletColors.BULLET_COLOR_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "bullet_color"), modEventBus);

        registerRegistry(ModBulletVisuals.BULLET_VISUAL_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "bullet_visual"), modEventBus);

        registerRegistry(ModTriggerModules.TRIGGER_MODULE_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trigger_module"), modEventBus);

        registerRegistry(ModTriggerModuleRules.TRIGGER_MODULE_RULE_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trigger_module_rule"), modEventBus);

        registerRegistry(ModVisualLayers.VISUAL_LAYER_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "visual_layer"), modEventBus);

        registerRegistry(ModRecursiveModules.RECURSIVE_MODULE_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "recursive_module"), modEventBus);

        registerRegistry(ModReviveModules.REVIVE_MODULE_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "revive_module"), modEventBus);

        registerRegistry(ModSplitModules.SPLIT_MODULE_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "split_module"), modEventBus);

        registerRegistry(ModPassiveAbilities.PASSIVE_ABILITY_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "passive_ability"), modEventBus);

        registerRegistry(ModActiveAbilities.ACTIVE_ABILITY_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "active_ability"), modEventBus);

        registerRegistry(ModTrinketAbilities.TRINKET_ABILITY_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trinket_ability"), modEventBus);

        registerRegistry(ModSetAbilities.SET_ABILITY_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "set_ability"), modEventBus);

        registerRegistry(ModPickupAbilities.PICKUP_ABILITY_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickup_ability"), modEventBus);

        registerRegistry(ModAttackTypes.ATTACK_TYPE_REGISTER, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_type"), modEventBus);

        registerRegistry(ModAttackPatterns.ATTACK_PATTERN_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_pattern"), modEventBus);

        registerRegistry(ModCombinationRules.ATTACK_COMBINATION_RULE_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_combination_rule"), modEventBus);

        registerRegistry(ModFamiliarEntities.FAMILIAR_ENTITY_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "familiar_entity"), modEventBus);

        registerRegistry(ModExecutableEffects.EXECUTABLE_EFFECT_REGISTRY, ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "executable_effect"), modEventBus);
    }

    private static <T> void registerRegistry(DeferredRegister<T> registry, ResourceLocation name,
                                            IEventBus modEventBus) {
        registry.makeRegistry(() -> new RegistryBuilder<T>().setName(name));
        registry.register(modEventBus);
    }
}

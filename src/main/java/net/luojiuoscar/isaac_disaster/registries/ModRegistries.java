package net.luojiuoscar.isaac_disaster.registries;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.ability.active.ActiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.pickup.PickupAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.trinket.TrinketAbility;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IExecutableEffect;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.combination.AttackCombinationRule;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.pill_effect.IPillEffect;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.IAttackTrajectory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryBuilder;

import static net.luojiuoscar.isaac_disaster.registries.ability.active.ModActiveAbility.ACTIVE_ABILITY_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbility.PASSIVE_ABILITY_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.ability.pickup.ModPickupAbility.PICKUP_ABILITY_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility.SET_ABILITY_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.ability.trinket.ModTrinketAbility.TRINKET_ABILITY_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.ability_effect.ModAbilityEffects.EXECUTABLE_EFFECT_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType.ATTACK_TYPE_REGISTER;
import static net.luojiuoscar.isaac_disaster.registries.attack_type.combination.ModCombinationRules.ATTACK_COMBINATION_RULE_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor.BULLET_COLOR_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.pill_effect.ModPillEffect.PILL_EFFECT_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModule.RECURSIVE_MODULE_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.trajectory.ModAttackTrajectory.ATTACK_TRAJECTORY_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule.TRIGGER_MODULE_REGISTRY;


public class ModRegistries {
    public static void register(IEventBus modEventBus) {
        IsaacDisaster.LOGGER.info("Initializing Registries...");

        ATTACK_TRAJECTORY_REGISTRY.makeRegistry(() -> {return new RegistryBuilder<IAttackTrajectory>()
                    .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_trajectory"));});
        ATTACK_TRAJECTORY_REGISTRY.register(modEventBus);

        BULLET_COLOR_REGISTRY.makeRegistry(() -> {return new RegistryBuilder<BulletColor>()
                .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "bullet_color"));});
        BULLET_COLOR_REGISTRY.register(modEventBus);

        TRIGGER_MODULE_REGISTRY.makeRegistry(() -> {return new RegistryBuilder<ITriggerModule>()
                .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trigger_module"));});
        TRIGGER_MODULE_REGISTRY.register(modEventBus);

        RECURSIVE_MODULE_REGISTRY.makeRegistry(() -> {return new RegistryBuilder<IRecursiveModule>()
                .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "recursive_module"));});
        RECURSIVE_MODULE_REGISTRY.register(modEventBus);

        PASSIVE_ABILITY_REGISTRY.makeRegistry(() -> {return new RegistryBuilder<PassiveAbility>()
                .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "passive_ability"));});
        PASSIVE_ABILITY_REGISTRY.register(modEventBus);

        ACTIVE_ABILITY_REGISTRY.makeRegistry(() -> {return new RegistryBuilder<ActiveAbility>()
                .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "active_ability"));});
        ACTIVE_ABILITY_REGISTRY.register(modEventBus);

        PILL_EFFECT_REGISTRY.makeRegistry(() -> {return new RegistryBuilder<IPillEffect>()
                .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pill_effect"));});
        PILL_EFFECT_REGISTRY.register(modEventBus);

        TRINKET_ABILITY_REGISTRY.makeRegistry(() -> {return new RegistryBuilder<TrinketAbility>()
                .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trinket_ability"));});
        TRINKET_ABILITY_REGISTRY.register(modEventBus);

        SET_ABILITY_REGISTRY.makeRegistry(() -> {return new RegistryBuilder<SetAbility>()
                .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "set_ability"));});
        SET_ABILITY_REGISTRY.register(modEventBus);

        PICKUP_ABILITY_REGISTRY.makeRegistry(() -> {return new RegistryBuilder<PickupAbility>()
                .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "pickup_ability"));});
        PICKUP_ABILITY_REGISTRY.register(modEventBus);

        ATTACK_TYPE_REGISTER.makeRegistry(() -> {return new RegistryBuilder<AttackType>()
                .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_type"));});
        ATTACK_TYPE_REGISTER.register(modEventBus);

        ATTACK_COMBINATION_RULE_REGISTRY.makeRegistry(() -> {return new RegistryBuilder<AttackCombinationRule>()
                .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_combination_rule"));});
        ATTACK_COMBINATION_RULE_REGISTRY.register(modEventBus);

        EXECUTABLE_EFFECT_REGISTRY.makeRegistry(() -> {return new RegistryBuilder<IExecutableEffect>()
                .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "executable_effect"));});
        EXECUTABLE_EFFECT_REGISTRY.register(modEventBus);
    }
}

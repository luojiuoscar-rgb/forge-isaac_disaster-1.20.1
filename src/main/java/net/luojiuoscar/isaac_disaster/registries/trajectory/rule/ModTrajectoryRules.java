package net.luojiuoscar.isaac_disaster.registries.trajectory.rule;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;


public final class ModTrajectoryRules {
    private ModTrajectoryRules() {}

    public static final ResourceKey<Registry<TrajectoryRule>> TRAJECTORY_RULE_KEY = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trajectory_rule"));

    public static final DeferredRegister<TrajectoryRule> TRAJECTORY_RULE_REGISTRY =
            DeferredRegister.create(TRAJECTORY_RULE_KEY, IsaacDisaster.MOD_ID);

    /** Tiny Planet owns ordinary bullets when both orbit and reflection are attached. */
    public static final RegistryObject<TrajectoryRule>
            TINY_PLANET_OVERRIDES_REFLECTION =
            TRAJECTORY_RULE_REGISTRY.register(
                    "tiny_planet_overrides_reflection",
                    () -> TrajectoryRule.replace(
                            Set.of(
                                    ModTrajectoryModules.TINY_PLANET_BULLET.getId(),
                                    ModTrajectoryModules.MY_REFLECTION_BULLET.getId()),
                            ModTrajectoryModules.TINY_PLANET_BULLET.getId(),
                            ModAttackTypes.BULLET.getId(),
                            100));

    public static final RegistryObject<TrajectoryRule>
            TINY_PLANET_OVERRIDES_GRAVITY =
            TRAJECTORY_RULE_REGISTRY.register(
                    "tiny_planet_overrides_gravity",
                    () -> TrajectoryRule.replace(
                            Set.of(
                                    ModTrajectoryModules.TINY_PLANET_BULLET.getId(),
                                    ModTrajectoryModules.GRAVITY.getId()),
                            ModTrajectoryModules.TINY_PLANET_BULLET.getId(),
                            ModAttackTypes.BULLET.getId(),
                            100));

}

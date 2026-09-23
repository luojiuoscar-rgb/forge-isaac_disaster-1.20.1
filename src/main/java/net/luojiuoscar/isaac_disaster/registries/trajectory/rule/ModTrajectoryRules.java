package net.luojiuoscar.isaac_disaster.registries.trajectory.rule;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;


public final class ModTrajectoryRules {
    private ModTrajectoryRules() {}

    public static final ResourceKey<Registry<TrajectoryRule>> TRAJECTORY_RULE_KEY = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trajectory_rule"));

    public static final DeferredRegister<TrajectoryRule> TRAJECTORY_RULE_REGISTRY =
            DeferredRegister.create(TRAJECTORY_RULE_KEY, IsaacDisaster.MOD_ID);

}

package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModAttackTrajectory {

    public static final ResourceKey<Registry<IAttackTrajectory>> ATTACK_TRAJECTORY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_trajectory"));

    public static final DeferredRegister<IAttackTrajectory> ATTACK_TRAJECTORY_REGISTRY =
            DeferredRegister.create(ATTACK_TRAJECTORY_KEY, IsaacDisaster.MOD_ID);

    // Built-in entries
    public static final RegistryObject<IAttackTrajectory> WIGGLE_WORM =
            ATTACK_TRAJECTORY_REGISTRY.register("wiggle_worm", () -> BuiltinTrajectory.WIGGLE_WORM);
    public static final RegistryObject<IAttackTrajectory> TINY_PLANET =
            ATTACK_TRAJECTORY_REGISTRY.register("tiny_planet", () -> BuiltinTrajectory.TINY_PLANET);
    public static final RegistryObject<IAttackTrajectory> RING_WORM =
            ATTACK_TRAJECTORY_REGISTRY.register("ring_worm", () -> BuiltinTrajectory.RING_WORM);
    public static final RegistryObject<IAttackTrajectory> OUROBOROS_WORM =
            ATTACK_TRAJECTORY_REGISTRY.register("ouroboros_worm", () -> BuiltinTrajectory.OUROBOROS_WORM);
    public static final RegistryObject<IAttackTrajectory> HOOK_WORM =
            ATTACK_TRAJECTORY_REGISTRY.register("hook_worm", () -> BuiltinTrajectory.HOOK_WORM);
    public static final RegistryObject<IAttackTrajectory> MY_REFLECTION =
            ATTACK_TRAJECTORY_REGISTRY.register("my_reflection", () -> BuiltinTrajectory.MY_REFLECTION);
    public static final RegistryObject<IAttackTrajectory> GRAVITY =
            ATTACK_TRAJECTORY_REGISTRY.register("gravity", () -> BuiltinTrajectory.GRAVITY);
}

package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModTrajectoryModules {
    private static final Map<ResourceLocation, TrajectoryModule<?>> BUILTIN_CODECS =
            new HashMap<>();

    public static final ResourceKey<Registry<TrajectoryModule<?>>> TRAJECTORY_MODULE_KEY =
            ResourceKey.createRegistryKey(
                    ResourceLocation.fromNamespaceAndPath(
                            IsaacDisaster.MOD_ID, "trajectory_module"));

    public static final DeferredRegister<TrajectoryModule<?>> TRAJECTORY_MODULE_REGISTRY =
            DeferredRegister.create(TRAJECTORY_MODULE_KEY, IsaacDisaster.MOD_ID);

    private static RegistryObject<TrajectoryModule<?>> registerBuiltin(
            String path, Supplier<? extends TrajectoryModule<?>> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, path);
        BUILTIN_CODECS.put(id, factory.get());
        return TRAJECTORY_MODULE_REGISTRY.register(path, factory::get);
    }

    /**
     * Forge registry is authoritative; the built-in definitions also support unit tests without
     * FML.
     */
    public static TrajectoryModule<?> resolve(ResourceLocation id) {
        IForgeRegistry<TrajectoryModule<?>> registry =
                RegistryManager.ACTIVE.getRegistry(TRAJECTORY_MODULE_KEY);
        if (registry != null) return registry.getValue(id);
        return BUILTIN_CODECS.get(id);
    }

    // Built-in entries
    public static final RegistryObject<TrajectoryModule<?>> WIGGLE_WORM =
            registerBuiltin("wiggle_worm", WiggleWormTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule<?>> TINY_PLANET_BULLET =
            registerBuiltin("tiny_planet_bullet", TinyPlanetBulletTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule<?>> TINY_PLANET_LASER =
            registerBuiltin("tiny_planet_laser", TinyPlanetLaserTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule<?>> RING_WORM =
            registerBuiltin("ring_worm", RingWormTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule<?>> OUROBOROS_WORM =
            registerBuiltin("ouroboros_worm", OuroborosWormTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule<?>> HOOK_WORM =
            registerBuiltin("hook_worm", HookWormTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule<?>> MY_REFLECTION_BULLET =
            registerBuiltin("my_reflection_bullet", MyReflectionBulletTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule<?>> MY_REFLECTION_LASER =
            registerBuiltin("my_reflection_laser", MyReflectionLaserTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule<?>> GRAVITY =
            registerBuiltin("gravity", GravityTrajectoryModule::new);
}

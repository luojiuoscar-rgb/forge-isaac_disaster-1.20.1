package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModTrajectoryModules {

    public static final ResourceKey<Registry<TrajectoryModule>> TRAJECTORY_MODULE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trajectory_module"));

    public static final DeferredRegister<TrajectoryModule> TRAJECTORY_MODULE_REGISTRY =
            DeferredRegister.create(TRAJECTORY_MODULE_KEY, IsaacDisaster.MOD_ID);

    // Built-in entries
    public static final RegistryObject<TrajectoryModule> WIGGLE_WORM =
            TRAJECTORY_MODULE_REGISTRY.register("wiggle_worm", WiggleWormTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule> TINY_PLANET_BULLET =
            TRAJECTORY_MODULE_REGISTRY.register("tiny_planet_bullet", TinyPlanetBulletTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule> TINY_PLANET_LASER =
            TRAJECTORY_MODULE_REGISTRY.register("tiny_planet_laser", TinyPlanetLaserTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule> RING_WORM =
            TRAJECTORY_MODULE_REGISTRY.register("ring_worm", RingWormTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule> OUROBOROS_WORM =
            TRAJECTORY_MODULE_REGISTRY.register("ouroboros_worm", OuroborosWormTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule> HOOK_WORM =
            TRAJECTORY_MODULE_REGISTRY.register("hook_worm", HookWormTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule> MY_REFLECTION_BULLET =
            TRAJECTORY_MODULE_REGISTRY.register("my_reflection_bullet", MyReflectionBulletTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule> MY_REFLECTION_LASER =
            TRAJECTORY_MODULE_REGISTRY.register("my_reflection_laser", MyReflectionLaserTrajectoryModule::new);
    public static final RegistryObject<TrajectoryModule> GRAVITY =
            TRAJECTORY_MODULE_REGISTRY.register("gravity", GravityTrajectoryModule::new);
}

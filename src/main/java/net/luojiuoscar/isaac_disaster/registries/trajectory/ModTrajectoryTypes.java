package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.ModRegistries;
import net.luojiuoscar.isaac_disaster.registries.content.trajectory.BuiltinTrajectories;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModTrajectoryTypes {
    private ModTrajectoryTypes() {}

    public static final DeferredRegister<AttackTrajectory> TRAJECTORIES =
            DeferredRegister.create(ModRegistries.ATTACK_TRAJECTORY_KEY, IsaacDisaster.MOD_ID);

    // Built-in entries
    public static final RegistryObject<AttackTrajectory> WIGGLE_WORM =
            TRAJECTORIES.register("wiggle_worm", () -> BuiltinTrajectories.WIGGLE_WORM);
    public static final RegistryObject<AttackTrajectory> TINY_PLANET =
            TRAJECTORIES.register("tiny_planet", () -> BuiltinTrajectories.TINY_PLANET);
    public static final RegistryObject<AttackTrajectory> RING_WORM =
            TRAJECTORIES.register("ring_worm", () -> BuiltinTrajectories.RING_WORM);
    public static final RegistryObject<AttackTrajectory> OUROBOROS_WORM =
            TRAJECTORIES.register("ouroboros_worm", () -> BuiltinTrajectories.OUROBOROS_WORM);
    public static final RegistryObject<AttackTrajectory> HOOK_WORM =
            TRAJECTORIES.register("hook_worm", () -> BuiltinTrajectories.HOOK_WORM);
    public static final RegistryObject<AttackTrajectory> MY_REFLECTION =
            TRAJECTORIES.register("my_reflection", () -> BuiltinTrajectories.MY_REFLECTION);

    public static void register(IEventBus modEventBus) {
        TRAJECTORIES.register(modEventBus);
    }
}

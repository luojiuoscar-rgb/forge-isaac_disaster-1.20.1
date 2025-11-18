package net.luojiuoscar.isaac_disaster.registries;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.trajectory.AttackTrajectory;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;

public class ModRegistries {

    // ---------------- ResourceKey ----------------
    public static final ResourceKey<Registry<AttackTrajectory>> ATTACK_TRAJECTORY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_trajectory"));

    // ---------------- DeferredRegister ----------------
    public static final DeferredRegister<AttackTrajectory> ATTACK_TRAJECTORIES =
            DeferredRegister.create(ATTACK_TRAJECTORY_KEY, IsaacDisaster.MOD_ID);

    // ---------------- 注册方法 ----------------
    public static void register(IEventBus modEventBus) {
        IsaacDisaster.LOGGER.info("Initializing AttackManager...");

        ATTACK_TRAJECTORIES.makeRegistry(() -> {
            return new RegistryBuilder<AttackTrajectory>()
                    .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_trajectory"));
        });

        ATTACK_TRAJECTORIES.register(modEventBus);
    }
}

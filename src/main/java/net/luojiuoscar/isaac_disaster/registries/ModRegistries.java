package net.luojiuoscar.isaac_disaster.registries;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.trajectory.AttackTrajectory;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryBuilder;

import static net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors.BULLET_COLOR_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.trajectory.ModAttackTrajectories.ATTACK_TRAJECTORY_REGISTRY;


public class ModRegistries {
    public static void register(IEventBus modEventBus) {
        IsaacDisaster.LOGGER.info("Initializing Registries...");

        ATTACK_TRAJECTORY_REGISTRY.makeRegistry(() -> {
            return new RegistryBuilder<AttackTrajectory>()
                    .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_trajectory"));
        });
        ATTACK_TRAJECTORY_REGISTRY.register(modEventBus);


        BULLET_COLOR_REGISTRY.makeRegistry(() -> {
            return new RegistryBuilder<BulletColor>()
                    .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "bullet_color"));
        });
        BULLET_COLOR_REGISTRY.register(modEventBus);
    }
}

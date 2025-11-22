package net.luojiuoscar.isaac_disaster.registries;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.IRecursiveModule;
import net.luojiuoscar.isaac_disaster.registries.trajectory.IAttackTrajectory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryBuilder;

import static net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors.BULLET_COLOR_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModule.RECURSIVE_MODULE_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.trajectory.ModAttackTrajectories.ATTACK_TRAJECTORY_REGISTRY;
import static net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule.TRIGGER_MODULE_REGISTRY;


public class ModRegistries {
    public static void register(IEventBus modEventBus) {
        IsaacDisaster.LOGGER.info("Initializing Registries...");

        ATTACK_TRAJECTORY_REGISTRY.makeRegistry(() -> {
            return new RegistryBuilder<IAttackTrajectory>()
                    .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "attack_trajectory"));
        });
        ATTACK_TRAJECTORY_REGISTRY.register(modEventBus);


        BULLET_COLOR_REGISTRY.makeRegistry(() -> {
            return new RegistryBuilder<BulletColor>()
                    .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "bullet_color"));
        });
        BULLET_COLOR_REGISTRY.register(modEventBus);

        TRIGGER_MODULE_REGISTRY.makeRegistry(() -> {
            return new RegistryBuilder<ITriggerModule>()
                    .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trigger_module"));
        });
        TRIGGER_MODULE_REGISTRY.register(modEventBus);

        RECURSIVE_MODULE_REGISTRY.makeRegistry(() -> {
            return new RegistryBuilder<IRecursiveModule>()
                    .setName(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "recursive_module"));
        });
        RECURSIVE_MODULE_REGISTRY.register(modEventBus);
    }
}

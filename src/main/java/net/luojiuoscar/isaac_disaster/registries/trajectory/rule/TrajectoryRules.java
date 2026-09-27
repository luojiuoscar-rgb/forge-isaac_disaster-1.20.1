package net.luojiuoscar.isaac_disaster.registries.trajectory.rule;

import java.util.*;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public final class TrajectoryRules {
    private static volatile TrajectoryRuleResolver resolver = new TrajectoryRuleResolver(Map.of());

    private TrajectoryRules() {
    }

    /** Called after all mods have registered; never scan the registry for each projectile. */
    public static void rebuildCache() {
        IForgeRegistry<TrajectoryRule> registry =
            RegistryManager.ACTIVE.getRegistry(ModTrajectoryRules.TRAJECTORY_RULE_KEY.location());
        if (registry == null) throw new IllegalStateException("trajectory rule registry missing");
        Map<ResourceLocation, TrajectoryRule> entries = new HashMap<>();
        for (TrajectoryRule rule : registry.getValues())
            entries.put(Objects.requireNonNull(registry.getKey(rule)), rule);
        resolver = new TrajectoryRuleResolver(entries);
    }

    public static List<TrajectorySpec> resolve(ResourceLocation type, List<TrajectorySpec> specs) {
        return resolver.resolve(type, specs);
    }
}

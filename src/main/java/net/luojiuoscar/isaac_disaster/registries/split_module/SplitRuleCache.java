package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable runtime index for rules targeting registered split modules. */
public final class SplitRuleCache {
    private static volatile Map<ResourceLocation, List<SplitRule>> rulesByModule = Map.of();

    private SplitRuleCache() {
    }

    /** Rebuilds the module-to-rule cache after Forge registries are ready. */
    public static void rebuildCache() {
        IForgeRegistry<SplitRule> registry = RegistryManager.ACTIVE.getRegistry(ModSplitRules.SPLIT_RULE_KEY);
        if (registry == null) {
            rulesByModule = Map.of();
            return;
        }

        Map<ResourceLocation, List<SplitRule>> mutableIndex = new HashMap<>();
        for (SplitRule rule : registry.getValues()) {
            for (ResourceLocation moduleId : rule.getTargetModules()) {
                mutableIndex.computeIfAbsent(moduleId, ignored -> new ArrayList<>()).add(rule);
            }
        }

        Map<ResourceLocation, List<SplitRule>> immutableIndex = new HashMap<>();
        mutableIndex.forEach((moduleId, rules) -> immutableIndex.put(moduleId, List.copyOf(rules)));
        rulesByModule = Map.copyOf(immutableIndex);
    }

    /** Returns false when any cached rule targeting the module rejects this event. */
    public static boolean allows(@NotNull BulletSplitEvent event, @NotNull ResourceLocation moduleId,
                                 @NotNull SplitModule module) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(moduleId, "moduleId");
        Objects.requireNonNull(module, "module");

        List<SplitRule> rules = rulesByModule.get(moduleId);
        if (rules == null) return true;
        for (SplitRule rule : rules) {
            if (!rule.allows(event, module)) return false;
        }
        return true;
    }
}

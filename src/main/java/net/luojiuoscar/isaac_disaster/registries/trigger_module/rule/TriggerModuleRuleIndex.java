package net.luojiuoscar.isaac_disaster.registries.trigger_module.rule;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TriggerModuleRuleIndex {
    private static volatile Map<ResourceLocation, RuleBucket> rulesByModule = Map.of();

    private TriggerModuleRuleIndex() {
    }

    public static void rebuild() {
        IForgeRegistry<TriggerModuleRule> registry =
                RegistryManager.ACTIVE.getRegistry(ModTriggerModuleRules.TRIGGER_MODULE_RULE_KEY);
        if (registry == null) {
            rulesByModule = Map.of();
            return;
        }

        Map<ResourceLocation, MutableRuleBucket> mutableIndex = new HashMap<>();
        for (TriggerModuleRule rule : registry.getValues()) {
            for (ResourceLocation moduleId : rule.getTargetModules()) {
                MutableRuleBucket bucket = mutableIndex.computeIfAbsent(moduleId, ignored -> new MutableRuleBucket());
                if (rule.getTriggerTypes().isEmpty()) {
                    bucket.anyTrigger.add(rule);
                } else {
                    for (TriggerType triggerType : rule.getTriggerTypes()) {
                        bucket.byTrigger.computeIfAbsent(triggerType.getId(), ignored -> new ArrayList<>()).add(rule);
                    }
                }
            }
        }

        Map<ResourceLocation, RuleBucket> immutableIndex = new HashMap<>();
        mutableIndex.forEach((moduleId, bucket) -> immutableIndex.put(moduleId, bucket.freeze()));
        rulesByModule = Map.copyOf(immutableIndex);
    }

    public static boolean allows(TriggerModuleRuleContext context) {
        RuleBucket bucket = rulesByModule.get(context.getCandidateModuleId());
        if (bucket == null) return true;
        return allows(bucket.anyTrigger, context)
                && allows(bucket.byTrigger.get(context.getTriggerType().getId()), context);
    }

    static boolean allows(List<TriggerModuleRule> rules, TriggerModuleRuleContext context) {
        if (rules == null) return true;
        for (TriggerModuleRule rule : rules) {
            if (!rule.allows(context)) {
                return false;
            }
        }
        return true;
    }

    private record RuleBucket(List<TriggerModuleRule> anyTrigger,
                              Map<ResourceLocation, List<TriggerModuleRule>> byTrigger) {
    }

    private static final class MutableRuleBucket {
        private final List<TriggerModuleRule> anyTrigger = new ArrayList<>();
        private final Map<ResourceLocation, List<TriggerModuleRule>> byTrigger = new HashMap<>();

        private RuleBucket freeze() {
            Map<ResourceLocation, List<TriggerModuleRule>> typedRules = new HashMap<>();
            byTrigger.forEach((type, rules) -> typedRules.put(type, List.copyOf(rules)));
            return new RuleBucket(List.copyOf(anyTrigger), Map.copyOf(typedRules));
        }
    }
}

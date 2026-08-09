package net.luojiuoscar.isaac_disaster.registries.trigger_module.rule;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * A registered gate evaluated before a target trigger module executes.
 */
public abstract class TriggerModuleRule {
    private final Set<ResourceLocation> targetModules;
    private final Set<TriggerType> triggerTypes;

    protected TriggerModuleRule(Set<ResourceLocation> targetModules, Set<TriggerType> triggerTypes) {
        if (targetModules.isEmpty()) {
            throw new IllegalArgumentException("A trigger module rule requires at least one target module");
        }
        this.targetModules = Set.copyOf(targetModules);
        this.triggerTypes = Set.copyOf(triggerTypes);
    }

    public Set<ResourceLocation> getTargetModules() {
        return targetModules;
    }

    public Set<TriggerType> getTriggerTypes() {
        return triggerTypes;
    }

    /**
     * Returns whether this rule targets the module and trigger type.
     * An empty trigger type set applies to every trigger type.
     */
    public boolean appliesTo(ResourceLocation moduleId, TriggerType triggerType) {
        return targetModules.contains(moduleId)
                && (triggerTypes.isEmpty()
                || triggerTypes.stream().anyMatch(type -> type.getId().equals(triggerType.getId())));
    }

    /** Returns whether the candidate module may execute for this trigger. */
    public abstract boolean allows(TriggerModuleRuleContext context);
}

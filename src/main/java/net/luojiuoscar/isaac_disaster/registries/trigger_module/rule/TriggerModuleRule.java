package net.luojiuoscar.isaac_disaster.registries.trigger_module.rule;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

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

    public boolean appliesTo(ResourceLocation moduleId, TriggerType triggerType) {
        return targetModules.contains(moduleId)
                && (triggerTypes.isEmpty()
                || triggerTypes.stream().anyMatch(type -> type.getId().equals(triggerType.getId())));
    }

    public abstract boolean allows(TriggerModuleRuleContext context);
}

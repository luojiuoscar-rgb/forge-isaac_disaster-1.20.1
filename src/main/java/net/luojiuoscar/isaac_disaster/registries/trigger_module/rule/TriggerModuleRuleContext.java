package net.luojiuoscar.isaac_disaster.registries.trigger_module.rule;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKey;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleInstance;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TriggerModuleRuleContext {
    private final TriggerModuleInstance candidate;
    private final TriggerType triggerType;
    private final ExecutableEffectContext effectContext;
    private final TriggerModuleSnapshot activeModules;

    public TriggerModuleRuleContext(TriggerModuleInstance candidate, TriggerType triggerType,
                                    ExecutableEffectContext effectContext, TriggerModuleSnapshot activeModules) {
        this.candidate = candidate;
        this.triggerType = triggerType;
        this.effectContext = effectContext;
        this.activeModules = activeModules;
    }

    public ResourceLocation getCandidateModuleId() {
        return candidate.id();
    }

    public int getCandidateStacks() {
        return candidate.stacks();
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public LivingEntity getEntity() {
        return effectContext.getEntity();
    }

    public <T> T get(ContextKey<T> key) {
        return readOnlyValue(effectContext.get(key));
    }

    public <T> T getOrDefault(ContextKey<T> key, T defaultValue) {
        return readOnlyValue(effectContext.getOrDefault(key, defaultValue));
    }

    public boolean has(ContextKey<?> key) {
        return effectContext.has(key);
    }

    public TriggerModuleSnapshot getActiveModules() {
        return activeModules;
    }

    @SuppressWarnings("unchecked")
    private static <T> T readOnlyValue(T value) {
        if (value instanceof List<?> list) return (T) List.copyOf(list);
        if (value instanceof Set<?> set) return (T) Set.copyOf(set);
        if (value instanceof Map<?, ?> map) return (T) Map.copyOf(map);
        return value;
    }
}

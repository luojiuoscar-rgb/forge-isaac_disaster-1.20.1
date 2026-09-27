package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import java.util.Map;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.resources.ResourceLocation;

/** Attaches a module for a concrete/root attack type, or a universal default module. */
public final class TrajectoryAttachment extends TriggerModule {
    private final Map<ResourceLocation, ResourceLocation> modulesByType;
    private final ResourceLocation defaultModuleId;

    public TrajectoryAttachment(ResourceLocation defaultModuleId) {
        super(CompositeTrigger.EMPTY);
        this.modulesByType = Map.of();
        this.defaultModuleId = defaultModuleId;
    }

    public TrajectoryAttachment(
        ResourceLocation defaultModuleId, Map<ResourceLocation, ResourceLocation> modulesByType) {
        super(CompositeTrigger.EMPTY);
        this.modulesByType = Map.copyOf(modulesByType);
        this.defaultModuleId = defaultModuleId;
    }

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        ResourceLocation current = attackContext.getTypeId();
        ResourceLocation root = attackContext.getRootTypeId();
        ResourceLocation selected = current == null ? null : modulesByType.get(current);
        if (selected == null && root != null) {
            selected = modulesByType.get(root);
        }
        if (selected == null) {
            selected = defaultModuleId;
        }
        if (selected != null) {
            attackContext.addTrajectoryModule(
                selected, context.getOrDefault(ContextKeys.AMPLIFIER, 1.0D).intValue());
        }
    }
}

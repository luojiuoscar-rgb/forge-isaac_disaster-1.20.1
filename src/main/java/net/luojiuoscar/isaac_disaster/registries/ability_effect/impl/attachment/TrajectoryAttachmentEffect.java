package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import java.util.Map;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.resources.ResourceLocation;

public final class TrajectoryAttachmentEffect extends AttackPreparationEffect {
    private final Map<ResourceLocation, ResourceLocation> modulesByType;
    private final ResourceLocation defaultModuleId;

    public TrajectoryAttachmentEffect(ResourceLocation defaultModuleId,
                                      Map<ResourceLocation, ResourceLocation> modulesByType) {
        this.modulesByType = Map.copyOf(modulesByType);
        this.defaultModuleId = defaultModuleId;
    }

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
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

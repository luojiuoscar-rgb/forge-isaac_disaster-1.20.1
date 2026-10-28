package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import java.util.List;
import java.util.Map;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment.TrajectoryAttachmentEffect;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.resources.ResourceLocation;

/** Attaches a trajectory for a concrete/root attack type, or a universal default. */
public final class TrajectoryAttachment extends TriggerModule {
    public TrajectoryAttachment(ResourceLocation defaultModuleId) {
        this(defaultModuleId, Map.of());
    }

    public TrajectoryAttachment(ResourceLocation defaultModuleId,
                                Map<ResourceLocation, ResourceLocation> modulesByType) {
        this(new TrajectoryAttachmentEffect(defaultModuleId, modulesByType));
    }

    private TrajectoryAttachment(TrajectoryAttachmentEffect effect) {
        super(new CompositeTrigger(List.of(new SimpleTrigger(ModTriggerTypes.EFFECT_ATTACHMENT_PREPARE,
                () -> effect))));
    }
}

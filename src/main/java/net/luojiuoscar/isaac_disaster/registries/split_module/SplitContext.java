package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/** Complete runtime data supplied to one SplitModule evaluation. */
public final class SplitContext {
    private final BulletSplitEvent event;
    private final IBulletObject parent;
    private final AttackContext referenceContext;
    private final SplitSequence sequence;
    private final SplitTriggerType triggerType;
    private final SplitTriggerCounts triggerCounts;
    private final SplitModule module;
    private final ResourceLocation moduleId;
    private final int stacks;
    private final int moduleTriggerCount;

    public SplitContext(@NotNull BulletSplitEvent event, @NotNull ResourceLocation moduleId,
                        @NotNull SplitModule module,
                        int stacks, int moduleTriggerCount) {
        this.event = Objects.requireNonNull(event, "event");
        this.parent = event.getParent();
        this.referenceContext = event.getReferenceContext();
        this.sequence = event.getSplitSequence();
        this.triggerType = event.getTriggerType();
        this.triggerCounts = parent.getSplitTriggerCounts().copy();
        this.module = Objects.requireNonNull(module, "module");
        this.moduleId = Objects.requireNonNull(moduleId, "moduleId");
        this.stacks = stacks;
        this.moduleTriggerCount = moduleTriggerCount;
    }

    public @NotNull BulletSplitEvent getEvent() { return event; }
    public @NotNull IBulletObject getParent() { return parent; }
    public @NotNull AttackContext getReferenceContext() { return referenceContext.copy(); }
    public @NotNull SplitSequence getSequence() { return sequence.copy(); }
    public @NotNull SplitTriggerType getTriggerType() { return triggerType; }
    public @NotNull SplitTriggerCounts getTriggerCounts() { return triggerCounts.copy(); }
    public @NotNull SplitModule getModule() { return module; }
    public @NotNull ResourceLocation getModuleId() { return moduleId; }
    public int getStacks() { return stacks; }
    public int getModuleTriggerCount() { return moduleTriggerCount; }
}

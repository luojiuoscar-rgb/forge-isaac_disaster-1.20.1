package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/** Cancelable event boundary for resolving one parent-bullet split. */
@Cancelable
public final class BulletSplitEvent extends Event {
    private final IBulletObject parent;
    private final SplitSequence splitSequence;
    private final AttackContext referenceContext;
    private final SplitTriggerType triggerType;

    public BulletSplitEvent(@NotNull IBulletObject parent, @NotNull SplitSequence splitSequence,
                            @NotNull AttackContext referenceContext,
                            @NotNull SplitTriggerType triggerType) {
        this.parent = Objects.requireNonNull(parent, "parent");
        this.splitSequence = Objects.requireNonNull(splitSequence, "splitSequence").copy();
        this.referenceContext = Objects.requireNonNull(referenceContext, "referenceContext").copy();
        this.triggerType = Objects.requireNonNull(triggerType, "triggerType");
    }

    public IBulletObject getParent() { return parent; }
    public SplitSequence getSplitSequence() { return splitSequence.copy(); }
    public AttackContext getReferenceContext() { return referenceContext.copy(); }
    public SplitTriggerType getTriggerType() { return triggerType; }
}

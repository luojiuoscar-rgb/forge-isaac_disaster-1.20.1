package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/** Cancelable event boundary for resolving one parent-bullet split. */
@Cancelable
public final class BulletSplitEvent extends Event {
    private final TearBullet parent;
    private final SplitSequence splitSequence;
    private final AttackContext referenceContext;

    public BulletSplitEvent(@NotNull TearBullet parent, @NotNull SplitSequence splitSequence,
                            @NotNull AttackContext referenceContext) {
        this.parent = Objects.requireNonNull(parent, "parent");
        this.splitSequence = Objects.requireNonNull(splitSequence, "splitSequence").copy();
        this.referenceContext = Objects.requireNonNull(referenceContext, "referenceContext").copy();
    }

    public TearBullet getParent() { return parent; }
    public SplitSequence getSplitSequence() { return splitSequence.copy(); }
    public AttackContext getReferenceContext() { return referenceContext.copy(); }
}

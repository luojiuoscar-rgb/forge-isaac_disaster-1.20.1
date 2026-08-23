package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Registry-defined split behavior. It generates contexts but never spawns entities itself. */
public abstract class SplitModule {
    private final AttackPattern pattern;
    private final int bulletCount;
    private final AttackType childAttackType;
    private final double priority;

    protected SplitModule(@NotNull AttackPattern pattern, int bulletCount,
                          @NotNull AttackType childAttackType, double priority) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.bulletCount = bulletCount;
        this.childAttackType = Objects.requireNonNull(childAttackType, "childAttackType");
        this.priority = priority;
    }

    /** Returns whether this module participates in the current split event. */
    public boolean isApplicable(@NotNull BulletSplitEvent event) {
        return true;
    }

    /** Generates child contexts and attaches an independent sequence to every child. */
    public final List<AttackContext> generate(
            @NotNull BulletSplitEvent event,
            @NotNull SplitSequence childSequence) {

        List<AttackContext> generated = pattern.generate(
                new AttackPatternContext(event.getReferenceContext(), bulletCount));
        List<AttackContext> contexts = new ArrayList<>(generated.size());
        for (AttackContext child : generated) {
            child.setSplitSequence(childSequence);
            contexts.add(child);
        }
        return List.copyOf(contexts);
    }

    public AttackPattern getPattern() { return pattern; }
    public int getBulletCount() { return bulletCount; }
    public AttackType getChildAttackType() { return childAttackType; }
    public double getPriority() { return priority; }
}

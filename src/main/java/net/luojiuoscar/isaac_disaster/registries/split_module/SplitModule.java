package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/** Registry-defined split behavior. It generates contexts but never spawns entities itself. */
public abstract class SplitModule {
    private final AttackPattern pattern;
    private final AttackType childAttackType;

    protected SplitModule(@NotNull AttackPattern pattern, @NotNull AttackType childAttackType) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.childAttackType = Objects.requireNonNull(childAttackType, "childAttackType");
    }

    public abstract boolean canTrigger(@NotNull SplitContext context);

    public abstract int getBulletCount();

    public abstract @NotNull List<AttackContext> generate(@NotNull SplitContext context);

    public abstract void applyInheritance(@NotNull SplitContext context,
                                          @NotNull List<AttackContext> children);

    /**
     * Returns whether this module is retained in one generated child's sequence.
     * Runtime inheritance is evaluated by {@link SplitSequence} for every entry.
     */
    public boolean shouldInherit(@NotNull SplitContext context, @NotNull AttackContext childContext) {
        return true;
    }

    /** Returns the configured child type; special modules may override it. */
    public @NotNull AttackType resolveChildAttackType(@NotNull SplitContext context) {
        return childAttackType;
    }

    public AttackPattern getPattern() { return pattern; }
    public AttackType getChildAttackType() { return childAttackType; }
    public abstract double getPriority();
}

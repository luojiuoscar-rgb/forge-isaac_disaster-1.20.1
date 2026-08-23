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

    public abstract boolean canTrigger(@NotNull SplitContext context);

    public abstract @NotNull List<AttackContext> generate(@NotNull SplitContext context);

    public abstract void applyInheritance(@NotNull SplitContext context,
                                          @NotNull List<AttackContext> children);

    /** Returns the configured child type; special modules may override it. */
    public @NotNull AttackType resolveChildAttackType(@NotNull SplitContext context) {
        return childAttackType;
    }

    public AttackPattern getPattern() { return pattern; }
    public int getBulletCount() { return bulletCount; }
    public AttackType getChildAttackType() { return childAttackType; }
    public double getPriority() { return priority; }
}

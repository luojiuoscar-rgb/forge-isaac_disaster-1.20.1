package net.luojiuoscar.isaac_disaster.registries.split_module;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Objects;

/** Registry-defined split behavior. It generates contexts but never spawns entities itself. */
public abstract class SplitModule {
    private final AttackPattern pattern;
    private final AttackType childAttackType;

    protected SplitModule(AttackPattern pattern, AttackType childAttackType) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.childAttackType = Objects.requireNonNull(childAttackType, "childAttackType");
    }

    public abstract boolean canTrigger(SplitContext context);

    public abstract int getBulletCount();

    public abstract List<AttackContext> generate(SplitContext context);

    public abstract void applyInheritance(SplitContext context, List<AttackContext> children);

    /**
     * Returns whether this module is retained in one generated child's sequence.
     * Runtime inheritance is evaluated by {@link SplitSequence} for every entry.
     */
    public boolean shouldInherit(SplitContext context, AttackContext childContext) {
        return true;
    }

    /** Returns whether this module allows a child module to be copied to its generated bullets. */
    public boolean shouldInheritChildModule(SplitContext context, ResourceLocation childModuleId,
                                            AttackContext childContext) {
        return true;
    }

    /** Returns the configured child type; special modules may override it. */
    public AttackType resolveChildAttackType(SplitContext context) {
        return childAttackType;
    }

    /** Whether one sound plays for the child attack request produced by this module. */
    public boolean shouldPlayChildSound(SplitContext context) {
        return false;
    }

    public AttackPattern getPattern() { return pattern; }
    public AttackType getChildAttackType() { return childAttackType; }
    public abstract double getPriority();
}

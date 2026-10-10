package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Candidate produced while selecting the player's current main attack type.
 *
 * <p>A candidate can come from a single owned attack type or from a matched combination rule.
 * Selection remains deterministic even when several candidates share the same numeric priority.</p>
 */
public final class AttackCandidate {
    private final ResourceLocation attackTypeId;
    private final AttackType attackType;
    private final ResourceLocation rl;
    private final int requiredAttackCount;
    private final int priorityTier;
    private final double priority;

    public AttackCandidate(ResourceLocation attackTypeId, AttackType attackType,
                           ResourceLocation rl, int requiredAttackCount, int priorityTier, double priority) {
        this.attackTypeId = Objects.requireNonNull(attackTypeId);
        this.attackType = Objects.requireNonNull(attackType);
        this.rl = rl == null ? attackTypeId : rl;
        this.requiredAttackCount = Math.max(1, requiredAttackCount);
        this.priorityTier = priorityTier;
        this.priority = priority;
    }

    public ResourceLocation attackTypeId() {
        return attackTypeId;
    }

    public AttackType attackType() {
        return attackType;
    }

    public ResourceLocation ruleId() {
        return rl;
    }

    public int requiredAttackCount() {
        return requiredAttackCount;
    }

    public int priorityTier() {
        return priorityTier;
    }

    public double priority() {
        return priority;
    }
}

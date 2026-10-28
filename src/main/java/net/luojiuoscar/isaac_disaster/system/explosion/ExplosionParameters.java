package net.luojiuoscar.isaac_disaster.system.explosion;

import net.minecraft.world.level.Explosion;

import java.util.Objects;

/** Immutable explosion snapshot. Entity radius is the actual radius, not vanilla's half-radius power. */
public record ExplosionParameters(float power, float centerDamage, float entityRadius,
                                  float blockPower, float blockResistanceMultiplier,
                                  BlockDestructionMode blockDestructionMode, EntityImpactMode entityImpactMode,
                                  boolean ignoreFriendly, boolean causesFire,
                                  Explosion.BlockInteraction blockInteraction, FeedbackMode feedbackMode) {
    // Bound geometry independently of damage so malformed inputs cannot cause unbounded world scans.
    private static final float MAX_POWER = 128;
    private static final float MAX_ENTITY_RADIUS = 256;

    public enum BlockDestructionMode { RESISTANCE, IGNORE_RESISTANCE }
    public enum EntityImpactMode { STANDARD, GIGA }
    public enum FeedbackMode { STANDARD, GIGA }

    public ExplosionParameters {
        power = Math.min(nonNegative(power), MAX_POWER);
        centerDamage = nonNegative(centerDamage);
        entityRadius = Math.min(nonNegative(entityRadius), MAX_ENTITY_RADIUS);
        blockPower = Math.min(nonNegative(blockPower), MAX_POWER);
        blockResistanceMultiplier = nonNegative(blockResistanceMultiplier);
        Objects.requireNonNull(blockDestructionMode, "blockDestructionMode");
        Objects.requireNonNull(entityImpactMode, "entityImpactMode");
        Objects.requireNonNull(blockInteraction, "blockInteraction");
        Objects.requireNonNull(feedbackMode, "feedbackMode");
    }

    public static ExplosionParameters standard(float power, float centerDamage) {
        float strength = Math.min(nonNegative(power), MAX_POWER);
        return new ExplosionParameters(strength, centerDamage, strength * 2, strength, 1,
                BlockDestructionMode.RESISTANCE, EntityImpactMode.STANDARD, false, false,
                Explosion.BlockInteraction.DESTROY_WITH_DECAY, FeedbackMode.STANDARD);
    }

    public static ExplosionParameters giga(float power) {
        return new ExplosionParameters(power, 300, power, power, 1,
                BlockDestructionMode.IGNORE_RESISTANCE, EntityImpactMode.GIGA, true, false,
                Explosion.BlockInteraction.DESTROY, FeedbackMode.GIGA);
    }

    public ExplosionParameters withIgnoreFriendly(boolean value) {
        return new ExplosionParameters(power, centerDamage, entityRadius, blockPower, blockResistanceMultiplier,
                blockDestructionMode, entityImpactMode, value, causesFire, blockInteraction, feedbackMode);
    }

    public ExplosionParameters withCausesFire(boolean value) {
        return new ExplosionParameters(power, centerDamage, entityRadius, blockPower, blockResistanceMultiplier,
                blockDestructionMode, entityImpactMode, ignoreFriendly, value, blockInteraction, feedbackMode);
    }

    public ExplosionParameters withBlockInteraction(Explosion.BlockInteraction value) {
        return new ExplosionParameters(power, centerDamage, entityRadius, blockPower, blockResistanceMultiplier,
                blockDestructionMode, entityImpactMode, ignoreFriendly, causesFire, value, feedbackMode);
    }

    private static float nonNegative(float value) {
        return Float.isFinite(value) ? Math.max(0, value) : 0;
    }
}

package net.luojiuoscar.isaac_disaster.entity.tnt;

import net.minecraft.world.level.Explosion;

/**
 * Defines the built-in Isaac bomb profiles.
 *
 * <p>Explosion power controls block destruction only. Entity damage is described by center damage
 * and damage radius, so a profile can change damage without changing how far the bomb breaks
 * blocks.</p>
 */
public enum BombData {
    MEGA(7, 1.4f, 120.0f, 7.0f),
    NORMAL(4, 0.98f, 60.0f, 4.0f),
    SMALL(1, 0.4f, 30.0f, 1.0f);

    public static final BombData DEFAULT = NORMAL;

    private final int power;
    private final float size;
    private final int fuseTicks;
    private final float centerDamage;
    private final float damageRadius;
    private final float blockPower;
    private final float blockResistanceMultiplier;
    private final boolean causesFire;
    private final Explosion.BlockInteraction blockInteraction;

    BombData(int power, float size, float centerDamage, float damageRadius){
        this(power, size, 80, centerDamage, damageRadius, power, 1.0f,
                false, Explosion.BlockInteraction.DESTROY_WITH_DECAY);
    }

    BombData(int power, float size, int fuseTicks, float centerDamage, float damageRadius, float blockPower,
             float blockResistanceMultiplier, boolean causesFire, Explosion.BlockInteraction blockInteraction){
        this.power = power;
        this.size = size;
        this.fuseTicks = fuseTicks;
        this.centerDamage = centerDamage;
        this.damageRadius = damageRadius;
        this.blockPower = blockPower;
        this.blockResistanceMultiplier = blockResistanceMultiplier;
        this.causesFire = causesFire;
        this.blockInteraction = blockInteraction;
    }

    public int power() {
        return power;
    }

    public float size() {
        return size;
    }

    public int fuseTicks() {
        return fuseTicks;
    }

    public float centerDamage() {
        return centerDamage;
    }

    public float damageRadius() {
        return damageRadius;
    }

    public float blockPower() {
        return blockPower;
    }

    public float blockResistanceMultiplier() {
        return blockResistanceMultiplier;
    }

    public boolean causesFire() {
        return causesFire;
    }

    public Explosion.BlockInteraction blockInteraction() {
        return blockInteraction;
    }

    public static BombData fromPower(int power) {
        if (power >= MEGA.power()) return MEGA;
        if (power <= SMALL.power()) return SMALL;
        return NORMAL;
    }
}

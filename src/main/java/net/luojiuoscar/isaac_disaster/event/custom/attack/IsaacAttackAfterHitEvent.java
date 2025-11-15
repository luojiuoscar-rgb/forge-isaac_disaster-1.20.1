package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Set;

public class IsaacAttackAfterHitEvent extends IsaacAttackEvent {
    private final EntityHitResult hit;
    private double damage;
    private final double targetHealth;

    public IsaacAttackAfterHitEvent(Object directSource, Entity indirectSource, int attackTypeId, Set<Integer> hitEffects,
                                    EntityHitResult hit, double damage, double targetHealth) {
        super(directSource, indirectSource, attackTypeId, hitEffects);
        this.hit = hit;
        this.damage = damage;
        this.targetHealth = targetHealth;
    }

    public EntityHitResult getHitResult() { return hit; }

    public double getDamage() { return damage; }
    public double getTargetHealth() { return targetHealth; }

    public void setDamage(double damage) { this.damage = damage; }
}

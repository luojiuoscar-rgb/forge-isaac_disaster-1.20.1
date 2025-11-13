package net.luojiuoscar.isaac_disaster.event.custom.attack;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Set;

public class IsaacAttackAfterHitEvent extends IsaacAttackEvent {
    private final EntityHitResult hit;
    private double damage;
    private final double targetHealth;
    private boolean discardAfterHit = true; // 默认命中后消失

    public IsaacAttackAfterHitEvent(Entity entity, int attackTypeId, Set<Integer> hitEffects,
                                    EntityHitResult hit, double damage, double targetHealth) {
        super(entity, attackTypeId, hitEffects);
        this.hit = hit;
        this.damage = damage;
        this.targetHealth = targetHealth;
    }

    public EntityHitResult getHitResult() { return hit; }

    public double getDamage() { return damage; }
    public double getTargetHealth() { return targetHealth; }

    public void setDamage(double damage) { this.damage = damage; }

    public boolean shouldDiscardAfterHit() { return discardAfterHit; }

    public void setDiscardAfterHit(boolean discardAfterHit) { this.discardAfterHit = discardAfterHit; }
}

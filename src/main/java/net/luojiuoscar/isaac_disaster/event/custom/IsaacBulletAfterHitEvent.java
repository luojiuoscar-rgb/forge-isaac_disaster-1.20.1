package net.luojiuoscar.isaac_disaster.event.custom;

import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.minecraft.world.phys.EntityHitResult;

public class IsaacBulletAfterHitEvent extends IsaacBulletEvent {
    private final EntityHitResult hit;
    private double damage;
    private final double targetHealth;
    private boolean discardAfterHit = true; // 默认命中后消失

    public IsaacBulletAfterHitEvent(IsaacBullet bullet, EntityHitResult hit, double damage, double targetHealth) {
        super(bullet);
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

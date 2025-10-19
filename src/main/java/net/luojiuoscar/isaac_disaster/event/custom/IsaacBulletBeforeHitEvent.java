package net.luojiuoscar.isaac_disaster.event.custom;

import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class IsaacBulletBeforeHitEvent extends IsaacBulletEvent {
    private final EntityHitResult hit;
    private double damage;

    public IsaacBulletBeforeHitEvent(IsaacBullet bullet, EntityHitResult hit, double damage) {
        super(bullet);
        this.hit = hit;
        this.damage = damage;
    }

    public EntityHitResult getHit() { return hit; }

    public double getDamage() { return damage; }

    public void setDamage(double damage) { this.damage = damage; }
}

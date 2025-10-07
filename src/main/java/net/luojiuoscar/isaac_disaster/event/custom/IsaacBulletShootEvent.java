package net.luojiuoscar.isaac_disaster.event.custom;

import net.luojiuoscar.isaac_disaster.entity.projectile.IsaacBullet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class IsaacBulletShootEvent extends IsaacBulletEvent {
    private final LivingEntity shooter;
    public IsaacBulletShootEvent(IsaacBullet bullet, LivingEntity shooter) {
        super(bullet);
        this.shooter = shooter;
    }
    public LivingEntity getShooter() { return shooter; }
}
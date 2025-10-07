package net.luojiuoscar.isaac_disaster.event.custom;

import net.luojiuoscar.isaac_disaster.entity.projectile.IsaacBullet;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class IsaacBulletTickEvent extends IsaacBulletEvent {
    public IsaacBulletTickEvent(IsaacBullet bullet) {
        super(bullet);
    }
}
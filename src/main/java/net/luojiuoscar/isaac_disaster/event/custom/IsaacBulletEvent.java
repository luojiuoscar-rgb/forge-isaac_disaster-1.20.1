package net.luojiuoscar.isaac_disaster.event.custom;

import net.luojiuoscar.isaac_disaster.entity.IsaacBullet;

public class IsaacBulletEvent extends net.minecraftforge.eventbus.api.Event {
    private final IsaacBullet bullet;

    public IsaacBulletEvent(IsaacBullet bullet) {
        this.bullet = bullet;
    }

    public IsaacBullet getBullet() {
        return bullet;
    }
}

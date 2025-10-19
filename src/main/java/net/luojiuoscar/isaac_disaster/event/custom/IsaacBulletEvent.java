package net.luojiuoscar.isaac_disaster.event.custom;

import net.luojiuoscar.isaac_disaster.entity.custom.IsaacBullet;
import net.minecraftforge.eventbus.api.Event;

public class IsaacBulletEvent extends Event {
    private final IsaacBullet bullet;

    public IsaacBulletEvent(IsaacBullet bullet) {
        this.bullet = bullet;
    }

    public IsaacBullet getBullet() {
        return bullet;
    }
}

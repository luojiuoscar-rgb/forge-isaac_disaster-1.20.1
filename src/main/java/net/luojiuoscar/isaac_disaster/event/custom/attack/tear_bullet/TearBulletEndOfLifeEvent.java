package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.entity.custom.TearBullet;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class TearBulletEndOfLifeEvent extends Event {
    private final TearBullet bullet;

    public TearBulletEndOfLifeEvent(TearBullet bullet) {
        this.bullet = bullet;
    }

    public TearBullet getBullet() {
        return bullet;
    }
}
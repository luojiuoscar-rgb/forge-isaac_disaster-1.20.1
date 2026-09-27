package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class TearBulletEndOfLifeEvent extends Event {
    private final IBulletObject bullet;

    public TearBulletEndOfLifeEvent(IBulletObject bullet) {
        this.bullet = bullet;
    }

    /** Returns the backend-independent projectile object. */
    public IBulletObject getBulletObject() {
        return bullet;
    }

}

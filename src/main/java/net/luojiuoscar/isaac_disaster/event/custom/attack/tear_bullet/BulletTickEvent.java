package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class BulletTickEvent extends Event {
    private final IBulletObject bullet;

    public BulletTickEvent(IBulletObject bullet) {
        this.bullet = bullet;
    }

    public IBulletObject getBullet() {
        return bullet;
    }
}
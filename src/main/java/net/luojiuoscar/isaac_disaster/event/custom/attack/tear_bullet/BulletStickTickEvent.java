package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickContext;

/** Posted once per attached server tick. */
public final class BulletStickTickEvent extends BulletStickEvent {
    public BulletStickTickEvent(BulletStickContext context) { super(context); }
}

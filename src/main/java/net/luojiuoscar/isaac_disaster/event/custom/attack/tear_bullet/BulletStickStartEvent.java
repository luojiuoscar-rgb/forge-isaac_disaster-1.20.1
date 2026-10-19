package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickContext;

/** Posted after a bullet attaches to a living target. */
public final class BulletStickStartEvent extends BulletStickEvent {
    public BulletStickStartEvent(BulletStickContext context) { super(context); }
}

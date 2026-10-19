package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import java.util.Objects;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickContext;
import net.minecraftforge.eventbus.api.Event;

/** Server-side notification of an attachment transition; it cannot be canceled. */
public abstract class BulletStickEvent extends Event {
    private final BulletStickContext context;

    protected BulletStickEvent(BulletStickContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public final BulletStickContext getStickContext() { return context; }

    public final BulletState getBulletObject() { return context.bullet(); }
}

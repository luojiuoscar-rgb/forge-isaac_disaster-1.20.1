package net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet;

import java.util.Objects;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickContext;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickEndReason;

/** Posted exactly once when attachment ends, including forced removal. */
public final class BulletStickEndEvent extends BulletStickEvent {
    public BulletStickEndEvent(BulletStickContext context) {
        super(context);
        Objects.requireNonNull(context.endReason(), "endReason");
    }

    public BulletStickEndReason getReason() { return getStickContext().endReason(); }
}

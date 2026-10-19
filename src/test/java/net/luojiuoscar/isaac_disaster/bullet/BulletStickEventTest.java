package net.luojiuoscar.isaac_disaster.bullet;

import java.util.UUID;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickContext;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickEndReason;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.*;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulletStickEventTest {
    @Test
    void lifecycleNotificationsExposeTheSameImmutableSnapshotAndCannotBeCanceled() {
        BulletState bullet = BulletState.builder().build();
        UUID targetId = UUID.randomUUID();
        Vec3 position = new Vec3(1, 2, 3);
        var context = new BulletStickContext(bullet, null, targetId, position, 7, 100, null);
        for (BulletStickEvent event : new BulletStickEvent[]{new BulletStickStartEvent(context),
                new BulletStickTickEvent(context)}) {
            assertSame(context, event.getStickContext());
            assertSame(bullet, event.getBulletObject());
            assertFalse(event.isCancelable());
            assertThrows(UnsupportedOperationException.class, () -> event.setCanceled(true));
        }
        assertEquals(targetId, context.targetUuid());
        assertSame(position, context.position());
        assertEquals(7, context.elapsedTicks());
        assertEquals(100, context.durationTicks());
        assertNull(context.target());
        assertNull(context.endReason());
        var ended = new BulletStickContext(bullet, null, targetId, position, 100, 100,
                BulletStickEndReason.EXPIRED);
        var end = new BulletStickEndEvent(ended);
        assertSame(ended, end.getStickContext());
        assertSame(bullet, end.getBulletObject());
        assertEquals(BulletStickEndReason.EXPIRED, end.getReason());
        assertFalse(end.isCancelable());
        assertThrows(UnsupportedOperationException.class, () -> end.setCanceled(true));
    }

    @Test
    void onlyExpirationIsNaturalAndEveryOtherExitIsForced() {
        for (BulletStickEndReason reason : BulletStickEndReason.values()) {
            assertEquals(reason == BulletStickEndReason.EXPIRED, reason.isNatural());
            assertEquals(reason != BulletStickEndReason.EXPIRED, reason.isForced());
        }
    }
}

package net.luojiuoscar.isaac_disaster.bullet.client;

import java.util.UUID;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickState;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulletVisibilityReentryTest {
    private static BulletState state() {
        return BulletState.builder().velocity(new Vec3(1, 0, 0)).stickDurationTicks(20).build();
    }
    @Test void sameGenerationRestoresAfterRecordedVisibilityExit() {
        BulletStream stream = new BulletStream();
        stream.spawn(1, 2, 10, state());
        assertTrue(stream.visibilityExit(1, 2, 15));
        assertNull(stream.get(1, 2));
        assertNull(stream.spawn(1, 2, 14, state()));
        BulletState restored = state();
        restored.applyStickSnapshot(new BulletStickState.Snapshot(
            BulletStickState.Phase.STUCK,
            20, 5, 0, 42, new UUID(1, 2), new Vec3(0, 1, 0)));
        assertSame(restored, stream.spawn(1, 2, 15, restored));
        assertTrue(stream.get(1, 2).isStuck());
        assertNull(stream.spawn(1, 2, 16, state()));
    }
    @Test void terminalRemovalRevokesPermitAndCannotBeConvertedIntoVisibilityExit() {
        BulletStream stream = new BulletStream();
        stream.spawn(1, 2, 10, state());
        stream.visibilityExit(1, 2, 15);
        stream.despawn(1, 2);
        assertFalse(stream.visibilityExit(1, 2, 17));
        assertNull(stream.spawn(1, 2, 20, state()));
    }
    @Test void staleVisibilityExitCannotRemoveReenteredSnapshot() {
        BulletStream stream = new BulletStream();
        stream.spawn(1, 2, 10, state());
        stream.visibilityExit(1, 2, 15);
        BulletState restored = stream.spawn(1, 2, 20, state());
        assertFalse(stream.visibilityExit(1, 2, 19));
        assertSame(restored, stream.get(1, 2));
    }
    @Test void olderGenerationExitOrTerminalCannotAffectNewGeneration() {
        BulletStream stream = new BulletStream();
        stream.spawn(1, 2, 10, state());
        stream.visibilityExit(1, 2, 15);
        BulletState replacement = stream.spawn(1, 3, 20, state());
        assertFalse(stream.visibilityExit(1, 2, 30));
        assertFalse(stream.despawn(1, 2));
        assertSame(replacement, stream.get(1, 3));
        stream.visibilityExit(1, 3, 35);
        assertFalse(stream.despawn(1, 2));
        assertNotNull(stream.spawn(1, 3, 40, state()));
    }
    @Test void terminalTombstoneCannotBeResurrectedWithoutAnExit() {
        BulletStream stream = new BulletStream();
        stream.spawn(1, 2, 10, state());
        stream.despawn(1, 2);
        assertNull(stream.spawn(1, 2, 20, state()));
        assertNotNull(stream.spawn(1, 3, 21, state()));
    }
}

package net.luojiuoscar.isaac_disaster.bullet;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.registries.trajectory.*;
import net.luojiuoscar.isaac_disaster.registries.trajectory.impl.TinyPlanetBulletTrajectoryModule;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class TinyPlanetSlotTest {
    private static final class Shot {
        final BulletState bullet =
            BulletState.builder()
                .position(Vec3.ZERO)
                .velocity(new Vec3(1, 0, 0))
                .baseSpeed(1)
                .range(200)
                .build();
        final TinyPlanetBulletTrajectoryModule module = new TinyPlanetBulletTrajectoryModule();
        TrajectoryRuntimeState state = new TrajectoryRuntimeState();

        Shot(int slot) {
            bullet.assignSlot(slot, 1);
        }

        void step() {
            var motion =
                module.apply(
                    new TrajectoryContext(
                        bullet,
                        bullet.position(),
                        bullet.velocity(),
                        new Vec3(0.1, 0, 0),
                        Vec3.ZERO,
                        state.path().distance(),
                        0,
                        1,
                        Vec3.ZERO,
                        0,
                        state));
            assertTrue(motion.desiredPosition().distanceTo(bullet.position()) <= 0.10000001);
            assertTrue(motion.desiredVelocity().length() <= 0.10000001);
            bullet.setPosition(motion.desiredPosition());
            bullet.setVelocity(motion.desiredVelocity());
        }
    }

    @Test
    void equalSeedsWithAdjacentSlotsOrbitInOppositeDirections() {
        Shot even = new Shot(0), odd = new Shot(1);
        for (int i = 0; i < 600; i++) {
            Vec3 beforeEven = even.bullet.position(), beforeOdd = odd.bullet.position();
            even.step();
            odd.step();
            assertEquals(even.bullet.position().x, odd.bullet.position().x, 1e-8);
            assertEquals(even.bullet.position().z, -odd.bullet.position().z, 1e-8);
            if (i > 80) {
                assertTrue(beforeEven.cross(even.bullet.position()).y < 0);
                assertTrue(beforeOdd.cross(odd.bullet.position()).y > 0);
                assertEquals(3, Math.hypot(even.bullet.position().x, even.bullet.position().z), 1e-7);
            }
        }
    }

    @Test
    void copiedAndDecodedStatePreserveOrbitDirectionAndPhase() {
        Shot server = new Shot(1);
        for (int i = 0; i < 100; i++) server.step();
        Shot client = new Shot(1);
        client.bullet.setPosition(server.bullet.position());
        client.bullet.setVelocity(server.bullet.velocity());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            server.state.copy().write(buf);
            client.state = TrajectoryRuntimeState.read(buf);
        } finally {
            buf.release();
        }
        for (int i = 0; i < 100; i++) {
            server.step();
            client.step();
            assertEquals(server.bullet.position(), client.bullet.position());
        }
        client.state.path().suspend();
        assertFalse(server.state.path().suspended());
    }
}

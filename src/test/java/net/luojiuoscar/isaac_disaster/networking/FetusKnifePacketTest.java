package net.luojiuoscar.isaac_disaster.networking;

import io.netty.buffer.Unpooled;
import net.luojiuoscar.isaac_disaster.bullet.client.ClientBulletRuntime;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletSteeringMode;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletSpawnS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FetusKnifePacketTest {
    @Test
    void spawnAndLateObserverSnapshotsRestoreArmedAndPlainFetusesWithoutInventoryLookups() {
        ClientBulletRuntime client = ClientBulletRuntime.INSTANCE;
        for (double speed : new double[]{1, 0.07, 0.01}) {
        for (boolean armed : new boolean[]{true, false}) {
            client.clear();
            BulletState source = BulletState.builder().velocity(new Vec3(0, 0, speed)).lifetime(100).build();
            var packet = new BulletSpawnS2CPacket(1, 3, 2, 50, new Vec3(3, 4, 5), new Vec3(3, 4, 4),
                    source.velocity(), Vec3.ZERO, 1, 20, 100, 20, 64, 3, 2, 0.42, 1.26,
                    0xffffff, 1, List.of(), ResourceLocation.parse("isaac_disaster:c_section"),
                    ResourceLocation.parse("isaac_disaster:bullet"), null, true, false, 6, 0.6, 64, 0.8,
                    BulletSteeringMode.DIRECT, source.getTrajectoryRuntime().snapshot(), true,
                    source.stickState().snapshot(), 1, armed);
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                packet.toBytes(buffer);
                var decoded = new BulletSpawnS2CPacket(buffer);
                assertEquals(armed, decoded.fetusKnife());
                assertEquals(0, buffer.readableBytes());
                client.spawn(decoded);
                var state = client.stream().get(3, 2);
                assertNotNull(state);
                assertEquals(armed, state.fetusKnife() != null);
                assertEquals(2, state.renderScale());
                assertEquals(20, state.age());
            } finally {
                client.clear();
                buffer.release();
            }
        }
        }
    }
}

package net.luojiuoscar.isaac_disaster.networking.packet.bullet;

import java.util.List;
import java.util.function.Supplier;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickState;
import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** Ordered attachment transitions with complete resumable trajectory snapshots. */
public final class BulletStickBatchS2CPacket {
    private final int epoch;
    private final int serverTick;
    private final List<Entry> entries;

    public BulletStickBatchS2CPacket(int epoch, int serverTick, List<Entry> entries) {
        this.epoch = epoch;
        this.serverTick = serverTick;
        this.entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public BulletStickBatchS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readList(Entry::read));
    }

    public int epoch() { return epoch; }
    public int serverTick() { return serverTick; }
    public List<Entry> entries() { return entries; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch);
        buf.writeVarInt(serverTick);
        buf.writeCollection(entries, (buffer, entry) -> entry.toBytes(buffer));
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleBulletStick(this)));
        context.setPacketHandled(true);
    }

    public record Entry(int slot, int generation, Vec3 position, Vec3 velocity, Vec3 acceleration,
                        int age, double traveled, BulletStickState.Snapshot stickSnapshot,
                        TrajectoryRuntime.Snapshot trajectorySnapshot) {
        public static Entry from(BulletState state) {
            return new Entry(state.slot(), state.generation(), state.position(), state.velocity(),
                    state.acceleration(), state.age(), state.traveled(), state.stickState().snapshot(),
                    state.getTrajectoryRuntime().snapshot());
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeVarInt(slot);
            buf.writeVarInt(generation);
            writeVec(buf, position);
            writeVec(buf, velocity);
            writeVec(buf, acceleration);
            buf.writeVarInt(age);
            buf.writeDouble(traveled);
            stickSnapshot.write(buf);
            trajectorySnapshot.write(buf);
        }
        private static Entry read(FriendlyByteBuf buf) {
            return new Entry(buf.readVarInt(), buf.readVarInt(), readVec(buf), readVec(buf), readVec(buf),
                buf.readVarInt(), buf.readDouble(), BulletStickState.Snapshot.read(buf),
                TrajectoryRuntime.Snapshot.read(buf));
        }
        private static void writeVec(FriendlyByteBuf buf, Vec3 v) {
            buf.writeDouble(v.x); buf.writeDouble(v.y); buf.writeDouble(v.z);
        }
        private static Vec3 readVec(FriendlyByteBuf buf) {
            return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
    }
}

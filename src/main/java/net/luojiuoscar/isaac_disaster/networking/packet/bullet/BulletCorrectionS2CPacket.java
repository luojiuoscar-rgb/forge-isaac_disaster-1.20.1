package net.luojiuoscar.isaac_disaster.networking.packet.bullet;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;

import java.util.function.Supplier;

public final class BulletCorrectionS2CPacket {
    private final int epoch;
    private final int slot;
    private final int generation;
    private final Vec3 position;
    private final Vec3 velocity;
    private final float blend;
    private final int age;
    private final double traveled;
    private final TrajectoryRuntime.Snapshot trajectorySnapshot;

    public BulletCorrectionS2CPacket(int epoch, int slot, int generation, Vec3 position, Vec3 velocity, float blend) {
        this(epoch, slot, generation, position, velocity, blend, 0, 0.0D, null);
    }

    public BulletCorrectionS2CPacket(int epoch, int slot, int generation, Vec3 position, Vec3 velocity, float blend,
                                     TrajectoryRuntime.Snapshot trajectorySnapshot) {
        this(epoch, slot, generation, position, velocity, blend, 0, 0.0D, trajectorySnapshot);
    }

    public BulletCorrectionS2CPacket(int epoch, int slot, int generation, Vec3 position, Vec3 velocity, float blend,
                                     int age, double traveled, TrajectoryRuntime.Snapshot trajectorySnapshot) {
        this.epoch = epoch; this.slot = slot; this.generation = generation;
        this.position = position == null ? Vec3.ZERO : position;
        this.velocity = velocity == null ? Vec3.ZERO : velocity;
        this.blend = blend;
        this.age = Math.max(0, age);
        this.traveled = Double.isFinite(traveled) ? Math.max(0.0D, traveled) : 0.0D;
        this.trajectorySnapshot = trajectorySnapshot;
    }

    public BulletCorrectionS2CPacket(int slot, int generation, Vec3 position, Vec3 velocity, float blend) {
        this(0, slot, generation, position, velocity, blend);
    }

    public BulletCorrectionS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), readVec(buf), readVec(buf), buf.readFloat(),
                buf.readVarInt(), buf.readDouble(), buf.readBoolean() ? TrajectoryRuntime.Snapshot.read(buf) : null);
    }

    public int epoch() { return epoch; }
    public int slot() { return slot; }
    public int generation() { return generation; }
    public Vec3 position() { return position; }
    public Vec3 velocity() { return velocity; }
    public float blend() { return blend; }
    public int age() { return age; }
    public double traveled() { return traveled; }
    public TrajectoryRuntime.Snapshot trajectorySnapshot() { return trajectorySnapshot; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch);
        buf.writeVarInt(slot);
        buf.writeVarInt(generation);
        writeVec(buf, position);
        writeVec(buf, velocity);
        buf.writeFloat(blend);
        buf.writeVarInt(age);
        buf.writeDouble(traveled);
        buf.writeBoolean(trajectorySnapshot != null);
        if (trajectorySnapshot != null) trajectorySnapshot.write(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleBulletCorrection(this)));
        context.setPacketHandled(true);
    }

    private static Vec3 readVec(FriendlyByteBuf buf) {
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    private static void writeVec(FriendlyByteBuf buf, Vec3 value) {
        Vec3 vector = value == null ? Vec3.ZERO : value;
        buf.writeDouble(vector.x);
        buf.writeDouble(vector.y);
        buf.writeDouble(vector.z);
    }
}

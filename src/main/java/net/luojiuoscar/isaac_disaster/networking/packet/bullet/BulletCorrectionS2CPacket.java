package net.luojiuoscar.isaac_disaster.networking.packet.bullet;

import net.luojiuoscar.isaac_disaster.bullet.client.ClientBulletRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class BulletCorrectionS2CPacket {
    private final int epoch;
    private final int slot;
    private final int generation;
    private final Vec3 position;
    private final Vec3 velocity;
    private final float blend;

    public BulletCorrectionS2CPacket(int epoch, int slot, int generation, Vec3 position, Vec3 velocity, float blend) {
        this.epoch = epoch;
        this.slot = slot;
        this.generation = generation;
        this.position = position == null ? Vec3.ZERO : position;
        this.velocity = velocity == null ? Vec3.ZERO : velocity;
        this.blend = blend;
    }

    public BulletCorrectionS2CPacket(int slot, int generation, Vec3 position, Vec3 velocity, float blend) {
        this(0, slot, generation, position, velocity, blend);
    }

    public BulletCorrectionS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), readVec(buf), readVec(buf), buf.readFloat());
    }

    public int epoch() { return epoch; }
    public int slot() { return slot; }
    public int generation() { return generation; }
    public Vec3 position() { return position; }
    public Vec3 velocity() { return velocity; }
    public float blend() { return blend; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch);
        buf.writeVarInt(slot);
        buf.writeVarInt(generation);
        writeVec(buf, position);
        writeVec(buf, velocity);
        buf.writeFloat(blend);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientBulletRuntime.INSTANCE.correct(this));
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

package net.luojiuoscar.isaac_disaster.networking.packet.bullet;

import net.luojiuoscar.isaac_disaster.bullet.ClientBulletRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public final class BulletShatterS2CPacket {
    public static final class Entry {
        private final Vec3 position;
        private final Vec3 velocity;
        private final double scale;
        private final int color;
        private final float alpha;
        private final List<ResourceLocation> visualIds;
        private final boolean fetus;

        public Entry(Vec3 position, Vec3 velocity, double scale, int color, float alpha,
                     List<ResourceLocation> visualIds, boolean fetus) {
            this.position = position == null ? Vec3.ZERO : position;
            this.velocity = velocity == null ? Vec3.ZERO : velocity;
            this.scale = scale;
            this.color = color;
            this.alpha = alpha;
            this.visualIds = visualIds == null ? List.of() : List.copyOf(visualIds);
            this.fetus = fetus;
        }

        public Vec3 position() { return position; }
        public Vec3 velocity() { return velocity; }
        public double scale() { return scale; }
        public int color() { return color; }
        public float alpha() { return alpha; }
        public List<ResourceLocation> visualIds() { return visualIds; }
        public boolean fetus() { return fetus; }
    }

    private final int epoch;
    private final List<Entry> entries;

    public BulletShatterS2CPacket(int epoch, List<Entry> entries) {
        this.epoch = epoch;
        this.entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public BulletShatterS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readList(value -> new Entry(readVec(value), readVec(value), value.readDouble(),
                value.readInt(), value.readFloat(), value.readList(FriendlyByteBuf::readResourceLocation), value.readBoolean())));
    }

    public int epoch() { return epoch; }
    public List<Entry> entries() { return entries; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch);
        buf.writeCollection(entries, (value, entry) -> {
            writeVec(value, entry.position());
            writeVec(value, entry.velocity());
            value.writeDouble(entry.scale());
            value.writeInt(entry.color());
            value.writeFloat(entry.alpha());
            value.writeCollection(entry.visualIds(), FriendlyByteBuf::writeResourceLocation);
            value.writeBoolean(entry.fetus());
        });
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientBulletRuntime.INSTANCE.applyShatter(this));
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

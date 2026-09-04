package net.luojiuoscar.isaac_disaster.networking.packet.bullet;

import net.luojiuoscar.isaac_disaster.bullet.ClientBulletRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public final class BulletTrackingBatchS2CPacket {
    public static final class TargetSample {
        private final int handle;
        private final Vec3 position;

        public TargetSample(int handle, Vec3 position) {
            this.handle = handle;
            this.position = position == null ? Vec3.ZERO : position;
        }

        public int handle() { return handle; }
        public Vec3 position() { return position; }
    }

    public static final class Assignment {
        private final int slot;
        private final int generation;
        private final int handle;
        private final boolean control;

        public Assignment(int slot, int generation, int handle, boolean control) {
            this.slot = slot;
            this.generation = generation;
            this.handle = handle;
            this.control = control;
        }

        public int slot() { return slot; }
        public int generation() { return generation; }
        public int handle() { return handle; }
        public boolean control() { return control; }
    }

    public static final class VelocitySample {
        private final int slot;
        private final int generation;
        private final Vec3 velocity;
        private final Vec3 acceleration;

        public VelocitySample(int slot, int generation, Vec3 velocity, Vec3 acceleration) {
            this.slot = slot;
            this.generation = generation;
            this.velocity = velocity == null ? Vec3.ZERO : velocity;
            this.acceleration = acceleration == null ? Vec3.ZERO : acceleration;
        }

        public int slot() { return slot; }
        public int generation() { return generation; }
        public Vec3 velocity() { return velocity; }
        public Vec3 acceleration() { return acceleration; }
    }

    private final int epoch;
    private final int sampleTick;
    private final List<TargetSample> targets;
    private final List<Assignment> assignments;
    private final List<VelocitySample> velocitySamples;

    public BulletTrackingBatchS2CPacket(int epoch, int sampleTick, List<TargetSample> targets,
                                        List<Assignment> assignments, List<VelocitySample> velocitySamples) {
        this.epoch = epoch;
        this.sampleTick = sampleTick;
        this.targets = targets == null ? List.of() : List.copyOf(targets);
        this.assignments = assignments == null ? List.of() : List.copyOf(assignments);
        this.velocitySamples = velocitySamples == null ? List.of() : List.copyOf(velocitySamples);
    }

    public BulletTrackingBatchS2CPacket(int epoch, int sampleTick, List<TargetSample> targets,
                                        List<Assignment> assignments) {
        this(epoch, sampleTick, targets, assignments, List.of());
    }

    public BulletTrackingBatchS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(),
                buf.readList(value -> new TargetSample(value.readVarInt(), readVec(value))),
                buf.readList(value -> new Assignment(value.readVarInt(), value.readVarInt(), value.readVarInt(), value.readBoolean())),
                buf.readList(value -> new VelocitySample(value.readVarInt(), value.readVarInt(), readVec(value), readVec(value))));
    }

    public int epoch() { return epoch; }
    public int sampleTick() { return sampleTick; }
    public List<TargetSample> targets() { return targets; }
    public List<Assignment> assignments() { return assignments; }
    public List<VelocitySample> velocitySamples() { return velocitySamples; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch);
        buf.writeVarInt(sampleTick);
        buf.writeCollection(targets, (value, sample) -> {
            value.writeVarInt(sample.handle());
            writeVec(value, sample.position());
        });
        buf.writeCollection(assignments, (value, assignment) -> {
            value.writeVarInt(assignment.slot());
            value.writeVarInt(assignment.generation());
            value.writeVarInt(assignment.handle());
            value.writeBoolean(assignment.control());
        });
        buf.writeCollection(velocitySamples, (value, sample) -> {
            value.writeVarInt(sample.slot());
            value.writeVarInt(sample.generation());
            writeVec(value, sample.velocity());
            writeVec(value, sample.acceleration());
        });
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientBulletRuntime.INSTANCE.applyTracking(this));
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

package net.luojiuoscar.isaac_disaster.networking.packet.bullet;

import net.luojiuoscar.isaac_disaster.bullet.ClientBulletRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class BulletSpawnS2CPacket {
    private final int epoch;
    private final int slot;
    private final int generation;
    private final int snapshotTick;
    private final Vec3 position;
    private final Vec3 previousPosition;
    private final Vec3 velocity;
    private final Vec3 acceleration;
    private final double baseSpeed;
    private final int age;
    private final int lifetime;
    private final double traveled;
    private final double range;
    private final float damage;
    private final double renderScale;
    private final double collisionWidth;
    private final double collisionHeight;
    private final int color;
    private final float alpha;
    private final List<ResourceLocation> visualIds;
    private final boolean fetus;
    private final UUID ownerUuid;
    private final boolean homing;
    private final boolean controllable;
    private final double homingRange;
    private final double homingSteer;
    private final double controlRange;
    private final double controlSteer;

    public BulletSpawnS2CPacket(int epoch, int slot, int generation, int snapshotTick, Vec3 position,
                                Vec3 previousPosition, Vec3 velocity, Vec3 acceleration, double baseSpeed, int age,
                                int lifetime, double traveled, double range, float damage, double renderScale,
                                double collisionWidth, double collisionHeight, int color, float alpha,
                                List<ResourceLocation> visualIds, boolean fetus, UUID ownerUuid, boolean homing,
                                boolean controllable, double homingRange, double homingSteer,
                                double controlRange, double controlSteer) {
        this.epoch = epoch;
        this.slot = slot;
        this.generation = generation;
        this.snapshotTick = snapshotTick;
        this.position = position == null ? Vec3.ZERO : position;
        this.previousPosition = previousPosition == null ? this.position : previousPosition;
        this.velocity = velocity == null ? Vec3.ZERO : velocity;
        this.acceleration = acceleration == null ? Vec3.ZERO : acceleration;
        this.baseSpeed = baseSpeed;
        this.age = age;
        this.lifetime = lifetime;
        this.traveled = traveled;
        this.range = range;
        this.damage = damage;
        this.renderScale = renderScale;
        this.collisionWidth = collisionWidth;
        this.collisionHeight = collisionHeight;
        this.color = color;
        this.alpha = alpha;
        this.visualIds = visualIds == null ? List.of() : List.copyOf(visualIds);
        this.fetus = fetus;
        this.ownerUuid = ownerUuid;
        this.homing = homing;
        this.controllable = controllable;
        this.homingRange = homingRange;
        this.homingSteer = homingSteer;
        this.controlRange = controlRange;
        this.controlSteer = controlSteer;
    }

    public BulletSpawnS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), readVec(buf), readVec(buf),
                readVec(buf), readVec(buf), buf.readDouble(), buf.readVarInt(), buf.readVarInt(), buf.readDouble(),
                buf.readDouble(), buf.readFloat(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt(),
                buf.readFloat(), buf.readList(FriendlyByteBuf::readResourceLocation), buf.readBoolean(), readUuid(buf),
                buf.readBoolean(), buf.readBoolean(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public int epoch() { return epoch; }
    public int slot() { return slot; }
    public int generation() { return generation; }
    public int snapshotTick() { return snapshotTick; }
    public Vec3 position() { return position; }
    public Vec3 previousPosition() { return previousPosition; }
    public Vec3 velocity() { return velocity; }
    public Vec3 acceleration() { return acceleration; }
    public double baseSpeed() { return baseSpeed; }
    public int age() { return age; }
    public int lifetime() { return lifetime; }
    public double traveled() { return traveled; }
    public double range() { return range; }
    public float damage() { return damage; }
    public double renderScale() { return renderScale; }
    public double collisionWidth() { return collisionWidth; }
    public double collisionHeight() { return collisionHeight; }
    public int color() { return color; }
    public float alpha() { return alpha; }
    public List<ResourceLocation> visualIds() { return visualIds; }
    public boolean fetus() { return fetus; }
    public UUID ownerUuid() { return ownerUuid; }
    public boolean homing() { return homing; }
    public boolean controllable() { return controllable; }
    public double homingRange() { return homingRange; }
    public double homingSteer() { return homingSteer; }
    public double controlRange() { return controlRange; }
    public double controlSteer() { return controlSteer; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch); buf.writeVarInt(slot); buf.writeVarInt(generation); buf.writeVarInt(snapshotTick);
        writeVec(buf, position); writeVec(buf, previousPosition); writeVec(buf, velocity); writeVec(buf, acceleration);
        buf.writeDouble(baseSpeed); buf.writeVarInt(age); buf.writeVarInt(lifetime); buf.writeDouble(traveled);
        buf.writeDouble(range); buf.writeFloat(damage); buf.writeDouble(renderScale); buf.writeDouble(collisionWidth);
        buf.writeDouble(collisionHeight); buf.writeInt(color); buf.writeFloat(alpha);
        buf.writeCollection(visualIds, FriendlyByteBuf::writeResourceLocation); buf.writeBoolean(fetus);
        buf.writeBoolean(ownerUuid != null); if (ownerUuid != null) buf.writeUUID(ownerUuid);
        buf.writeBoolean(homing); buf.writeBoolean(controllable); buf.writeDouble(homingRange); buf.writeDouble(homingSteer);
        buf.writeDouble(controlRange); buf.writeDouble(controlSteer);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> ClientBulletRuntime.INSTANCE.spawn(this));
        context.setPacketHandled(true);
    }

    private static Vec3 readVec(FriendlyByteBuf buf) {
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    private static void writeVec(FriendlyByteBuf buf, Vec3 value) {
        Vec3 vector = value == null ? Vec3.ZERO : value;
        buf.writeDouble(vector.x); buf.writeDouble(vector.y); buf.writeDouble(vector.z);
    }

    private static UUID readUuid(FriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readUUID() : null;
    }
}

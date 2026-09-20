package net.luojiuoscar.isaac_disaster.networking.packet.bullet;

import net.luojiuoscar.isaac_disaster.bullet.client.ClientBulletRuntime;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Server-to-client immutable bullet snapshot, including deterministic trajectory state. */
public final class BulletSpawnS2CPacket {
    private final int epoch, slot, generation, snapshotTick, age, lifetime, color;
    private final Vec3 position, previousPosition, velocity, acceleration;
    private final double baseSpeed, traveled, range, renderScale, collisionWidth, collisionHeight;
    private final float damage, alpha;
    private final List<ResourceLocation> visualIds;
    private final boolean homing, controllable;
    private final ResourceLocation typeId, rootTypeId;
    private final UUID ownerUuid;
    private final double homingRange, homingSteer, controlRange, controlSteer;
    private final TrajectoryRuntime.Snapshot trajectorySnapshot;

    public BulletSpawnS2CPacket(int epoch, int slot, int generation, int snapshotTick, Vec3 position,
                                Vec3 previousPosition, Vec3 velocity, Vec3 acceleration, double baseSpeed, int age,
                                int lifetime, double traveled, double range, float damage, double renderScale,
                                double collisionWidth, double collisionHeight, int color, float alpha,
                                List<ResourceLocation> visualIds, ResourceLocation typeId, ResourceLocation rootTypeId, UUID ownerUuid, boolean homing,
                                boolean controllable, double homingRange, double homingSteer,
                                double controlRange, double controlSteer, TrajectoryRuntime.Snapshot trajectorySnapshot) {
        this.epoch = epoch; this.slot = slot; this.generation = generation; this.snapshotTick = snapshotTick;
        this.position = position == null ? Vec3.ZERO : position;
        this.previousPosition = previousPosition == null ? this.position : previousPosition;
        this.velocity = velocity == null ? Vec3.ZERO : velocity;
        this.acceleration = acceleration == null ? Vec3.ZERO : acceleration;
        this.baseSpeed = baseSpeed; this.age = age; this.lifetime = lifetime; this.traveled = traveled; this.range = range;
        this.damage = damage; this.renderScale = renderScale; this.collisionWidth = collisionWidth; this.collisionHeight = collisionHeight;
        this.color = color; this.alpha = alpha; this.visualIds = visualIds == null ? List.of() : List.copyOf(visualIds);
        this.typeId = java.util.Objects.requireNonNull(typeId, "typeId");
        this.rootTypeId = java.util.Objects.requireNonNull(rootTypeId, "rootTypeId"); this.ownerUuid = ownerUuid; this.homing = homing; this.controllable = controllable;
        this.homingRange = homingRange; this.homingSteer = homingSteer; this.controlRange = controlRange; this.controlSteer = controlSteer;
        this.trajectorySnapshot = java.util.Objects.requireNonNull(trajectorySnapshot, "trajectorySnapshot");
    }

    public BulletSpawnS2CPacket(int epoch, int slot, int generation, int snapshotTick, Vec3 position,
                                Vec3 previousPosition, Vec3 velocity, Vec3 acceleration, double baseSpeed, int age,
                                int lifetime, double traveled, double range, float damage, double renderScale,
                                double collisionWidth, double collisionHeight, int color, float alpha,
                                List<ResourceLocation> visualIds, ResourceLocation typeId, ResourceLocation rootTypeId, UUID ownerUuid, boolean homing,
                                boolean controllable, double homingRange, double homingSteer,
                                double controlRange, double controlSteer) {
        this(epoch, slot, generation, snapshotTick, position, previousPosition, velocity, acceleration, baseSpeed, age,
                lifetime, traveled, range, damage, renderScale, collisionWidth, collisionHeight, color, alpha, visualIds,
                typeId, rootTypeId, ownerUuid, homing, controllable, homingRange, homingSteer, controlRange, controlSteer,
                new TrajectoryRuntime(position, velocity, List.of()).snapshot());
    }

    public BulletSpawnS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), readVec(buf), readVec(buf), readVec(buf), readVec(buf),
                buf.readDouble(), buf.readVarInt(), buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readInt(), buf.readFloat(), buf.readList(FriendlyByteBuf::readResourceLocation),
                buf.readResourceLocation(), buf.readResourceLocation(), readUuid(buf), buf.readBoolean(), buf.readBoolean(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                TrajectoryRuntime.Snapshot.read(buf));
    }

    public int epoch() { return epoch; } public int slot() { return slot; } public int generation() { return generation; }
    public int snapshotTick() { return snapshotTick; } public Vec3 position() { return position; } public Vec3 previousPosition() { return previousPosition; }
    public Vec3 velocity() { return velocity; } public Vec3 acceleration() { return acceleration; } public double baseSpeed() { return baseSpeed; }
    public int age() { return age; } public int lifetime() { return lifetime; } public double traveled() { return traveled; } public double range() { return range; }
    public float damage() { return damage; } public double renderScale() { return renderScale; } public double collisionWidth() { return collisionWidth; }
    public double collisionHeight() { return collisionHeight; } public int color() { return color; } public float alpha() { return alpha; }
    public List<ResourceLocation> visualIds() { return visualIds; } public ResourceLocation typeId() { return typeId; } public ResourceLocation rootTypeId() { return rootTypeId; } public UUID ownerUuid() { return ownerUuid; }
    public boolean homing() { return homing; } public boolean controllable() { return controllable; } public double homingRange() { return homingRange; }
    public double homingSteer() { return homingSteer; } public double controlRange() { return controlRange; } public double controlSteer() { return controlSteer; }
    public TrajectoryRuntime.Snapshot trajectorySnapshot() { return trajectorySnapshot; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch); buf.writeVarInt(slot); buf.writeVarInt(generation); buf.writeVarInt(snapshotTick);
        writeVec(buf, position); writeVec(buf, previousPosition); writeVec(buf, velocity); writeVec(buf, acceleration);
        buf.writeDouble(baseSpeed); buf.writeVarInt(age); buf.writeVarInt(lifetime); buf.writeDouble(traveled); buf.writeDouble(range);
        buf.writeFloat(damage); buf.writeDouble(renderScale); buf.writeDouble(collisionWidth); buf.writeDouble(collisionHeight); buf.writeInt(color); buf.writeFloat(alpha);
        buf.writeCollection(visualIds, FriendlyByteBuf::writeResourceLocation); buf.writeResourceLocation(typeId); buf.writeResourceLocation(rootTypeId); buf.writeBoolean(ownerUuid != null); if (ownerUuid != null) buf.writeUUID(ownerUuid);
        buf.writeBoolean(homing); buf.writeBoolean(controllable); buf.writeDouble(homingRange); buf.writeDouble(homingSteer); buf.writeDouble(controlRange); buf.writeDouble(controlSteer);
        trajectorySnapshot.write(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) { NetworkEvent.Context context = supplier.get(); context.enqueueWork(() -> ClientBulletRuntime.INSTANCE.spawn(this)); context.setPacketHandled(true); }
    private static Vec3 readVec(FriendlyByteBuf buf) { return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()); }
    private static void writeVec(FriendlyByteBuf buf, Vec3 value) { Vec3 v = value == null ? Vec3.ZERO : value; buf.writeDouble(v.x); buf.writeDouble(v.y); buf.writeDouble(v.z); }
    private static UUID readUuid(FriendlyByteBuf buf) { return buf.readBoolean() ? buf.readUUID() : null; }
}

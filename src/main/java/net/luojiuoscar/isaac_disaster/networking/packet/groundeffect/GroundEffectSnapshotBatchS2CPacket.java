package net.luojiuoscar.isaac_disaster.networking.packet.groundeffect;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.luojiuoscar.isaac_disaster.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectCoverage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class GroundEffectSnapshotBatchS2CPacket {
    private final int epoch;
    private final int serverTick;
    private final boolean update;
    private final List<Entry> entries;

    public GroundEffectSnapshotBatchS2CPacket(int epoch, int serverTick, boolean update, List<Entry> entries) {
        this.epoch = epoch;
        this.serverTick = serverTick;
        this.update = update;
        this.entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public GroundEffectSnapshotBatchS2CPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readList(Entry::read));
    }

    public int epoch() { return epoch; }
    public int serverTick() { return serverTick; }
    public boolean update() { return update; }
    public List<Entry> entries() { return entries; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch);
        buf.writeVarInt(serverTick);
        buf.writeBoolean(update);
        buf.writeCollection(entries, (buffer, entry) -> entry.toBytes(buffer));
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleGroundEffectSnapshot(this)));
        context.setPacketHandled(true);
    }

    public record Entry(int slot, int generation, Vec3 position, double radius, int age, int lifetime,
                        long creationOrder, UUID ownerUuid, ResourceLocation typeId,
                        int color, long shapeSeed, int revision, GroundEffectCoverage coverage) {
        public Entry(int slot, int generation, Vec3 position, double radius, int age, int lifetime,
                     long creationOrder, UUID ownerUuid, ResourceLocation typeId, int color, long shapeSeed) {
            this(slot, generation, position, radius, age, lifetime, creationOrder, ownerUuid, typeId,
                    color, shapeSeed, 0, GroundEffectCoverage.puddle(radius, shapeSeed));
        }

        public static Entry from(AbstractGroundEffect effect) {
            return new Entry(effect.slot(), effect.generation(), effect.position(), effect.radius(),
                    effect.age(), effect.lifetime(), effect.creationOrder(),
                    effect.ownerUuid(), effect.typeId(), effect.getColor(), effect.shapeSeed(), effect.revision(), effect.coverage());
        }

        /** Exact wire size; coverage already caches its geometry size, so this never encodes vertices. */
        public long serializedBytes() {
            // Valid ResourceLocation names consist solely of ASCII characters.
            int typeBytes = typeId.getNamespace().length() + 1 + typeId.getPath().length();
            if (typeBytes > 32767) throw new IllegalArgumentException("Ground-effect type id exceeds UTF limit");
            return 53L + (ownerUuid == null ? 0 : 16)
                    + varIntBytes(slot) + varIntBytes(generation) + varIntBytes(age) + varIntBytes(lifetime)
                    + varIntBytes(revision) + varIntBytes(typeBytes) + typeBytes + coverage.serializedBytes();
        }

        private static int varIntBytes(int value) {
            int bytes = 1;
            while ((value & ~127) != 0) { value >>>= 7; bytes++; }
            return bytes;
        }

        public static Entry read(FriendlyByteBuf buf) {
            int slot = buf.readVarInt();
            int generation = buf.readVarInt();
            Vec3 position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            double radius = buf.readDouble();
            int age = buf.readVarInt();
            int lifetime = buf.readVarInt();
            long order = buf.readLong();
            UUID owner = buf.readBoolean() ? buf.readUUID() : null;
            ResourceLocation type = buf.readResourceLocation();
            int color = buf.readInt();
            long seed = buf.readLong();
            int revision = buf.readVarInt();
            return new Entry(slot, generation, position, radius, age, lifetime, order, owner, type,
                    color, seed, revision, GroundEffectCoverage.read(buf, radius, seed));
        }

        public void toBytes(FriendlyByteBuf buf) {
            buf.writeVarInt(slot);
            buf.writeVarInt(generation);
            buf.writeDouble(position.x);
            buf.writeDouble(position.y);
            buf.writeDouble(position.z);
            buf.writeDouble(radius);
            buf.writeVarInt(age);
            buf.writeVarInt(lifetime);
            buf.writeLong(creationOrder);
            buf.writeBoolean(ownerUuid != null);
            if (ownerUuid != null) buf.writeUUID(ownerUuid);
            buf.writeResourceLocation(typeId);
            buf.writeInt(color);
            buf.writeLong(shapeSeed);
            buf.writeVarInt(revision);
            coverage.write(buf);
        }
    }
}

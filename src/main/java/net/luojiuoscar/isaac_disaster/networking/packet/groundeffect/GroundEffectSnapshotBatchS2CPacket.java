package net.luojiuoscar.isaac_disaster.networking.packet.groundeffect;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import java.util.ArrayList;
import java.util.Collections;
import net.luojiuoscar.isaac_disaster.groundeffect.core.AbstractGroundEffect;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import org.jetbrains.annotations.Nullable;
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
        this.entries = entries == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public GroundEffectSnapshotBatchS2CPacket(FriendlyByteBuf buf) {
        this(decode(buf));
    }

    private GroundEffectSnapshotBatchS2CPacket(GroundEffectSnapshotBatchS2CPacket decoded) {
        this(decoded.epoch(), decoded.serverTick(), decoded.update(), decoded.entries());
    }

    private static GroundEffectSnapshotBatchS2CPacket decode(FriendlyByteBuf buf) {
        return GroundEffectPacketCodec.decode(buf, input -> {
            int epoch = input.readVarInt();
            int tick = input.readVarInt();
            boolean update = input.readBoolean();
            int count = input.readVarInt();
            if (count < 0 || count > input.readableBytes() / 32) return null;
            List<Entry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                Entry entry = Entry.read(input);
                if (entry == null) return null;
                entries.add(entry);
            }
            return new GroundEffectSnapshotBatchS2CPacket(epoch, tick, update, entries);
        }, () -> new GroundEffectSnapshotBatchS2CPacket(0, 0, false, List.of()));
    }

    public int epoch() { return epoch; }
    public int serverTick() { return serverTick; }
    public boolean update() { return update; }
    public List<Entry> entries() { return entries; }

    public void toBytes(FriendlyByteBuf buf) {
        if (entries.stream().anyMatch(entry -> entry == null || !entry.isValid())) {
            IsaacDisaster.LOGGER.warn("Ignoring invalid ground-effect batch encoding");
            return;
        }
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
                        int color, long shapeSeed, int revision,
                        int animationDurationTicks, int animationElapsedTicks) {
        public Entry {
            animationDurationTicks = Math.max(0, animationDurationTicks);
            animationElapsedTicks = Math.max(0, animationElapsedTicks);
        }

        public boolean isValid() {
            return position != null && Double.isFinite(position.x) && Double.isFinite(position.y)
                    && Double.isFinite(position.z) && Double.isFinite(radius) && radius >= 0
                    && typeId != null
                    && typeId.getNamespace().length() + 1 + typeId.getPath().length() <= 32767;
        }
        public Entry(int slot, int generation, Vec3 position, double radius, int age, int lifetime,
                     long creationOrder, UUID ownerUuid, ResourceLocation typeId, int color, long shapeSeed,
                     int revision) {
            this(slot, generation, position, radius, age, lifetime, creationOrder, ownerUuid, typeId,
                    color, shapeSeed, revision, 0, 0);
        }
        public Entry(int slot, int generation, Vec3 position, double radius, int age, int lifetime,
                     long creationOrder, UUID ownerUuid, ResourceLocation typeId, int color, long shapeSeed) {
            this(slot, generation, position, radius, age, lifetime, creationOrder, ownerUuid, typeId,
                    color, shapeSeed, 0);
        }

        @Nullable
        public static Entry from(AbstractGroundEffect effect) {
            if (effect == null) return null;
            try {
                ResourceLocation type = effect.typeId();
                if (type == null) return null;
                Entry entry = new Entry(effect.slot(), effect.generation(), effect.position(), effect.radius(),
                        effect.age(), effect.lifetime(), effect.creationOrder(), effect.ownerUuid(), type,
                        effect.getColor(), effect.shapeSeed(), effect.revision(),
                        effect.animationDurationTicks(), effect.animationElapsedTicks());
                if (!entry.isValid()) {
                    IsaacDisaster.LOGGER.warn("Skipping invalid ground-effect snapshot: slot={}", effect.slot());
                    return null;
                }
                return entry;
            } catch (RuntimeException failure) {
                IsaacDisaster.LOGGER.warn("Skipping failed ground-effect snapshot: slot={}", effect.slot(), failure);
                return null;
            }
        }

        /** Exact metadata wire size; the client reconstructs the complete footprint from radius and seed. */
        public long serializedBytes() {
            // Valid ResourceLocation names consist solely of ASCII characters.
            if (!isValid()) return Long.MAX_VALUE;
            int typeBytes = typeId.getNamespace().length() + 1 + typeId.getPath().length();
            if (typeBytes > 32767) return Long.MAX_VALUE;
            return 53L + (ownerUuid == null ? 0 : 16)
                    + varIntBytes(slot) + varIntBytes(generation) + varIntBytes(age) + varIntBytes(lifetime)
                    + varIntBytes(revision) + varIntBytes(animationElapsedTicks)
                    + varIntBytes(animationDurationTicks) + varIntBytes(typeBytes) + typeBytes;
        }

        private static int varIntBytes(int value) {
            int bytes = 1;
            while ((value & ~127) != 0) { value >>>= 7; bytes++; }
            return bytes;
        }

        @Nullable
        public static Entry read(FriendlyByteBuf buf) {
            try {
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
                int duration = Math.max(0, buf.readVarInt());
                int elapsed = Math.max(0, buf.readVarInt());
                Entry entry = new Entry(slot, generation, position, radius, age, lifetime, order, owner, type,
                        color, seed, revision, duration, elapsed);
                return entry.isValid() ? entry : null;
            } catch (RuntimeException malformed) {
                return null;
            }
        }

        public void toBytes(FriendlyByteBuf buf) {
            if (!isValid()) {
                IsaacDisaster.LOGGER.warn("Ignoring invalid ground-effect snapshot encoding");
                return;
            }
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
            buf.writeVarInt(animationDurationTicks);
            buf.writeVarInt(animationElapsedTicks);
        }
    }
}

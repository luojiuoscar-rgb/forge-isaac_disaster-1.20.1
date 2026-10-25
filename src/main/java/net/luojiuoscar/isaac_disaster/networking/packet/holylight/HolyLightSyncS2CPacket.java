package net.luojiuoscar.isaac_disaster.networking.packet.holylight;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightSnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** A world-session reset, sparse clock sample, visible spawns or removals; never animation vertices. */
public record HolyLightSyncS2CPacket(int epoch, ResourceLocation dimension, long serverTick, boolean reset,
                                   List<HolyLightSnapshot> entries, List<Long> removed) {
    public static final int MAX_ENTRIES = 4096;
    public static final int MAX_REMOVED = 32768;

    public HolyLightSyncS2CPacket {
        entries = List.copyOf(entries);
        removed = List.copyOf(removed);
    }

    public HolyLightSyncS2CPacket(FriendlyByteBuf buffer) { this(decode(buffer)); }

    private HolyLightSyncS2CPacket(HolyLightSyncS2CPacket packet) {
        this(packet.epoch, packet.dimension, packet.serverTick, packet.reset, packet.entries, packet.removed);
    }

    private static HolyLightSyncS2CPacket decode(FriendlyByteBuf buffer) {
        try {
            int epoch = buffer.readVarInt();
            ResourceLocation dimension = buffer.readResourceLocation();
            long tick = buffer.readLong();
            boolean reset = buffer.readBoolean();
            int count = buffer.readVarInt();
            if (epoch <= 0 || tick < 0 || count < 0 || count > MAX_ENTRIES) throw new IllegalArgumentException("header");
            List<HolyLightSnapshot> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                var entry = new HolyLightSnapshot(buffer.readLong(),
                        new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                        buffer.readDouble(), buffer.readDouble(), buffer.readLong(),
                        buffer.readVarInt(), buffer.readBoolean(), buffer.readLong());
                if (!entry.valid() || entry.startTick() > tick) throw new IllegalArgumentException("snapshot");
                entries.add(entry);
            }
            int removalCount = buffer.readVarInt();
            if (removalCount < 0 || removalCount > MAX_REMOVED) throw new IllegalArgumentException("removals");
            List<Long> removed = new ArrayList<>(removalCount);
            for (int i = 0; i < removalCount; i++) {
                long id = buffer.readLong();
                if (id <= 0) throw new IllegalArgumentException("identity");
                removed.add(id);
            }
            if (buffer.isReadable()) throw new IllegalArgumentException("trailing bytes");
            return new HolyLightSyncS2CPacket(epoch, dimension, tick, reset, entries, removed);
        } catch (RuntimeException malformed) {
            if (buffer != null) buffer.readerIndex(buffer.writerIndex());
            IsaacDisaster.LOGGER.warn("Discarding malformed holy-light snapshot: {}", malformed.toString());
            return new HolyLightSyncS2CPacket(0, Level.OVERWORLD.location(), 0, false, List.of(), List.of());
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(epoch);
        buffer.writeResourceLocation(dimension);
        buffer.writeLong(serverTick);
        buffer.writeBoolean(reset);
        buffer.writeVarInt(entries.size());
        for (var entry : entries) {
            buffer.writeLong(entry.id());
            buffer.writeDouble(entry.position().x);
            buffer.writeDouble(entry.position().y);
            buffer.writeDouble(entry.position().z);
            buffer.writeDouble(entry.radius());
            buffer.writeDouble(entry.beamHeight());
            buffer.writeLong(entry.startTick());
            buffer.writeVarInt(entry.warningTicks());
            buffer.writeBoolean(entry.grounded());
            buffer.writeLong(entry.seed());
        }
        buffer.writeVarInt(removed.size());
        removed.forEach(buffer::writeLong);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        if (epoch > 0) DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleHolyLight(this)));
        context.setPacketHandled(true);
    }
}

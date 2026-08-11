package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.EntityVisualStateClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class EntityVisualStateS2CPacket {
    private final int entityId;
    private final UUID entityUuid;
    private final List<ResourceLocation> freezeSources;
    private final List<ResourceLocation> activeVisualLayers;

    public EntityVisualStateS2CPacket(int entityId, UUID entityUuid,
                                      Iterable<ResourceLocation> freezeSources,
                                      Iterable<ResourceLocation> activeVisualLayers) {
        this.entityId = entityId;
        this.entityUuid = entityUuid;
        this.freezeSources = copy(freezeSources);
        this.activeVisualLayers = copy(activeVisualLayers);
    }

    public EntityVisualStateS2CPacket(FriendlyByteBuf buffer) {
        entityId = buffer.readVarInt();
        entityUuid = buffer.readUUID();
        freezeSources = readLocations(buffer);
        activeVisualLayers = readLocations(buffer);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeUUID(entityUuid);
        writeLocations(buffer, freezeSources);
        writeLocations(buffer, activeVisualLayers);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> EntityVisualStateClient.apply(entityId, entityUuid,
                        freezeSources, activeVisualLayers)));
        context.setPacketHandled(true);
        return true;
    }

    private static List<ResourceLocation> copy(Iterable<ResourceLocation> locations) {
        List<ResourceLocation> result = new ArrayList<>();
        locations.forEach(result::add);
        return List.copyOf(result);
    }

    private static void writeLocations(FriendlyByteBuf buffer, List<ResourceLocation> locations) {
        buffer.writeVarInt(locations.size());
        locations.forEach(buffer::writeResourceLocation);
    }

    private static List<ResourceLocation> readLocations(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > 256) {
            throw new IllegalArgumentException("Invalid entity visual state size: " + size);
        }

        List<ResourceLocation> locations = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            locations.add(buffer.readResourceLocation());
        }
        return List.copyOf(locations);
    }
}

package net.luojiuoscar.isaac_disaster.networking.packet.laser;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Updates one persistent Revelation visual; the final damage pulse also ends its visual. */
public record RevelationBeamS2CPacket(UUID beamId, UUID ownerId, int ownerEntityId,
                                     ResourceLocation dimension, Vec3 start, Vec3 direction,
                                     float length, float width, boolean finished,
                                     boolean followingPlayer, boolean controllable) {
    public RevelationBeamS2CPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readUUID(), buf.readVarInt(), buf.readResourceLocation(),
                readVector(buf), readVector(buf), buf.readFloat(), buf.readFloat(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(beamId);
        buf.writeUUID(ownerId);
        buf.writeVarInt(ownerEntityId);
        buf.writeResourceLocation(dimension);
        writeVector(buf, start);
        writeVector(buf, direction);
        buf.writeFloat(length);
        buf.writeFloat(width);
        buf.writeBoolean(finished);
        buf.writeBoolean(followingPlayer);
        buf.writeBoolean(controllable);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleRevelationBeam(this)));
        context.setPacketHandled(true);
    }

    private static Vec3 readVector(FriendlyByteBuf buf) {
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    private static void writeVector(FriendlyByteBuf buf, Vec3 vector) {
        buf.writeDouble(vector.x);
        buf.writeDouble(vector.y);
        buf.writeDouble(vector.z);
    }
}

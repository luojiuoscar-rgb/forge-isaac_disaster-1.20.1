package net.luojiuoscar.isaac_disaster.networking.packet.groundeffect;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** Explicit world-session start or a sparse clock-only calibration. */
public record GroundEffectSyncS2CPacket(int epoch, ResourceLocation dimension, int serverTick, boolean reset) {
    public GroundEffectSyncS2CPacket {
        if (dimension == null) {
            IsaacDisaster.LOGGER.warn("Ignoring ground-effect session without a dimension");
            epoch = 0;
            dimension = Level.OVERWORLD.location();
            reset = false;
        }
    }

    public GroundEffectSyncS2CPacket(FriendlyByteBuf buffer) { this(decode(buffer)); }

    private GroundEffectSyncS2CPacket(GroundEffectSyncS2CPacket decoded) {
        this(decoded.epoch(), decoded.dimension(), decoded.serverTick(), decoded.reset());
    }

    private static GroundEffectSyncS2CPacket decode(FriendlyByteBuf buffer) {
        return GroundEffectPacketCodec.decode(buffer, input -> new GroundEffectSyncS2CPacket(
                input.readVarInt(), input.readResourceLocation(), input.readVarInt(), input.readBoolean()),
                () -> new GroundEffectSyncS2CPacket(0, Level.OVERWORLD.location(), 0, false));
    }
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(epoch);
        buffer.writeResourceLocation(dimension);
        buffer.writeVarInt(serverTick);
        buffer.writeBoolean(reset);
    }
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleGroundEffectSync(this)));
        context.setPacketHandled(true);
    }
}

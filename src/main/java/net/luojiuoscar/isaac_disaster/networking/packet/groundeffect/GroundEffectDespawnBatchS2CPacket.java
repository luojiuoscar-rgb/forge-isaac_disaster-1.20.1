package net.luojiuoscar.isaac_disaster.networking.packet.groundeffect;

import net.luojiuoscar.isaac_disaster.client.network.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;

public final class GroundEffectDespawnBatchS2CPacket {
    private final int epoch;
    private final int serverTick;
    private final boolean visibilityExit;
    private final List<Long> identities;

    public GroundEffectDespawnBatchS2CPacket(int epoch, int serverTick, boolean visibilityExit, List<Long> identities) {
        this.epoch = epoch;
        this.serverTick = serverTick;
        this.visibilityExit = visibilityExit;
        this.identities = identities == null || identities.stream().anyMatch(id -> id == null) ? List.of() : List.copyOf(identities);
    }

    public GroundEffectDespawnBatchS2CPacket(FriendlyByteBuf buf) {
        this(decode(buf));
    }

    private GroundEffectDespawnBatchS2CPacket(GroundEffectDespawnBatchS2CPacket decoded) {
        this(decoded.epoch(), decoded.serverTick(), decoded.visibilityExit(), decoded.identities());
    }

    private static GroundEffectDespawnBatchS2CPacket decode(FriendlyByteBuf buf) {
        return GroundEffectPacketCodec.decode(buf, input -> {
            int epoch = input.readVarInt();
            int tick = input.readVarInt();
            boolean exit = input.readBoolean();
            int count = input.readVarInt();
            if (count < 0 || count > input.readableBytes() / Long.BYTES) return null;
            List<Long> identities = new ArrayList<>(count);
            for (int i = 0; i < count; i++) identities.add(input.readLong());
            return new GroundEffectDespawnBatchS2CPacket(epoch, tick, exit, identities);
        }, () -> new GroundEffectDespawnBatchS2CPacket(0, 0, false, List.of()));
    }

    public int epoch() { return epoch; }
    public int serverTick() { return serverTick; }
    public boolean visibilityExit() { return visibilityExit; }
    public List<Long> identities() { return identities; }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(epoch);
        buf.writeVarInt(serverTick);
        buf.writeBoolean(visibilityExit);
        buf.writeCollection(identities, FriendlyByteBuf::writeLong);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> context.enqueueWork(() -> ClientPacketHandlers.handleGroundEffectDespawn(this)));
        context.setPacketHandled(true);
    }
}

package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.client.flight.IsaacFlightClientController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Confirms server-owned Isaac-flight permission and thrust state to the local client. */
public class IsaacFlightStateS2CPacket {
    private final boolean enabled;
    private final boolean thrusting;
    private final double speedMultiplier;
    private final double absoluteSpeedCap;

    public IsaacFlightStateS2CPacket(boolean enabled, boolean thrusting, double speedMultiplier, double absoluteSpeedCap) {
        this.enabled = enabled;
        this.thrusting = thrusting;
        this.speedMultiplier = speedMultiplier;
        this.absoluteSpeedCap = absoluteSpeedCap;
    }

    public IsaacFlightStateS2CPacket(FriendlyByteBuf buf) {
        this.enabled = buf.readBoolean();
        this.thrusting = buf.readBoolean();
        this.speedMultiplier = buf.readDouble();
        this.absoluteSpeedCap = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(enabled);
        buf.writeBoolean(thrusting);
        buf.writeDouble(speedMultiplier);
        buf.writeDouble(absoluteSpeedCap);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> IsaacFlightClientController.applyServerState(enabled, thrusting,
                        speedMultiplier, absoluteSpeedCap)));
        context.setPacketHandled(true);
        return true;
    }
}

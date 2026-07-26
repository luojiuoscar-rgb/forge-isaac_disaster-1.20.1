package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.system.flight.IsaacFlightController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sends only the current jump-held state; the server calculates all movement. */
public class IsaacFlightInputC2SPacket {
    private final boolean thrusting;

    public IsaacFlightInputC2SPacket(boolean thrusting) {
        this.thrusting = thrusting;
    }

    public IsaacFlightInputC2SPacket(FriendlyByteBuf buf) {
        this.thrusting = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(thrusting);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                IsaacFlightController.receiveInput(player, thrusting, player.level().getGameTime());
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}

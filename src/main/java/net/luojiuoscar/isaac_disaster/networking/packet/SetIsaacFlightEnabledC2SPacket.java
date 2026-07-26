package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.capability.player.flight.PlayerIsaacFlightProvider;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.system.flight.IsaacFlightController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Requests a persistent toggle of the Isaac-flight controller. */
public class SetIsaacFlightEnabledC2SPacket {
    public SetIsaacFlightEnabledC2SPacket() {
    }

    public SetIsaacFlightEnabledC2SPacket(FriendlyByteBuf ignored) {
    }

    public void toBytes(FriendlyByteBuf ignored) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            player.getCapability(PlayerIsaacFlightProvider.PLAYER_ISAAC_FLIGHT).ifPresent(state -> {
                state.setEnabled(!state.isEnabled());
                if (!state.isEnabled()) IsaacFlightController.stopThrust(player);
                player.displayClientMessage(Component.literal("以撒飞行：" + (state.isEnabled() ? "已启用" : "已禁用")), true);
                ModMessages.sentToPlayer(new IsaacFlightStateS2CPacket(state.isEnabled(), state.isThrusting(),
                        Config.FLIGHT_SPEED_MULTIPLIER.get(), Config.FLIGHT_ABSOLUTE_SPEED_CAP.get()), player);
            });
        });
        context.setPacketHandled(true);
        return true;
    }
}

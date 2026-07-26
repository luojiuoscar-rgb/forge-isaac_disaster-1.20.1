package net.luojiuoscar.isaac_disaster.client.flight;

import net.luojiuoscar.isaac_disaster.client.ModKeyMappings;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.IsaacFlightInputC2SPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.SetIsaacFlightEnabledC2SPacket;
import net.luojiuoscar.isaac_disaster.capability.player.flight.PlayerIsaacFlightProvider;
import net.luojiuoscar.isaac_disaster.system.flight.IsaacFlightController;
import net.luojiuoscar.isaac_disaster.system.flight.IsaacFlightInput;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/** Handles local jump edges, input heartbeats, prediction, and the flight toggle key. */
public final class IsaacFlightClientController {
    private static final IsaacFlightInput INPUT = new IsaacFlightInput();
    private static boolean sentThrusting;
    private static int heartbeat;

    private IsaacFlightClientController() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            resetRuntimeInput();
            return;
        }

        boolean jumpDown = minecraft.options.keyJump.isDown();
        if (!IsaacFlightController.getState(player).map(state -> state.isThrusting()).orElse(false)) {
            sentThrusting = false;
            heartbeat = 0;
        }
        boolean airbornePress = INPUT.update(player, jumpDown);
        boolean canRun = IsaacFlightController.canRun(player);

        if (airbornePress && canRun) {
            sendInput(true);
            IsaacFlightController.beginThrust(player, player.level().getGameTime());
        } else if (sentThrusting && !canRun) {
            sendInput(false);
            IsaacFlightController.stopThrust(player);
        } else if (!jumpDown && sentThrusting) {
            sendInput(false);
            IsaacFlightController.stopThrust(player);
        } else if (player.onGround() && sentThrusting) {
            sendInput(false);
            IsaacFlightController.stopThrust(player);
        }

        if (sentThrusting && ++heartbeat >= 4) {
            heartbeat = 0;
            ModMessages.sendToServer(new IsaacFlightInputC2SPacket(true));
        }
        IsaacFlightController.tickClient(player);
    }

    public static void toggleEnabled() {
        ModMessages.sendToServer(new SetIsaacFlightEnabledC2SPacket());
    }

    public static void applyServerState(boolean enabled, boolean thrusting, double speedMultiplier, double absoluteSpeedCap) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        player.getCapability(PlayerIsaacFlightProvider.PLAYER_ISAAC_FLIGHT).ifPresent(state -> {
            state.setEnabled(enabled);
            state.setFlightTuning(speedMultiplier, absoluteSpeedCap);
            sentThrusting = thrusting;
            heartbeat = 0;
            if (thrusting) {
                IsaacFlightController.beginThrust(player, player.level().getGameTime());
            } else {
                IsaacFlightController.stopThrust(player);
            }
        });
    }

    /** Clears local-only key edge and packet heartbeat state after a world is left. */
    public static void resetRuntimeInput() {
        INPUT.reset();
        sentThrusting = false;
        heartbeat = 0;
    }

    private static void sendInput(boolean thrusting) {
        sentThrusting = thrusting;
        heartbeat = 0;
        ModMessages.sendToServer(new IsaacFlightInputC2SPacket(thrusting));
    }
}

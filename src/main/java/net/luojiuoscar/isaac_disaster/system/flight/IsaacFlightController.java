package net.luojiuoscar.isaac_disaster.system.flight;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.capability.player.flight.PlayerIsaacFlight;
import net.luojiuoscar.isaac_disaster.capability.player.flight.PlayerIsaacFlightProvider;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.FlightHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.IsaacFlightStateS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/** Owns the shared Isaac-flight state transitions and movement calculation. */
public final class IsaacFlightController {
    public static final int INPUT_TIMEOUT_TICKS = 10;
    private static final double STEERING_FACTOR = 0.2D;

    private IsaacFlightController() {
    }

    public static Optional<PlayerIsaacFlight> getState(Player player) {
        return player.getCapability(PlayerIsaacFlightProvider.PLAYER_ISAAC_FLIGHT)
                .resolve();
    }

    public static boolean canRun(Player player) {
        if (player.isCreative() || player.isSpectator() || player.isDeadOrDying()
                || player.isPassenger() || player.isSleeping() || player.isFallFlying()
                || player.getAbilities().flying || !FlightHelper.hasIsaacFlightSource(player)) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.isChangingDimension()) {
            return false;
        }
        return getState(player).map(PlayerIsaacFlight::isEnabled).orElse(false);
    }

    public static void beginThrust(Player player, long gameTime) {
        getState(player).ifPresent(state -> {
            if (!canRun(player)) return;
            state.setThrusting(true);
            state.markInput(gameTime);
            acquirePose(player, state);
            player.setNoGravity(true);
        });
    }

    public static void receiveInput(Player player, boolean thrusting, long gameTime) {
        if (thrusting) {
            beginThrust(player, gameTime);
        } else {
            stopThrust(player);
        }
    }

    public static void stopThrust(Player player) {
        getState(player).ifPresent(state -> {
            state.setThrusting(false);
            state.markInput(Long.MIN_VALUE);
            releasePose(player, state);
            player.setNoGravity(false);
            player.resetFallDistance();
        });
    }

    public static boolean tickServer(ServerPlayer player) {
        Optional<PlayerIsaacFlight> optional = getState(player);
        if (optional.isEmpty()) return false;
        PlayerIsaacFlight state = optional.get();
        if (!state.isThrusting()) return false;

        long now = player.level().getGameTime();
        if (now - state.getInputLastSeenTick() > INPUT_TIMEOUT_TICKS || !canRun(player)) {
            stopThrust(player);
            sendStoppedState(player, state);
            return false;
        }

        applyVelocity(player, state);
        player.resetFallDistance();

        if (player.getEffect(ModEffects.TRANSCENDENCE.get()) == null) {
            player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(stats -> {
                stats.addCurrentFlyTime(player, 1);
                if (stats.getFlyTimeCurrent() >= PlayerHelper.getFly(player)) {
                    stopThrust(player);
                    sendStoppedState(player, state);
                }
            });
        }
        return state.isThrusting();
    }

    public static void tickClient(Player player) {
        Optional<PlayerIsaacFlight> optional = getState(player);
        if (optional.map(PlayerIsaacFlight::isThrusting).orElse(false) && canRun(player)) {
            applyVelocity(player, optional.get());
            player.resetFallDistance();
        } else if (optional.map(PlayerIsaacFlight::isThrusting).orElse(false)) {
            stopThrust(player);
        }
    }

    public static void resetTransientState(Player player) {
        getState(player).ifPresent(state -> {
            state.setThrusting(false);
            state.markInput(Long.MIN_VALUE);
            releasePose(player, state);
            player.setNoGravity(false);
        });
    }

    private static void applyVelocity(Player player, PlayerIsaacFlight state) {
        player.setNoGravity(true);
        double movementSpeed = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        double speedMultiplier = player.level().isClientSide
                ? state.getSpeedMultiplier()
                : Config.FLIGHT_SPEED_MULTIPLIER.get();
        double absoluteSpeedCap = player.level().isClientSide
                ? state.getAbsoluteSpeedCap()
                : Config.FLIGHT_ABSOLUTE_SPEED_CAP.get();
        double speedCap = IsaacFlightRules.calculateSpeedCap(
                movementSpeed,
                speedMultiplier,
                absoluteSpeedCap
        );
        Vec3 nextVelocity = IsaacFlightRules.calculateNextVelocity(
                player.getDeltaMovement(),
                player.getLookAngle(),
                speedCap,
                STEERING_FACTOR
        );
        player.setDeltaMovement(nextVelocity);
        player.hasImpulse = true;
    }

    private static void acquirePose(Player player, PlayerIsaacFlight state) {
        if (state.isPoseOwned() || player.getForcedPose() != null) return;
        player.setForcedPose(Pose.FALL_FLYING);
        state.setPoseOwned(true);
    }

    private static void releasePose(Player player, PlayerIsaacFlight state) {
        if (!state.isPoseOwned()) return;
        if (player.getForcedPose() == Pose.FALL_FLYING) {
            player.setForcedPose(null);
        }
        state.setPoseOwned(false);
    }

    private static void sendStoppedState(ServerPlayer player, PlayerIsaacFlight state) {
        ModMessages.sentToPlayer(new IsaacFlightStateS2CPacket(state.isEnabled(), false,
                Config.FLIGHT_SPEED_MULTIPLIER.get(), Config.FLIGHT_ABSOLUTE_SPEED_CAP.get()), player);
    }

}

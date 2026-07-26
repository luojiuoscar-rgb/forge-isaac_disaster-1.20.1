package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.flight.PlayerIsaacFlightProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.helper.FlightHelper;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.IsaacFlightStateS2CPacket;
import net.luojiuoscar.isaac_disaster.system.flight.IsaacFlightController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Owns server lifecycle hooks for the custom flight controller. */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IsaacFlightEvents {
    private IsaacFlightEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()
                || !(event.player instanceof ServerPlayer player)) return;
        boolean thrusting = IsaacFlightController.tickServer(player);
        if (!thrusting && player.tickCount % 4 == 0
                && FlightHelper.hasIsaacFlightSource(player)
                && player.getEffect(net.luojiuoscar.isaac_disaster.effect.ModEffects.TRANSCENDENCE.get()) == null) {
            player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                    stats -> stats.addCurrentFlyTime(player, -1));
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            IsaacFlightController.resetTransientState(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && FlightHelper.hasIsaacFlightSource(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        sendState(player);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        IsaacFlightController.resetTransientState(player);
        sendState(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            IsaacFlightController.resetTransientState(player);
            sendState(player);
        }
    }

    public static void sendState(ServerPlayer player) {
        player.getCapability(PlayerIsaacFlightProvider.PLAYER_ISAAC_FLIGHT).ifPresent(
                state -> ModMessages.sentToPlayer(
                        new IsaacFlightStateS2CPacket(state.isEnabled(), state.isThrusting(),
                                Config.FLIGHT_SPEED_MULTIPLIER.get(), Config.FLIGHT_ABSOLUTE_SPEED_CAP.get()), player));
    }
}

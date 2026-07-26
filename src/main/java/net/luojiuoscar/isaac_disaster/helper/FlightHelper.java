package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.minecraft.world.entity.player.Player;

/** Provides read-only checks for Isaac flight sources. */
public final class FlightHelper {
    private FlightHelper() {
    }

    public static boolean hasIsaacFlightSource(Player player) {
        return PlayerHelper.canFly(player)
                || player.hasEffect(ModEffects.TRANSCENDENCE.get());
    }
}

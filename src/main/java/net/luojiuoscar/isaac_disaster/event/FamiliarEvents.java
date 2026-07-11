package net.luojiuoscar.isaac_disaster.event;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.helper.FamiliarHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server-side maintenance hooks for Isaac familiar entities.
 */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FamiliarEvents {
    private static final int RECONCILE_INTERVAL = 20;

    /**
     * Maintains existing familiars periodically and admits at most one queued familiar each player tick.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side.isClient()) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!player.isAlive()) return;

        if (player.tickCount % RECONCILE_INTERVAL == 0) {
            FamiliarHelper.maintainExistingFamiliars(player);
        }
        FamiliarHelper.spawnNextMissingFamiliar(player);
    }

    /**
     * Removes runtime familiar entities immediately when their owner dies while preserving requirements.
     */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FamiliarHelper.discardAllRuntimeFamiliars(player);
        }
    }
}

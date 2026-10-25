package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.networking.packet.holylight.HolyLightSyncS2CPacket;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightPhase;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Client world ownership, presentation cues and server snapshot bridge. */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, value = Dist.CLIENT)
public final class ClientHolyLightRuntime {
    public static final ClientHolyLightRuntime INSTANCE = new ClientHolyLightRuntime();
    private final HolyLightClientState state = new HolyLightClientState();
    private final Map<Long, Visual> visuals = new HashMap<>();
    private ClientLevel world;

    private ClientHolyLightRuntime() { }

    public void apply(HolyLightSyncS2CPacket packet) {
        useWorld(Minecraft.getInstance().level);
        if (world == null || !world.dimension().location().equals(packet.dimension())) return;
        if (!state.synchronize(packet.epoch(), packet.reset(), packet.serverTick())) return;
        discardExpiredVisuals();
        for (HolyLightSnapshot snapshot : packet.entries()) {
            HolyLightClientState.Entry entry = state.add(snapshot.id(), snapshot.startTick(),
                    snapshot.warningTicks(), snapshot.grounded());
            if (entry != null) visuals.computeIfAbsent(snapshot.id(), ignored -> new Visual(snapshot));
        }
        for (long id : packet.removed()) {
            state.remove(id);
            Visual removed = visuals.remove(id);
            if (removed != null) stopWarning(removed);
        }
        presentCues();
    }

    Collection<Visual> visuals() { return visuals.values(); }
    HolyLightClientState.Entry entry(long id) { return state.get(id); }

    double renderTime(float partialTick) {
        return state.renderTime(partialTick, Minecraft.getInstance().isPaused());
    }

    public void clear() {
        visuals.values().forEach(ClientHolyLightRuntime::stopWarning);
        state.clear();
        visuals.clear();
        world = null;
    }

    private void useWorld(ClientLevel level) {
        if (world == level) return;
        visuals.values().forEach(ClientHolyLightRuntime::stopWarning);
        state.clearForWorldUnload();
        visuals.clear();
        world = level;
        state.useDimension(level == null ? null : level.dimension().location().toString());
    }

    private void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        useWorld(minecraft.level);
        if (world == null || minecraft.isPaused()) return;
        state.tick(false);
        discardExpiredVisuals();
        presentCues();
        for (Visual visual : visuals.values()) {
            var entry = state.get(visual.snapshot.id());
            if (entry.phase() != HolyLightPhase.WARNING || ((long) state.now() + entry.id()) % 4 != 0) continue;
            Vec3 position = visual.snapshot.position();
            double angle = world.random.nextDouble() * Math.PI * 2;
            double radius = visual.snapshot.radius() * (0.8D + world.random.nextDouble() * 0.4D);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            world.addParticle(ParticleTypes.END_ROD, position.x + x, position.y + 0.15D,
                    position.z + z, -x * 0.08D, 0.025D, -z * 0.08D);
        }
    }

    private void discardExpiredVisuals() {
        visuals.values().removeIf(visual -> {
            if (state.get(visual.snapshot.id()) != null) return false;
            stopWarning(visual);
            return true;
        });
    }

    private static void stopWarning(Visual visual) {
        if (visual.warningSound == null) return;
        visual.warningSound.cancel();
        Minecraft.getInstance().getSoundManager().stop(visual.warningSound);
        visual.warningSound = null;
    }

    private void presentCues() {
        if (world == null) return;
        for (Visual visual : visuals.values()) {
            var entry = state.get(visual.snapshot.id());
            Vec3 position = visual.snapshot.position();
            if (visual.warningSound != null && !visual.warningSound.canPlaySound()) stopWarning(visual);
            if (entry.takeWarningCue()) {
                visual.warningSound = new HolyLightWarningSound(visual.snapshot, state);
                Minecraft.getInstance().getSoundManager().play(visual.warningSound);
            }
            if (entry.takeImpactCue()) {
                world.addParticle(ParticleTypes.FLASH, position.x, position.y + 0.15D, position.z, 0, 0, 0);
                for (int i = 0; i < 6; i++) {
                    double angle = i * Math.PI / 3 + (visual.snapshot.seed() & 15) * 0.2D;
                    world.addParticle(ParticleTypes.END_ROD, position.x, position.y + 0.1D, position.z,
                            Math.cos(angle) * 0.12D, 0.08D + i * 0.012D, Math.sin(angle) * 0.12D);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) INSTANCE.tick();
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        INSTANCE.clear();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() == INSTANCE.world) INSTANCE.useWorld(null);
    }

    static final class Visual {
        final HolyLightSnapshot snapshot;
        final AABB bounds;
        final HolyLightScorchProjection scorch;
        HolyLightWarningSound warningSound;

        Visual(HolyLightSnapshot snapshot) {
            this.snapshot = snapshot;
            Vec3 position = snapshot.position();
            double extent = snapshot.radius() * 1.3D;
            bounds = new AABB(position.x - extent, position.y - 1.0D, position.z - extent,
                    position.x + extent, position.y + snapshot.beamHeight(), position.z + extent);
            scorch = snapshot.grounded() ? new HolyLightScorchProjection(snapshot) : null;
        }
    }
}

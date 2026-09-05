package net.luojiuoscar.isaac_disaster.bullet.client;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletSteeringMode;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletCorrectionS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletSpawnS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletTrackingBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletShatterS2CPacket;
import net.luojiuoscar.isaac_disaster.client.particle.TearShatterParticles;
import net.luojiuoscar.isaac_disaster.registries.attack_type.BulletSourceType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraft.client.Minecraft;

/** Client stream facade used by network handlers and a future batched renderer. */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, value = Dist.CLIENT)
public final class ClientBulletRuntime {
    public static final ClientBulletRuntime INSTANCE = new ClientBulletRuntime();
    private final BulletStream stream = new BulletStream();
    private ClientBulletRuntime() {}
    /** Removes a stream entry encoded as generation high bits and slot low bits. */
    public void despawn(long identity) { stream.despawn((int) identity, (int) (identity >>> 32)); }
    /** Accepts a lifecycle packet epoch before applying a removal or correction. */
    public boolean acceptEpoch(int epoch) { return stream.acceptEpoch(epoch); }
    /** Advances client prediction for all stream entries. */
    public void tick() { stream.tick(); }
    /** Decodes a compact spawn packet into the local prediction stream. */
    public void spawn(BulletSpawnS2CPacket packet) {
        if (!stream.acceptEpoch(packet.epoch())) return;
        BulletState state = stream.spawn(packet.slot(), packet.generation(), packet.snapshotTick(), BulletState.builder()
                .position(packet.position()).velocity(packet.velocity()).acceleration(packet.acceleration()).baseSpeed(packet.baseSpeed())
                .lifetime(packet.lifetime()).range(packet.range()).damage(packet.damage()).renderScale(packet.renderScale())
                .collisionWidth(packet.collisionWidth()).collisionHeight(packet.collisionHeight())
                .ownerUuid(packet.ownerUuid()).color(packet.color()).alpha(packet.alpha())
                .visualIds(new java.util.HashSet<>(packet.visualIds())).sourceType(packet.fetus() ? BulletSourceType.FETUS_BULLET : BulletSourceType.TEAR_BULLET)
                .steeringMode(packet.fetus() ? BulletSteeringMode.DIRECT : BulletSteeringMode.LIMITED)
                .homing(packet.homing()).controllable(packet.controllable()).homingRange(packet.homingRange())
                .homingSteer(packet.homingSteer()).controlRange(packet.controlRange()).controlSteer(packet.controlSteer()).build());

        if (state == null) return;
        state.restoreSnapshot(packet.previousPosition(), packet.age(), packet.traveled());
    }
    /** Applies a sparse server correction through the stream's blend policy. */
    public void correct(BulletCorrectionS2CPacket packet) {
        if (!stream.acceptEpoch(packet.epoch())) return;
        stream.correct(packet.slot(), packet.generation(), packet.position(), packet.velocity(), packet.blend());
    }
    /** Applies a server-selected shared target sample without resolving any client entities. */
    public void applyTracking(BulletTrackingBatchS2CPacket packet) {
        stream.applyTrackingVelocity(packet.epoch(), packet.sampleTick(), packet.targets(), packet.velocitySamples(), packet.assignments());
    }
    /** Spawns lightweight shatter visuals after a server-authoritative death event. */
    public void applyShatter(BulletShatterS2CPacket packet) {
        if (!acceptEpoch(packet.epoch()) || Minecraft.getInstance().level == null) return;
        for (BulletShatterS2CPacket.Entry entry : packet.entries()) {
            TearShatterParticles.spawn(Minecraft.getInstance().level, entry.position(), entry.velocity(), entry.scale(),
                    entry.color(), entry.alpha(), entry.visualIds(), entry.fetus());
        }
}
    /** Clears stale stream state when a session ends. */
    public void clear() { stream.clear(); }

    /** Clears world-owned prediction state but keeps the epoch watermark across transitions. */
    public void clearForWorldUnload() { stream.clearForWorldUnload(); }

    public BulletStream stream() { return stream; }

    /** Advances client-side prediction once per client tick. */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != ClientTickEvent.Phase.END) return;
        if (Minecraft.getInstance().level == null) INSTANCE.clearForWorldUnload();
        else INSTANCE.tick();
    }

    /** Drops transient bullets before a new connection can reuse their slot identities. */
    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) { INSTANCE.clear(); }

    /** Drops transient bullets when a client world is unloaded without a network logout. */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) INSTANCE.clearForWorldUnload();
    }
}

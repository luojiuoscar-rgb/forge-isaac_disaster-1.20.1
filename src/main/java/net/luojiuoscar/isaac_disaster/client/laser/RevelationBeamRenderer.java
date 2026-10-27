package net.luojiuoscar.isaac_disaster.client.laser;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.RevelationBeamS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.RevelationAttack;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client.HolyLightVisuals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Client-owned finite beam visuals. Damage remains entirely in RevelationAttack. */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, value = Dist.CLIENT)
public final class RevelationBeamRenderer {
    private static final int IDLE_TIMEOUT_TICKS = 100;
    private static final double RING_OFFSET = .8D;
    private static final Map<UUID, BeamState> BEAMS = new HashMap<>();
    private static final List<BeamFrame> FRAMES = new ArrayList<>();
    private static ClientLevel world;
    private static long clientTicks;

    private RevelationBeamRenderer() { }

    public static void update(RevelationBeamS2CPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        useWorld(level);
        if (level == null || !level.dimension().location().equals(packet.dimension())
                || !finite(packet.start()) || !finite(packet.direction())
                || packet.direction().lengthSqr() < 1.0E-12D
                || !Float.isFinite(packet.length()) || packet.length() <= 0
                || !Float.isFinite(packet.width()) || packet.width() <= 0 || packet.ageTicks() < 0) return;
        long now = clientTicks;
        BEAMS.computeIfAbsent(packet.beamId(), ignored -> new BeamState(packet, now)).update(packet, now);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        useWorld(level);
        if (level != null && !minecraft.isPaused()) {
            clientTicks++;
            BEAMS.values().removeIf(beam -> beam.expiresAt <= clientTicks);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        useWorld(null);
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() == world) useWorld(null);
    }

    private static void useWorld(ClientLevel level) {
        if (world != level) {
            BEAMS.clear();
            FRAMES.clear();
            clientTicks = 0;
            world = level;
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        useWorld(level);
        if (level == null || BEAMS.isEmpty()) return;
        float partial = minecraft.isPaused() ? 0 : event.getPartialTick();
        double now = clientTicks + partial;
        Vec3 camera = event.getCamera().getPosition();
        FRAMES.clear();
        for (BeamState beam : BEAMS.values()) {
            if (beam.expiresAt <= now) continue;
            BeamFrame frame = beam.frame(minecraft, level, camera, partial, now);
            Vec3 start = frame.start.add(camera);
            Vec3 end = start.add(frame.axis.scale(frame.length));
            AABB bounds = new AABB(start, end).inflate(frame.width * .8D);
            if (event.getFrustum().isVisible(bounds)) FRAMES.add(frame);
        }

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        RenderType light = HolyLightVisuals.lightRenderType();
        VertexConsumer vertices = buffers.getBuffer(light);
        for (BeamFrame frame : FRAMES) {
            drawBeam(vertices, frame);
            drawImpactRing(vertices, frame);
        }
        buffers.endBatch(light);
    }

    static final class BeamState {
        private final UUID ownerId;
        private final int ownerEntityId;
        private long bornAt;
        private Vec3 previousStart;
        private Vec3 start;
        private Vec3 previousDirection;
        private Vec3 direction;
        private float length;
        private float width;
        private long updatedAt;
        private long expiresAt;
        private boolean followingPlayer;
        private boolean controllable;
        private boolean finished;

        BeamState(RevelationBeamS2CPacket packet, long now) {
            ownerId = packet.ownerId();
            ownerEntityId = packet.ownerEntityId();
            bornAt = now - packet.ageTicks();
            previousStart = start = packet.start();
            previousDirection = direction = packet.direction().normalize();
            updatedAt = now;
        }

        void update(RevelationBeamS2CPacket packet, long now) {
            if (finished) return;
            double blend = blend(now);
            previousStart = previousStart.lerp(start, blend);
            previousDirection = interpolateDirection(previousDirection, direction, blend);
            start = packet.start();
            direction = packet.direction().normalize();
            // A delayed or duplicate snapshot must never restart expansion or the impact ring.
            bornAt = Math.min(bornAt, now - packet.ageTicks());
            length = packet.length();
            width = packet.width();
            updatedAt = now;
            finished = packet.finished();
            expiresAt = now + (finished ? HolyLightVisuals.BEAM_FADE_TICKS : IDLE_TIMEOUT_TICKS);
            followingPlayer = packet.followingPlayer();
            controllable = packet.controllable();
        }

        double age(double now) {
            return Math.max(0, now - bornAt);
        }

        double fadeAge(double now) {
            return finished ? Math.max(0, now - (expiresAt - HolyLightVisuals.BEAM_FADE_TICKS)) : -1;
        }

        private double blend(double now) {
            return Math.max(0, Math.min(1, (now - updatedAt) / 2.0D));
        }

        private BeamFrame frame(Minecraft minecraft, ClientLevel level, Vec3 camera,
                                float partial, double now) {
            Vec3 origin = previousStart.lerp(start, blend(now));
            Vec3 axis = interpolateDirection(previousDirection, direction, blend(now));
            Entity owner = level.getEntity(ownerEntityId);
            boolean liveOwner = followingPlayer && owner != null && ownerId.equals(owner.getUUID())
                    && owner.isAlive() && !owner.isRemoved();
            if (liveOwner) {
                origin = RevelationAttack.getBeamOrigin(owner, partial);
                if (controllable) axis = owner.getViewVector(partial).normalize();
            }
            Vec3 ringCenter = origin.add(axis.scale(RING_OFFSET));
            boolean firstPerson = liveOwner && owner == minecraft.player
                    && minecraft.options.getCameraType().isFirstPerson();
            double visibleLength = length;
            if (firstPerson) {
                double trim = Math.min(length * .5D, width * .75D + .4D);
                origin = origin.add(axis.scale(trim));
                visibleLength -= trim;
            }
            return new BeamFrame(origin.subtract(camera), axis, visibleLength, width,
                    age(now), fadeAge(now), ringCenter.subtract(camera));
        }
    }

    record BeamFrame(Vec3 start, Vec3 axis, double length, double width,
                     double age, double fadeAge, Vec3 ringCenter) { }

    private static Vec3 interpolateDirection(Vec3 from, Vec3 to, double fraction) {
        Vec3 result = from.lerp(to, fraction);
        return result.lengthSqr() < 1.0E-12D ? to : result.normalize();
    }

    private static boolean finite(Vec3 vector) {
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }

    static void drawBeam(VertexConsumer vertices, BeamFrame beam) {
        int row = HolyLightVisuals.firingRow(beam.age, beam.fadeAge);
        Vec3 end = beam.start.add(beam.axis.scale(beam.length));
        double envelope = Math.min(1, Math.max(0, beam.age / HolyLightVisuals.BEAM_EXPAND_TICKS));
        if (beam.fadeAge >= 0) {
            envelope *= Math.max(0, 1 - beam.fadeAge / HolyLightVisuals.BEAM_FADE_TICKS);
        }
        HolyLightVisuals.drawCylinder(vertices, beam.start, end, beam.axis, beam.width * .5D * envelope, row, 1);
        HolyLightVisuals.drawBeam(vertices, beam.start, end,
                HolyLightVisuals.billboardSide(beam.axis, beam.start), beam.width * .8D, row, 1);
    }

    static void drawImpactRing(VertexConsumer vertices, BeamFrame beam) {
        if (beam.age >= HolyLightVisuals.IMPACT_RING_TICKS) return;
        Vec3 right = HolyLightVisuals.perpendicular(beam.axis);
        HolyLightVisuals.drawRing(vertices, beam.ringCenter, right, beam.axis.cross(right).normalize(),
                beam.width * .8D, false, beam.age / HolyLightVisuals.IMPACT_RING_TICKS);
    }
}

package net.luojiuoscar.isaac_disaster.client.laser;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.RevelationBeamS2CPacket;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.RevelationAttack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
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
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Client-owned, finite beam visuals. Server snapshots remain authoritative after detachment. */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, value = Dist.CLIENT)
public final class RevelationBeamRenderer {
    private static final int SIDES = 12;
    private static final int GLOW_BANDS = 16;
    private static final int IDLE_TIMEOUT_TICKS = 100;
    private static final int FADE_TICKS = 2;
    private static final double GLOW_RADIUS = 2.5D;
    private static final double GLOW_WAVE = 0.1D;
    private static final double[] CIRCLE_COS = new double[SIDES + 1];
    private static final double[] CIRCLE_SIN = new double[SIDES + 1];
    private static final double[] BAND_POSITION = new double[GLOW_BANDS + 1];
    private static final float[] BAND_ALPHA = new float[GLOW_BANDS + 1];
    private static final Matrix4f POSE = new Matrix4f();
    private static final RenderType CORE = BeamStates.create(false);
    private static final RenderType GLOW = BeamStates.create(true);
    private static final Map<UUID, BeamState> BEAMS = new HashMap<>();
    private static final List<BeamFrame> FRAMES = new ArrayList<>();
    private static ClientLevel world;
    private static long clientTicks;

    static {
        for (int i = 0; i <= SIDES; i++) {
            double angle = i * Math.PI * 2 / SIDES;
            CIRCLE_COS[i] = Math.cos(angle);
            CIRCLE_SIN[i] = Math.sin(angle);
        }
        for (int i = 0; i <= GLOW_BANDS; i++) {
            double position = -1 + 2.0D * i / GLOW_BANDS;
            BAND_POSITION[i] = position;
            BAND_ALPHA[i] = glowAlpha(position, 1f);
        }
    }

    private RevelationBeamRenderer() { }

    public static void update(RevelationBeamS2CPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        useWorld(level);
        if (level == null || !level.dimension().location().equals(packet.dimension())
                || !finite(packet.start()) || !finite(packet.direction())
                || packet.direction().lengthSqr() < 1.0E-12D
                || !Float.isFinite(packet.length()) || packet.length() <= 0
                || !Float.isFinite(packet.width()) || packet.width() <= 0) return;
        if (packet.finished() && !BEAMS.containsKey(packet.beamId())) return;
        long now = clientTicks;
        BEAMS.computeIfAbsent(packet.beamId(), ignored -> new BeamState(packet, now)).update(packet, now);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ClientLevel level = Minecraft.getInstance().level;
        useWorld(level);
        if (level != null && !Minecraft.getInstance().isPaused()) {
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
        float partial = event.getPartialTick();
        double now = clientTicks + (minecraft.isPaused() ? 0 : partial);
        Vec3 camera = event.getCamera().getPosition();
        FRAMES.clear();
        for (BeamState beam : BEAMS.values()) {
            if (beam.expiresAt <= now) continue;
            BeamFrame frame = beam.frame(minecraft, level, camera, partial, now);
            if (frame.radius <= 0) continue;
            Vec3 start = frame.start.add(camera);
            Vec3 end = start.add(frame.axis.scale(frame.length));
            AABB bounds = new AABB(start, end).inflate(frame.radius * (GLOW_RADIUS + GLOW_WAVE));
            if (event.getFrustum().isVisible(bounds)) FRAMES.add(frame);
        }
        if (FRAMES.isEmpty()) return;

        // AFTER_WEATHER already has the world view installed; vertices are only camera-relative.
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer core = buffers.getBuffer(CORE);
        for (BeamFrame frame : FRAMES) drawCore(core, POSE, frame);
        buffers.endBatch(CORE);
        VertexConsumer glow = buffers.getBuffer(GLOW);
        for (BeamFrame frame : FRAMES) drawGlow(glow, POSE, frame);
        buffers.endBatch(GLOW);
    }

    private static final class BeamState {
        private final UUID ownerId;
        private final int ownerEntityId;
        private final long bornAt;
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

        private BeamState(RevelationBeamS2CPacket packet, long now) {
            ownerId = packet.ownerId();
            ownerEntityId = packet.ownerEntityId();
            bornAt = now;
            previousStart = start = packet.start();
            previousDirection = direction = packet.direction().normalize();
            updatedAt = now;
        }

        private void update(RevelationBeamS2CPacket packet, long now) {
            if (finished) return;
            double blend = blend(now);
            previousStart = previousStart.lerp(start, blend);
            previousDirection = interpolateDirection(previousDirection, direction, blend);
            start = packet.start();
            direction = packet.direction().normalize();
            length = packet.length();
            width = packet.width();
            updatedAt = now;
            finished = packet.finished();
            expiresAt = now + (finished ? FADE_TICKS : IDLE_TIMEOUT_TICKS);
            followingPlayer = packet.followingPlayer();
            controllable = packet.controllable();
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
            boolean firstPerson = liveOwner && owner == minecraft.player
                    && minecraft.options.getCameraType().isFirstPerson();
            double visibleLength = length;
            if (firstPerson) {
                double trim = Math.min(length * 0.5D, width * 0.75D + 0.4D);
                origin = origin.add(axis.scale(trim));
                visibleLength -= trim;
            }
            double envelope = Math.min(1, Math.min((now - bornAt) / 1.5D,
                    (expiresAt - now) / (double) FADE_TICKS));
            return new BeamFrame(origin.subtract(camera), axis, visibleLength,
                    width * 0.5D * Math.max(0, envelope), now - bornAt, firstPerson);
        }
    }

    private record BeamFrame(Vec3 start, Vec3 axis, double length, double radius,
                             double age, boolean firstPerson) { }

    private static Vec3 interpolateDirection(Vec3 from, Vec3 to, double fraction) {
        Vec3 result = from.lerp(to, fraction);
        return result.lengthSqr() < 1.0E-12D ? to : result.normalize();
    }

    private static boolean finite(Vec3 vector) {
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }

    private static Vec3 perpendicular(Vec3 axis) {
        Vec3 reference = Math.abs(axis.y) < 0.9D ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        return axis.cross(reference).normalize();
    }

    private static void drawCore(VertexConsumer vertices, Matrix4f pose, BeamFrame beam) {
        Vec3 side = perpendicular(beam.axis);
        Vec3 up = beam.axis.cross(side).normalize();
        Vec3 end = beam.start.add(beam.axis.scale(beam.length));
        for (int i = 0; i < SIDES; i++) {
            Vec3 a = radial(side, up, i, beam.radius);
            Vec3 b = radial(side, up, i + 1, beam.radius);
            quad(vertices, pose, beam.start.add(a), beam.start.add(b), end.add(b), end.add(a),
                    1f, 1f, 1f, 1f, 1f);
            quad(vertices, pose, beam.start, beam.start.add(b), beam.start.add(a), beam.start,
                    1f, 1f, 1f, 1f, 1f);
            quad(vertices, pose, end, end.add(a), end.add(b), end, 1f, 1f, 1f, 1f, 1f);
        }
    }

    private static void drawGlow(VertexConsumer vertices, Matrix4f pose, BeamFrame beam) {
        Vec3 side = beam.axis.cross(beam.start.scale(-1));
        side = side.lengthSqr() < 1.0E-12D ? perpendicular(beam.axis) : side.normalize();
        float strength = beam.firstPerson ? 0.18f : 0.32f;
        int slices = 8;
        for (int slice = 0; slice < slices; slice++) {
            double distanceA = beam.length * slice / slices;
            double distanceB = beam.length * (slice + 1) / slices;
            double radiusA = beam.radius * (GLOW_RADIUS + GLOW_WAVE * Math.sin(beam.age * 0.35D - distanceA * 0.3D));
            double radiusB = beam.radius * (GLOW_RADIUS + GLOW_WAVE * Math.sin(beam.age * 0.35D - distanceB * 0.3D));
            Vec3 a = beam.start.add(beam.axis.scale(distanceA));
            Vec3 b = beam.start.add(beam.axis.scale(distanceB));
            for (int band = 0; band < GLOW_BANDS; band++) {
                double t0 = BAND_POSITION[band];
                double t1 = BAND_POSITION[band + 1];
                quad(vertices, pose, a.add(side.scale(t0 * radiusA)), a.add(side.scale(t1 * radiusA)),
                        b.add(side.scale(t1 * radiusB)), b.add(side.scale(t0 * radiusB)),
                        1f, 0.86f, 0.55f, BAND_ALPHA[band] * strength, BAND_ALPHA[band + 1] * strength);
            }
        }
        if (!beam.firstPerson) drawSourceRing(vertices, pose, beam);
    }

    private static float glowAlpha(double distance, float strength) {
        double fade = Math.max(0, 1 - distance * distance);
        return (float) (strength * fade * fade);
    }

    private static void drawSourceRing(VertexConsumer vertices, Matrix4f pose, BeamFrame beam) {
        Vec3 side = perpendicular(beam.axis);
        Vec3 up = beam.axis.cross(side).normalize();
        double outer = beam.radius * (1.8D + 0.05D * Math.sin(beam.age * 0.4D));
        for (int i = 0; i < SIDES; i++) {
            quad(vertices, pose, beam.start.add(radial(side, up, i, beam.radius)),
                    beam.start.add(radial(side, up, i, outer)),
                    beam.start.add(radial(side, up, i + 1, outer)),
                    beam.start.add(radial(side, up, i + 1, beam.radius)),
                    1f, 0.9f, 0.65f, 0.35f, 0f);
        }
    }

    private static Vec3 radial(Vec3 side, Vec3 up, int index, double radius) {
        return side.scale(CIRCLE_COS[index] * radius).add(up.scale(CIRCLE_SIN[index] * radius));
    }

    private static void quad(VertexConsumer vertices, Matrix4f pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                             float red, float green, float blue, float alphaA, float alphaB) {
        vertex(vertices, pose, a, red, green, blue, alphaA);
        vertex(vertices, pose, b, red, green, blue, alphaB);
        vertex(vertices, pose, c, red, green, blue, alphaB);
        vertex(vertices, pose, d, red, green, blue, alphaA);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f pose, Vec3 point,
                               float red, float green, float blue, float alpha) {
        vertices.vertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .color(red, green, blue, alpha).endVertex();
    }

    private static final class BeamStates extends RenderStateShard {
        private BeamStates() { super("revelation_beam_state", () -> { }, () -> { }); }

        private static RenderType create(boolean glow) {
            return RenderType.create(IsaacDisaster.MOD_ID + (glow ? "_revelation_glow" : "_revelation_core"),
                    DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 4096, false, false,
                    RenderType.CompositeState.builder()
                            .setShaderState(POSITION_COLOR_SHADER)
                            .setTransparencyState(glow ? LIGHTNING_TRANSPARENCY : NO_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(glow ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                            .createCompositeState(false));
        }
    }
}

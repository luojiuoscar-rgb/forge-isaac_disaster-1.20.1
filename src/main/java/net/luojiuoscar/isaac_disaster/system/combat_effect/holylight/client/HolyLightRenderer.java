package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightPhase;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightSnapshot;
import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core.HolyLightTimeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/** Horizontal animated impact rings, a yaw billboard and collision-conforming scorch decals. */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, value = Dist.CLIENT)
public final class HolyLightRenderer {
    private static final int IMPACT_RING_TICKS = 4;
    private static final int BEAM_EXPAND_TICKS = 2;
    private static final int BEAM_FADE_TICKS = 2;
    private static final int BEAM_HOLD_ROW = 10;
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static final List<ClientHolyLightRuntime.Visual> VISIBLE = new ArrayList<>();

    private HolyLightRenderer() { }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;
        ClientHolyLightRuntime runtime = ClientHolyLightRuntime.INSTANCE;
        double now = runtime.renderTime(event.getPartialTick());
        Vec3 camera = event.getCamera().getPosition();
        VISIBLE.clear();
        for (var visual : runtime.visuals()) {
            var entry = runtime.entry(visual.snapshot.id());
            if (entry == null || visual.snapshot.phase(entry.age(now)) == HolyLightPhase.EXPIRED) continue;
            if (camera.distanceToSqr(visual.snapshot.position()) > 4096.0D) continue;
            if (event.getFrustum().isVisible(visual.bounds)) VISIBLE.add(visual);
        }
        if (VISIBLE.isEmpty()) return;

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer light = buffers.getBuffer(HolyLightRenderTypes.LIGHT);
        double yaw = Math.toRadians(event.getCamera().getYRot());
        double sideX = Math.cos(yaw);
        double sideZ = Math.sin(yaw);
        for (var visual : VISIBLE) {
            var entry = runtime.entry(visual.snapshot.id());
            double age = entry.age(now);
            HolyLightPhase phase = visual.snapshot.phase(age);
            if (phase != HolyLightPhase.WARNING && phase != HolyLightPhase.FIRING) continue;
            double phaseAge = visual.snapshot.phaseAge(age);
            drawRing(light, visual.snapshot, camera, phase, phaseAge);
            drawBeam(light, visual.snapshot, camera, phase, phaseAge, sideX, sideZ);
        }
        buffers.endBatch(HolyLightRenderTypes.LIGHT);

        VertexConsumer scorch = buffers.getBuffer(HolyLightRenderTypes.SCORCH);
        for (var visual : VISIBLE) {
            var entry = runtime.entry(visual.snapshot.id());
            double age = entry.age(now);
            float alpha = scorchAlpha(visual.snapshot, age);
            if (visual.scorch == null || alpha <= 0) continue;
            for (var face : visual.scorch.faces(minecraft.level)) drawScorch(scorch, visual.snapshot, face, camera, alpha);
        }
        buffers.endBatch(HolyLightRenderTypes.SCORCH);
    }

    private static float scorchAlpha(HolyLightSnapshot snapshot, double age) {
        if (!snapshot.grounded()) return 0;
        return switch (snapshot.phase(age)) {
            case FIRING -> 1;
            case SCORCH -> (float) Math.max(0, 1 - snapshot.phaseAge(age) / HolyLightTimeline.SCORCH_TICKS);
            default -> 0;
        };
    }

    private static void drawRing(VertexConsumer vertices, HolyLightSnapshot snapshot, Vec3 camera,
                                 HolyLightPhase phase, double age) {
        int duration = phase == HolyLightPhase.WARNING ? snapshot.warningTicks() : IMPACT_RING_TICKS;
        if (age >= duration) return;
        int frame = Math.min(10, (int) (age * 11 / duration));
        float u0 = (frame * 16 + 0.5F) / 256;
        float u1 = (frame * 16 + 15.5F) / 256;
        float v0 = (phase == HolyLightPhase.WARNING ? 0.5F : 16.5F) / 32;
        float v1 = (phase == HolyLightPhase.WARNING ? 15.5F : 31.5F) / 32;
        double x = snapshot.position().x - camera.x;
        double y = snapshot.position().y - camera.y + 0.0125D;
        double z = snapshot.position().z - camera.z;
        double radius = snapshot.radius();
        vertex(vertices, x - radius, y, z - radius, u0, v0, 1);
        vertex(vertices, x - radius, y, z + radius, u0, v1, 1);
        vertex(vertices, x + radius, y, z + radius, u1, v1, 1);
        vertex(vertices, x + radius, y, z - radius, u1, v0, 1);
    }

    private static void drawBeam(VertexConsumer vertices, HolyLightSnapshot snapshot, Vec3 camera,
                                 HolyLightPhase phase, double age, double sideX, double sideZ) {
        double halfWidth = phase == HolyLightPhase.WARNING
                ? 2.0D - 1.75D * Math.min(1, age / snapshot.warningTicks()) : 1.0D;
        halfWidth *= snapshot.radius() / 1.6D;
        double dx = sideX * halfWidth;
        double dz = sideZ * halfWidth;
        double x = snapshot.position().x - camera.x;
        double y = snapshot.position().y - camera.y + 0.016D;
        double z = snapshot.position().z - camera.z;
        float u0 = 224.5F / 256;
        float u1 = 255.5F / 256;
        int row = 0;
        if (phase == HolyLightPhase.FIRING) {
            double fadeStart = HolyLightTimeline.FIRING_TICKS - BEAM_FADE_TICKS;
            if (age < BEAM_EXPAND_TICKS) {
                row = Math.min(BEAM_HOLD_ROW, (int) (age * (BEAM_HOLD_ROW + 1) / BEAM_EXPAND_TICKS));
            } else if (age < fadeStart) {
                // Keep the bright, wide profile; the later atlas rows already shrink and fade.
                row = BEAM_HOLD_ROW;
            } else {
                row = Math.min(31, BEAM_HOLD_ROW + (int) ((age - fadeStart) * (32 - BEAM_HOLD_ROW) / BEAM_FADE_TICKS));
            }
        }
        float v = (row + 0.5F) / 32;
        float alpha = phase == HolyLightPhase.WARNING ? 0.55F : 1.0F;
        vertex(vertices, x - dx, y, z - dz, u0, v, alpha);
        vertex(vertices, x + dx, y, z + dz, u1, v, alpha);
        vertex(vertices, x + dx, y + snapshot.beamHeight(), z + dz, u1, v, alpha);
        vertex(vertices, x - dx, y + snapshot.beamHeight(), z - dz, u0, v, alpha);
    }

    private static void drawScorch(VertexConsumer vertices, HolyLightSnapshot snapshot,
                                   HolyLightScorchProjection.Face face, Vec3 camera, float alpha) {
        scorchVertex(vertices, snapshot, face.x0(), face.y(), face.z0(), camera, alpha);
        scorchVertex(vertices, snapshot, face.x0(), face.y(), face.z1(), camera, alpha);
        scorchVertex(vertices, snapshot, face.x1(), face.y(), face.z1(), camera, alpha);
        scorchVertex(vertices, snapshot, face.x1(), face.y(), face.z0(), camera, alpha);
    }

    private static void scorchVertex(VertexConsumer vertices, HolyLightSnapshot snapshot,
                                      double x, double y, double z, Vec3 camera, float alpha) {
        double u = (x - snapshot.position().x) / (snapshot.radius() * 2) + 0.5D;
        double v = (z - snapshot.position().z) / (snapshot.radius() * 2) + 0.5D;
        int turns = (int) (snapshot.seed() & 3);
        for (int i = 0; i < turns; i++) {
            double rotated = 1 - v;
            v = u;
            u = rotated;
        }
        vertex(vertices, x - camera.x, y - camera.y + 0.0125D, z - camera.z,
                (float) ((192.5D + u * 15) / 256), (float) ((16.5D + v * 15) / 32), alpha);
    }

    private static void vertex(VertexConsumer vertices, double x, double y, double z, float u, float v, float alpha) {
        // AFTER_WEATHER already installs the world view. Only camera-relative coordinates belong here.
        vertices.vertex(IDENTITY, (float) x, (float) y, (float) z).color(1, 1, 1, alpha).uv(u, v).endVertex();
    }
}

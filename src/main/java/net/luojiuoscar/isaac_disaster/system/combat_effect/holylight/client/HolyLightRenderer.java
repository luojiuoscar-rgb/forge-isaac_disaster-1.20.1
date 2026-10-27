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
        int duration = phase == HolyLightPhase.WARNING ? snapshot.warningTicks() : HolyLightVisuals.IMPACT_RING_TICKS;
        if (age >= duration) return;
        HolyLightVisuals.drawRing(vertices, snapshot.position().subtract(camera).add(0, .0125D, 0),
                new Vec3(1, 0, 0), new Vec3(0, 0, 1), snapshot.radius(),
                phase == HolyLightPhase.WARNING, age / duration);
    }

    private static void drawBeam(VertexConsumer vertices, HolyLightSnapshot snapshot, Vec3 camera,
                                 HolyLightPhase phase, double age, double sideX, double sideZ) {
        double halfWidth = phase == HolyLightPhase.WARNING
                ? 2.0D - 1.75D * Math.min(1, age / snapshot.warningTicks()) : 1.0D;
        halfWidth *= snapshot.radius() / 1.6D;
        int row = 0;
        if (phase == HolyLightPhase.FIRING) {
            row = HolyLightVisuals.firingRow(age,
                    age - (HolyLightTimeline.FIRING_TICKS - HolyLightVisuals.BEAM_FADE_TICKS));
        }
        float alpha = phase == HolyLightPhase.WARNING ? 0.55F : 1.0F;
        Vec3 start = snapshot.position().subtract(camera).add(0, .016D, 0);
        HolyLightVisuals.drawBeam(vertices, start, start.add(0, snapshot.beamHeight(), 0),
                new Vec3(sideX, 0, sideZ), halfWidth, row, alpha);
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

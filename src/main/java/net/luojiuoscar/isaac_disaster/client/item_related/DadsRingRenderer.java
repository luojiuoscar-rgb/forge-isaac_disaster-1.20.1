package net.luojiuoscar.isaac_disaster.client.item_related;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.luojiuoscar.isaac_disaster.system.freeze.state.EntityVisualState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, value = Dist.CLIENT)
public final class DadsRingRenderer {
    private static final int SEGMENTS = 64;
    private static final Matrix4f POSE = new Matrix4f();
    private static final RenderType GLOW = States.create();
    private static final double[] COS = new double[SEGMENTS + 1];
    private static final double[] SIN = new double[SEGMENTS + 1];

    static {
        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = i * Math.PI * 2 / SEGMENTS;
            COS[i] = Math.cos(angle);
            SIN[i] = Math.sin(angle);
        }
    }

    private DadsRingRenderer() {}

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        var buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertices = null;
        Vec3 camera = event.getCamera().getPosition();
        float partial = event.getPartialTick();
        for (var player : minecraft.level.players()) {
            if (!player.isAlive() || player.isSpectator() || player.isInvisible()
                    || !EntityVisualState.hasLayer(player, ModVisualLayers.DADS_RING.getId())) continue;
            if (!event.getFrustum().isVisible(player.getBoundingBox().inflate(2.15))) continue;
            Vec3 center = new Vec3(Mth.lerp(partial, player.xo, player.getX()),
                    Mth.lerp(partial, player.yo, player.getY()) + 0.7 * player.getScale(),
                    Mth.lerp(partial, player.zo, player.getZ())).subtract(camera);
            if (vertices == null) vertices = buffers.getBuffer(GLOW);
            band(vertices, center, 1.86, 1.97, 0, 0.55f);
            band(vertices, center, 1.97, 2.03, 0.75f, 0.75f);
            band(vertices, center, 2.03, 2.14, 0.55f, 0);
        }
        if (vertices != null) buffers.endBatch(GLOW);
    }

    private static void band(VertexConsumer vertices, Vec3 center, double inner, double outer,
                             float innerAlpha, float outerAlpha) {
        for (int i = 0; i < SEGMENTS; i++) {
            vertex(vertices, center, i, inner, innerAlpha);
            vertex(vertices, center, i, outer, outerAlpha);
            vertex(vertices, center, i + 1, outer, outerAlpha);
            vertex(vertices, center, i + 1, inner, innerAlpha);
        }
    }

    private static void vertex(VertexConsumer vertices, Vec3 center, int index, double radius, float alpha) {
        vertices.vertex(POSE, (float) (center.x + COS[index] * radius), (float) center.y,
                (float) (center.z + SIN[index] * radius)).color(1f, 0.88f, 0.55f, alpha).endVertex();
    }

    private static final class States extends RenderStateShard {
        private States() { super("dads_ring_state", () -> {}, () -> {}); }

        private static RenderType create() {
            return RenderType.create(IsaacDisaster.MOD_ID + "_dads_ring", DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS, 4096, false, false, RenderType.CompositeState.builder()
                            .setShaderState(POSITION_COLOR_SHADER).setTransparencyState(LIGHTNING_TRANSPARENCY)
                            .setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(COLOR_WRITE).createCompositeState(false));
        }
    }
}

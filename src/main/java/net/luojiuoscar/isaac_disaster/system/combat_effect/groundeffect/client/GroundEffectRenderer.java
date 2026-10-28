package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.client.render.OwnerHostility;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Draws all active ground effects as flat world-space polygons. */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, value = Dist.CLIENT)
public final class GroundEffectRenderer {
    private static final RenderType PUDDLE = GroundEffectRenderTypes.puddle();

    private GroundEffectRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;

        var stream = ClientGroundEffectRuntime.INSTANCE.stream();
        var states = stream.states();
        double animationTick = stream.localAnimationTick();
        if (states.isEmpty()) return;

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(PUDDLE);
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = new PoseStack();
        double seconds = (Util.getNanos() % 1_000_000_000L) / 1_000_000_000.0D;
        float pulseBrightness = GroundEffectAppearance.brightness(true, seconds);
        Map<UUID, Boolean> hostileOwners = new HashMap<>();
        for (ClientGroundEffectState state : states) {
            // Resolve each owner once per frame, regardless of how many puddles they left behind.
            boolean flashing = state.ownerUuid() != null
                    && hostileOwners.computeIfAbsent(state.ownerUuid(), ownerId -> {
                var owner = minecraft.level.getPlayerByUUID(ownerId);
                return owner != null && OwnerHostility.isHostile(minecraft.player.getUUID(),
                        minecraft.player.getTeam(), owner.getUUID(), owner.getTeam());
            });
            drawPuddle(vertices, pose, state, state.renderPosition(animationTick).subtract(camera),
                    flashing ? pulseBrightness : 1.0F, state.animationScale(animationTick));
        }
        buffers.endBatch(PUDDLE);
    }

    private static void drawPuddle(VertexConsumer vertices, PoseStack pose, ClientGroundEffectState state,
                                   Vec3 relative, float brightness, double scale) {
        if (scale <= 0.0D) return;
        int color = state.getColor();
        float red = GroundEffectAppearance.brightenChannel((color >> 16) & 255, brightness);
        float green = GroundEffectAppearance.brightenChannel((color >> 8) & 255, brightness);
        float blue = GroundEffectAppearance.brightenChannel(color & 255, brightness);
        float alpha = state.age() >= state.lifetime() - 10
                ? Math.max(0.0F, (state.lifetime() - state.age()) / 10.0F)
                : 0.78F;
        for (var point : state.triangleVertices()) {
            vertex(vertices, pose, relative.x + point.x() * scale, relative.y,
                    relative.z + point.z() * scale, red, green, blue, alpha);
        }
    }

    private static void vertex(VertexConsumer vertices, PoseStack pose, double x, double y, double z,
                               float red, float green, float blue, float alpha) {
        vertices.vertex(pose.last().pose(), (float) x, (float) y, (float) z)
                .color(red, green, blue, alpha).endVertex();
    }
}

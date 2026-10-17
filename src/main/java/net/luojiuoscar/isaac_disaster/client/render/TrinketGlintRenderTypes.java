package net.luojiuoscar.isaac_disaster.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public final class TrinketGlintRenderTypes extends RenderType {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/misc/trinket_gold_glint.png");
    private static final double SPEED_MULTIPLIER = 1.25D;
    private static final TexturingStateShard GOLD_GLINT_TEXTURING = new TexturingStateShard(
            IsaacDisaster.MOD_ID + "_gold_glint_texturing",
            TrinketGlintRenderTypes::setupGoldGlintTexturing, RenderSystem::resetTextureMatrix);
    private static final RenderType GLINT = createGoldGlint(
            "trinket_gold_glint", RENDERTYPE_GLINT_SHADER, MAIN_TARGET);
    private static final RenderType GLINT_DIRECT = createGoldGlint(
            "trinket_gold_glint_direct", RENDERTYPE_GLINT_DIRECT_SHADER, MAIN_TARGET);
    private static final RenderType GLINT_TRANSLUCENT = createGoldGlint(
            "trinket_gold_glint_translucent", RENDERTYPE_GLINT_TRANSLUCENT_SHADER, ITEM_ENTITY_TARGET);

    private TrinketGlintRenderTypes() {
        super("trinket_glint_render_type_access", DefaultVertexFormat.POSITION_TEX,
                VertexFormat.Mode.QUADS, 256, false, false, () -> { }, () -> { });
    }

    /** Returns the cached golden counterpart of a vanilla item glint type, or null for other types. */
    public static @Nullable RenderType goldCounterpart(RenderType renderType) {
        if (renderType == RenderType.glint()) return GLINT;
        if (renderType == RenderType.glintDirect()) return GLINT_DIRECT;
        if (renderType == RenderType.glintTranslucent()) return GLINT_TRANSLUCENT;
        return null;
    }

    private static RenderType createGoldGlint(String name, ShaderStateShard shader, OutputStateShard output) {
        return create(IsaacDisaster.MOD_ID + "_" + name, DefaultVertexFormat.POSITION_TEX,
                VertexFormat.Mode.QUADS, 256, false, false,
                CompositeState.builder()
                        .setShaderState(shader)
                        .setTextureState(new TextureStateShard(TEXTURE, true, false))
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .setDepthTestState(EQUAL_DEPTH_TEST)
                        .setTransparencyState(GLINT_TRANSPARENCY)
                        .setTexturingState(GOLD_GLINT_TEXTURING)
                        .setOutputState(output)
                        .createCompositeState(false));
    }

    private static void setupGoldGlintTexturing() {
        long time = (long) (Util.getMillis() * Minecraft.getInstance().options.glintSpeed().get()
                * 8.0D * SPEED_MULTIPLIER);
        float u = (time % 110000L) / 110000.0F;
        float v = (time % 30000L) / 30000.0F;
        RenderSystem.setTextureMatrix(new Matrix4f().translation(-u, v, 0.0F)
                .rotateZ(0.17453292F).scale(8.0F));
    }
}

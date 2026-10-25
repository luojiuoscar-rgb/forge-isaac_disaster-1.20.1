package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Cached atlas batches; neither pass writes depth or uses additive blending. */
final class HolyLightRenderTypes extends RenderStateShard {
    static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "textures/effects/holy_light.png");
    static final RenderType LIGHT = create("holy_light", true);
    static final RenderType SCORCH = create("holy_light_scorch", false);

    private HolyLightRenderTypes() {
        super("holy_light_states", () -> { }, () -> { });
    }

    private static RenderType create(String name, boolean light) {
        return RenderType.create("isaac_disaster_" + name,
                DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 4096, false, true,
                RenderType.CompositeState.builder()
                        .setShaderState(light ? RENDERTYPE_BEACON_BEAM_SHADER : POSITION_COLOR_TEX_SHADER)
                        .setTextureState(new TextureStateShard(ATLAS, false, false))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setCullState(NO_CULL)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false));
    }
}

package net.luojiuoscar.isaac_disaster.renderer.visual;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Render types shared by projectile visuals. */
public final class ProjectileRenderTypes {
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT = new ConcurrentHashMap<>();

    private ProjectileRenderTypes() {
    }

    public static RenderType translucent(ResourceLocation texture) {
        return TRANSLUCENT.computeIfAbsent(texture, ProjectileRenderTypes::createTranslucent);
    }

    private static RenderType createTranslucent(ResourceLocation texture) {
        return RenderType.create(
                IsaacDisaster.MOD_ID + "_projectile_translucent",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                true,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(States.shader())
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setTransparencyState(States.transparency())
                        .setLightmapState(States.lightmap())
                        .setOverlayState(States.overlay())
                        .setCullState(States.noCull())
                        .setDepthTestState(States.depthTest())
                        .setWriteMaskState(States.colorWrite())
                        .createCompositeState(true));
    }

    private static final class States extends RenderStateShard {
        private States() {
            super("projectile_render_type_access", () -> { }, () -> { });
        }

        private static ShaderStateShard shader() { return RENDERTYPE_ENTITY_TRANSLUCENT_SHADER; }
        private static TransparencyStateShard transparency() { return TRANSLUCENT_TRANSPARENCY; }
        private static LightmapStateShard lightmap() { return LIGHTMAP; }
        private static OverlayStateShard overlay() { return OVERLAY; }
        private static CullStateShard noCull() { return NO_CULL; }
        private static DepthTestStateShard depthTest() { return LEQUAL_DEPTH_TEST; }
        private static WriteMaskStateShard colorWrite() { return COLOR_WRITE; }
    }
}

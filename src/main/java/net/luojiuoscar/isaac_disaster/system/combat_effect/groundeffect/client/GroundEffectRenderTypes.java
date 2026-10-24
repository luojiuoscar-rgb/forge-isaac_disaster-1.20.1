package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/** Flat color render state for ground-effect polygons. */
final class GroundEffectRenderTypes {
    private GroundEffectRenderTypes() {
    }

    static RenderType puddle() {
        return RenderType.create(IsaacDisaster.MOD_ID + "_ground_effect_puddle",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, 4096,
                false, true, RenderType.CompositeState.builder()
                        .setShaderState(States.shader())
                        .setTransparencyState(States.transparency())
                        .setCullState(States.cull())
                        .setDepthTestState(States.depth())
                        .setWriteMaskState(States.writeMask())
                        .createCompositeState(false));
    }

    private static final class States extends RenderStateShard {
        private States() {
            super("ground_effect_render_state", () -> { }, () -> { });
        }

        private static ShaderStateShard shader() { return POSITION_COLOR_SHADER; }
        private static TransparencyStateShard transparency() { return TRANSLUCENT_TRANSPARENCY; }
        private static CullStateShard cull() { return NO_CULL; }
        private static DepthTestStateShard depth() { return LEQUAL_DEPTH_TEST; }
        private static WriteMaskStateShard writeMask() { return COLOR_DEPTH_WRITE; }
    }
}

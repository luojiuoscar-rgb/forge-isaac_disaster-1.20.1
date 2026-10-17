package net.luojiuoscar.isaac_disaster.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.luojiuoscar.isaac_disaster.client.render.TrinketGlintRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MultiBufferSource.BufferSource.class)
public abstract class GlintBufferSourceMixin {
    @Shadow @Final
    protected Map<RenderType, BufferBuilder> fixedBuffers;

    @Shadow
    public abstract void endBatch(RenderType renderType);

    @Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V", at = @At("RETURN"))
    private void isaac$flushGoldTrinketGlint(RenderType renderType, CallbackInfo ci) {
        RenderType goldType = TrinketGlintRenderTypes.goldCounterpart(renderType);
        if (goldType != null && this.fixedBuffers.containsKey(goldType)) {
            this.endBatch(goldType);
        }
    }
}

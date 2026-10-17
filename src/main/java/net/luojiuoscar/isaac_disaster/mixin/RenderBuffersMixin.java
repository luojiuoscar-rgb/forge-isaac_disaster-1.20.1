package net.luojiuoscar.isaac_disaster.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.luojiuoscar.isaac_disaster.client.render.TrinketGlintRenderTypes;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderBuffers.class)
public abstract class RenderBuffersMixin {
    @Inject(method = "put", at = @At("TAIL"))
    private static void isaac$addGoldTrinketGlintBuffer(
            Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> buffers,
            RenderType renderType, CallbackInfo ci) {
        RenderType goldType = TrinketGlintRenderTypes.goldCounterpart(renderType);
        if (goldType != null && !buffers.containsKey(goldType)) {
            buffers.put(goldType, new BufferBuilder(goldType.bufferSize()));
        }
    }
}

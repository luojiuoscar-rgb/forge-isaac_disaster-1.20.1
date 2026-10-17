package net.luojiuoscar.isaac_disaster.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luojiuoscar.isaac_disaster.client.render.TrinketGlintRenderTypes;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @WrapOperation(
            method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;"
                    + "ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;"
                    + "IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;"
                            + "getFoilBufferDirect(Lnet/minecraft/client/renderer/MultiBufferSource;"
                            + "Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;"
                            + "getFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;"
                            + "Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
            }
    )
    private VertexConsumer isaac$useGoldTrinketGlint(MultiBufferSource buffer, RenderType renderType,
                                                    boolean isItem, boolean withGlint,
                                                    Operation<VertexConsumer> original,
                                                    @Local(argsOnly = true) ItemStack stack) {
        if (!withGlint || !(stack.getItem() instanceof Trinket) || !Trinket.isEnchanted(stack)) {
            return original.call(buffer, renderType, isItem, withGlint);
        }
        MultiBufferSource goldenBuffer = requestedType -> {
            RenderType goldType = TrinketGlintRenderTypes.goldCounterpart(requestedType);
            return buffer.getBuffer(goldType == null ? requestedType : goldType);
        };
        return original.call(goldenBuffer, renderType, isItem, withGlint);
    }
}

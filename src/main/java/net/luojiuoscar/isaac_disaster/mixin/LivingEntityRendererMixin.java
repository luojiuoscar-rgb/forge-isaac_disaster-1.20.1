package net.luojiuoscar.isaac_disaster.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.client.EntityRenderFreeze;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    private static final String RENDER = "render(Lnet/minecraft/world/entity/LivingEntity;FF"
            + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

    @Shadow
    protected abstract void setupRotations(LivingEntity entity, PoseStack poseStack,
                                            float ageInTicks, float bodyYaw, float partialTick);

    @Shadow
    protected abstract void scale(LivingEntity entity, PoseStack poseStack, float partialTick);

    @Inject(method = RENDER, at = @At("HEAD"))
    private void beginFrozenRender(LivingEntity entity, float entityYaw, float partialTick,
                                   PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                   CallbackInfo ci) {
        EntityRenderFreeze.begin(entity);
    }

    @Redirect(
            method = RENDER,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;"
                            + "setupRotations(Lnet/minecraft/world/entity/LivingEntity;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V"
            )
    )
    private void freezeFrozenRotations(LivingEntityRenderer<?, ?> renderer, LivingEntity entity,
                                       PoseStack poseStack,
                                       float ageInTicks, float bodyYaw, float partialTick) {
        EntityRenderFreeze.freezeRotations(entity, poseStack,
                () -> setupRotations(entity, poseStack, ageInTicks, bodyYaw, partialTick));
    }

    @Redirect(
            method = RENDER,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;"
                            + "scale(Lnet/minecraft/world/entity/LivingEntity;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;F)V"
            )
    )
    private void freezeFrozenScale(LivingEntityRenderer<?, ?> renderer, LivingEntity entity,
                                   PoseStack poseStack, float partialTick) {
        EntityRenderFreeze.freezeScale(entity, poseStack,
                () -> scale(entity, poseStack, partialTick));
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private void endFrozenRender(LivingEntity entity, float entityYaw, float partialTick,
                                 PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                 CallbackInfo ci) {
        EntityRenderFreeze.end();
    }
}

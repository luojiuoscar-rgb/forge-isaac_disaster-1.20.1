package net.luojiuoscar.isaac_disaster.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luojiuoscar.isaac_disaster.client.EntityRenderFreeze;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin {
    @Inject(method = "translateAndRotate", at = @At("HEAD"))
    private void freezeFrozenPose(PoseStack poseStack, CallbackInfo ci) {
        // 使冻结生物保持静止
        EntityRenderFreeze.freezePart((ModelPart) (Object) this);
    }
}

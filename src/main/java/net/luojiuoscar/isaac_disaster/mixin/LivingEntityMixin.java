package net.luojiuoscar.isaac_disaster.mixin;

import net.luojiuoscar.isaac_disaster.system.ScaleUtils;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void injectScale(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        cir.setReturnValue(ScaleUtils.getScaledDimensions(self, pose));
    }
}

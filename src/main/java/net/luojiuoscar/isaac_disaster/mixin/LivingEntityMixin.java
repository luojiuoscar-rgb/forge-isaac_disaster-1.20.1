package net.luojiuoscar.isaac_disaster.mixin;

import net.luojiuoscar.isaac_disaster.system.ScaleUtils;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // scale
    @Inject(method = "getScale", at = @At("RETURN"), cancellable = true, remap = false)
    private void injectScale(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        cir.setReturnValue(cir.getReturnValue() * ScaleUtils.getScale(self));
    }
}

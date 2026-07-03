package net.luojiuoscar.isaac_disaster.mixin;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void injectScale(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        Player self = (Player)(Object)this;
        cir.setReturnValue(cir.getReturnValue().scale(self.getScale()));
    }

    @Inject(method = "getStandingEyeHeight", at = @At("RETURN"), cancellable = true)
    private void injectEyeHeight(Pose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
        Player self = (Player)(Object)this;
        cir.setReturnValue(cir.getReturnValue() * self.getScale());
    }
}

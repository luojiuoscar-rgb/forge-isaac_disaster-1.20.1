package net.luojiuoscar.isaac_disaster.mixin;

import net.luojiuoscar.isaac_disaster.system.EntityVisualState;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void freezeFrozenMobAi(CallbackInfo ci) {
        // 服务端暂停冻结生物的 AI
        Mob self = (Mob) (Object) this;
        if (EntityVisualState.isFrozen(self)) {
            ci.cancel();
        }
    }
}

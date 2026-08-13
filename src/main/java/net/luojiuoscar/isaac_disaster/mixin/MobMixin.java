package net.luojiuoscar.isaac_disaster.mixin;

import net.luojiuoscar.isaac_disaster.system.EntityFreezeState;
import net.luojiuoscar.isaac_disaster.system.TimeStopState;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void updateTimeStopState(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (!self.level().isClientSide()) {
            TimeStopState.updateMobFreezeState(self);
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"), cancellable = true)
    private void freezeTimeStoppedMob(CallbackInfo ci) {
        // 时停跳过整个 Mob.aiStep，避免执行继承而来的跳跃、travel 和重力推进。
        Mob self = (Mob) (Object) this;
        if (TimeStopState.isTimeStopTarget(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void freezeFrozenMobAi(CallbackInfo ci) {
        // 服务端暂停冻结生物的 AI
        Mob self = (Mob) (Object) this;
        if (EntityFreezeState.shouldFreeze(self)) {
            ci.cancel();
        }
    }
}

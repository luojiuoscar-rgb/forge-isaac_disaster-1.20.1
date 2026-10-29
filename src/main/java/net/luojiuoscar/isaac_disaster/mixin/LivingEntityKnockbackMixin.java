package net.luojiuoscar.isaac_disaster.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.luojiuoscar.isaac_disaster.system.damage.HurtKnockback;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityKnockbackMixin {
    @WrapOperation(method = "hurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    private void isaacDisaster$scopeHurtKnockback(LivingEntity entity, double strength,
                                                      double x, double z, Operation<Void> original,
                                                      @Local(argsOnly = true) DamageSource source) {
        HurtKnockback.withSource(entity, source, strength, () -> original.call(entity, strength, x, z));
    }

    @WrapOperation(method = "knockback", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/common/ForgeHooks;onLivingKnockBack(Lnet/minecraft/world/entity/LivingEntity;FDD)Lnet/minecraftforge/event/entity/living/LivingKnockBackEvent;",
            remap = false))
    private LivingKnockBackEvent isaacDisaster$dispatchSourceKnockback(LivingEntity entity, float strength,
            double x, double z, Operation<LivingKnockBackEvent> original,
            @Share("sourceKnockbackHandled") LocalBooleanRef handled) {
        HurtKnockback.Response response = HurtKnockback.claim(entity);
        handled.set(response != null);
        if (response == null) return original.call(entity, strength, x, z);
        LivingKnockBackEvent event = original.call(entity, response.strength(),
                response.tear() ? response.ratioX() : x, response.tear() ? response.ratioZ() : z);
        response.apply(entity, event);
        return event;
    }

    @Inject(method = "knockback", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/common/ForgeHooks;onLivingKnockBack(Lnet/minecraft/world/entity/LivingEntity;FDD)Lnet/minecraftforge/event/entity/living/LivingKnockBackEvent;",
            shift = At.Shift.AFTER, remap = false), cancellable = true)
    private void isaacDisaster$consumeSourceKnockback(double strength, double x, double z, CallbackInfo ci,
            @Share("sourceKnockbackHandled") LocalBooleanRef handled) {
        // The complete event has already dispatched. Keep its fields intact and consume only native movement.
        if (handled.get()) ci.cancel();
    }
}

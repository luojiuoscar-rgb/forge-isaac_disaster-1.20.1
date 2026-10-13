package net.luojiuoscar.isaac_disaster.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.luojiuoscar.isaac_disaster.accessor.AttributeInstanceOwnerAccess;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.system.rockbottom.RockBottomState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AttributeInstance.class)
public abstract class AttributeInstanceMixin implements AttributeInstanceOwnerAccess {
    @Unique
    private LivingEntity isaacDisaster$owner;

    @Override
    public void isaacDisaster$setOwner(LivingEntity owner) {
        this.isaacDisaster$owner = owner;
    }

    @Override
    @Nullable
    public LivingEntity isaacDisaster$getOwner() {
        return this.isaacDisaster$owner;
    }

    @Inject(method = "setDirty", at = @At("TAIL"))
    private void isaacDisaster$updateFireRate(CallbackInfo ci) {
        AttributeInstance instance = (AttributeInstance) (Object) this;
        if (this.isaacDisaster$owner instanceof Player player
                && instance.getAttribute() == ModAttributes.TEARS.get()) {
            PlayerHelper.refreshFireRateBaseValue(player);
        }
    }

    @Inject(method = "getValue", at = @At("HEAD"))
    private void isaacDisaster$initializeFireRate(CallbackInfoReturnable<Double> cir) {
        AttributeInstance instance = (AttributeInstance) (Object) this;
        if (this.isaacDisaster$owner instanceof Player player
                && instance.getAttribute() == ModAttributes.FIRE_RATE.get()) {
            PlayerHelper.refreshFireRateBaseValue(player);
        }
    }

    @ModifyReturnValue(method = "getValue", at = @At("RETURN"))
    private double isaacDisaster$applyRockBottom(double original) {
        LivingEntity owner = this.isaacDisaster$owner;
        if (owner == null) return original;

        AttributeInstance instance = (AttributeInstance) (Object) this;
        return RockBottomState.resolveValue(owner, instance.getAttribute(), original);
    }
}

package net.luojiuoscar.isaac_disaster.mixin;

import net.luojiuoscar.isaac_disaster.accessor.AttributeInstanceOwnerAccess;
import net.luojiuoscar.isaac_disaster.system.rockbottom.RockBottomState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private void isaacDisaster$applyRockBottom(CallbackInfoReturnable<Double> cir) {
        LivingEntity owner = this.isaacDisaster$owner;
        if (owner == null) return;

        AttributeInstance instance = (AttributeInstance) (Object) this;
        cir.setReturnValue(RockBottomState.resolveValue(owner, instance.getAttribute(), cir.getReturnValue()));
    }
}

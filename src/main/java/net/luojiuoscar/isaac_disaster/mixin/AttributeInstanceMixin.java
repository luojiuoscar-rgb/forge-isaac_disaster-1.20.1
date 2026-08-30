package net.luojiuoscar.isaac_disaster.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.luojiuoscar.isaac_disaster.accessor.AttributeInstanceOwnerAccess;
import net.luojiuoscar.isaac_disaster.system.rockbottom.RockBottomState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

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

    @ModifyReturnValue(method = "getValue", at = @At("RETURN"))
    private double isaacDisaster$applyRockBottom(double original) {
        LivingEntity owner = this.isaacDisaster$owner;
        if (owner == null) return original;

        AttributeInstance instance = (AttributeInstance) (Object) this;
        return RockBottomState.resolveValue(owner, instance.getAttribute(), original);
    }
}

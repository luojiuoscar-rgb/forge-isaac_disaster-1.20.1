package net.luojiuoscar.isaac_disaster.mixin;

import net.luojiuoscar.isaac_disaster.accessor.AttributeInstanceOwnerAccess;
import net.luojiuoscar.isaac_disaster.accessor.AttributeMapOwnerAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AttributeMap.class)
public abstract class AttributeMapMixin implements AttributeMapOwnerAccess {
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

    @Inject(method = "getInstance(Lnet/minecraft/world/entity/ai/attributes/Attribute;)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;", at = @At("RETURN"))
    private void isaacDisaster$bindAttributeOwner(Attribute attribute,
                                                    CallbackInfoReturnable<AttributeInstance> cir) {
        AttributeInstance instance = cir.getReturnValue();
        if (instance != null && this.isaacDisaster$owner != null) {
            ((AttributeInstanceOwnerAccess) (Object) instance)
                    .isaacDisaster$setOwner(this.isaacDisaster$owner);
        }
    }
}

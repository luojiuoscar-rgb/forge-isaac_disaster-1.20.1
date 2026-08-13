package net.luojiuoscar.isaac_disaster.mixin;

import net.luojiuoscar.isaac_disaster.accessor.LivingEntityFreezeAccess;
import net.luojiuoscar.isaac_disaster.system.ScaleUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityFreezeAccess {
    @Unique
    private static final EntityDataAccessor<Boolean> FROZEN =
            SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void isaacDisaster$defineTimeStopData(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        self.getEntityData().define(FROZEN, false);
    }

    @Override
    public boolean isaacDisaster$isFrozen() {
        LivingEntity self = (LivingEntity) (Object) this;
        return self.getEntityData().get(FROZEN);
    }

    @Override
    public void isaacDisaster$setFrozen(boolean frozen) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.getEntityData().get(FROZEN) != frozen) {
            self.getEntityData().set(FROZEN, frozen);
        }
    }

    @Inject(method = "getScale", at = @At("RETURN"), cancellable = true)
    private void injectScale(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        cir.setReturnValue(cir.getReturnValue() * ScaleUtils.getScale(self));
    }
}

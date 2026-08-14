package net.luojiuoscar.isaac_disaster.mixin;

import net.luojiuoscar.isaac_disaster.accessor.LivingEntityFreezeAccess;
import net.luojiuoscar.isaac_disaster.system.freeze.EntityFreezeRules;
import net.luojiuoscar.isaac_disaster.system.ScaleUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(method = "travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getFriction(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F",
            remap = false))
    private float isaacDisaster$useFrozenGroundFriction(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
        LivingEntity self = (LivingEntity) (Object) this;
        // Only the frozen effect changes the ground-friction sample; all other travel behavior remains vanilla.
        return EntityFreezeRules.usesLowFriction(self) ? 0.989F : state.getFriction(level, pos, entity);
    }

    @Inject(method = "getScale", at = @At("RETURN"), cancellable = true)
    private void injectScale(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        cir.setReturnValue(cir.getReturnValue() * ScaleUtils.getScale(self));
    }
}

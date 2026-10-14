package net.luojiuoscar.isaac_disaster.mixin;

import net.luojiuoscar.isaac_disaster.effect.custom.PanicEffect;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin {
    @Shadow private int swell;
    @Shadow private int oldSwell;
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_IS_IGNITED;

    @Inject(method = "tick", at = @At("HEAD"))
    private void stopFearedDetonation(CallbackInfo ci) {
        Creeper creeper = (Creeper) (Object) this;
        if (!PanicEffect.suppressesAttacks(creeper)) return;
        swell = 0;
        oldSwell = 0;
        creeper.getEntityData().set(DATA_IS_IGNITED, false);
        creeper.setSwellDir(-1);
    }

    @Inject(method = "explodeCreeper", at = @At("HEAD"), cancellable = true)
    private void preventFearedExplosion(CallbackInfo ci) {
        if (PanicEffect.suppressesAttacks((Creeper) (Object) this)) ci.cancel();
    }
}

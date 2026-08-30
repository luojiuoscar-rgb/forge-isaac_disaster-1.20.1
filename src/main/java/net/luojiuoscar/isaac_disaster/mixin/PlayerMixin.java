package net.luojiuoscar.isaac_disaster.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @ModifyReturnValue(method = "getDimensions", at = @At("RETURN"))
    private EntityDimensions isaacDisaster$applyScale(EntityDimensions original) {
        Player self = (Player)(Object)this;
        return original.scale(self.getScale());
    }

    @ModifyReturnValue(method = "getStandingEyeHeight", at = @At("RETURN"))
    private float isaacDisaster$applyEyeHeight(float original) {
        Player self = (Player)(Object)this;
        return original * self.getScale();
    }
}

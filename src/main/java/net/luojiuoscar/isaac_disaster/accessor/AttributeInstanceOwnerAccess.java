package net.luojiuoscar.isaac_disaster.accessor;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface AttributeInstanceOwnerAccess {
    void isaacDisaster$setOwner(LivingEntity owner);

    @Nullable
    LivingEntity isaacDisaster$getOwner();
}

package net.luojiuoscar.isaac_disaster.accessor;

/** Exposes the common synchronized freeze flag added to every LivingEntity. */
public interface LivingEntityFreezeAccess {
    boolean isaacDisaster$isFrozen();

    void isaacDisaster$setFrozen(boolean frozen);
}

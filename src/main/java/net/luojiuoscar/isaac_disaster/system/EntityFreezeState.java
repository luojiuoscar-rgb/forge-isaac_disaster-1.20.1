package net.luojiuoscar.isaac_disaster.system;

import net.minecraft.world.entity.LivingEntity;

/** Combines entity-owned freeze sources with the level-wide time-stop state. */
public final class EntityFreezeState {
    private EntityFreezeState() {
    }

    public static boolean shouldFreeze(LivingEntity entity) {
        // Entity-owned sources such as golden and petrified freeze the entity through its Capability.
        return EntityVisualState.isFrozen(entity)
                // The synchronized flag carries the server's final time-stop decision to clients.
                || TimeStopState.isTimeStopTarget(entity);
    }
}

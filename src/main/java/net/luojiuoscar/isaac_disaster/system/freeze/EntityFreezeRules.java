package net.luojiuoscar.isaac_disaster.system.freeze;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.system.freeze.state.EntityVisualState;
import net.luojiuoscar.isaac_disaster.system.freeze.state.TimeStopState;
import net.minecraft.world.entity.LivingEntity;

/** Combines entity-owned freeze sources with the level-wide time-stop state. */
public final class EntityFreezeRules {
    private EntityFreezeRules() {
    }

    public static boolean shouldFreeze(LivingEntity entity) {
        // Entity-owned sources such as golden and petrified freeze the entity through its Capability.
        return EntityVisualState.isFrozen(entity)
                // The synchronized flag carries the server's final time-stop decision to clients.
                || TimeStopState.isTimeStopTarget(entity);
    }

    public static boolean shouldClearHorizontalMotion(LivingEntity entity) {
        // Frozen entities retain external movement so they can slide across low-friction ground.
        return EntityVisualState.isFrozen(entity) && !usesLowFriction(entity);
    }

    public static boolean shouldCancelKnockback(LivingEntity entity) {
        // Time stop always prevents new movement because its Mob.aiStep skips travel entirely.
        return TimeStopState.isTimeStopTarget(entity)
                // Frozen takes priority over material freezes and deliberately preserves knockback.
                || (EntityVisualState.isFrozen(entity) && !usesLowFriction(entity));
    }

    public static boolean usesLowFriction(LivingEntity entity) {
        // The concrete frozen effect is the only entity-owned source that changes ground friction.
        return EntityVisualState.hasFreezeSource(entity, ModEffects.FROZEN.getId());
    }
}

package net.luojiuoscar.isaac_disaster.loot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

/**
 * Helper methods for reading common Isaac loot information from Minecraft loot contexts.
 */
public final class LootContextHelper {
    private LootContextHelper() {
    }

    /**
     * Finds the player responsible for the current loot roll, if one is available.
     */
    @Nullable
    public static ServerPlayer findResponsiblePlayer(LootContext context) {
        ServerPlayer player = getResponsiblePlayer(context.getParamOrNull(LootContextParams.THIS_ENTITY));
        if (player != null) return player;

        player = getResponsiblePlayer(context.getParamOrNull(LootContextParams.KILLER_ENTITY));
        if (player != null) return player;

        player = getResponsiblePlayer(context.getParamOrNull(LootContextParams.DIRECT_KILLER_ENTITY));
        if (player != null) return player;

        if (context.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER) instanceof ServerPlayer lastDamagePlayer) {
            return lastDamagePlayer;
        }
        return null;
    }

    /**
     * Returns an entity from the common source parameters, preferring THIS_ENTITY.
     */
    @Nullable
    public static Entity findSourceEntity(LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity != null) return entity;
        entity = context.getParamOrNull(LootContextParams.KILLER_ENTITY);
        if (entity != null) return entity;
        return context.getParamOrNull(LootContextParams.DIRECT_KILLER_ENTITY);
    }

    @Nullable
    private static ServerPlayer getResponsiblePlayer(@Nullable Entity entity) {
        if (entity instanceof ServerPlayer player) return player;
        if (entity instanceof TamableAnimal tamable && tamable.getOwner() instanceof ServerPlayer owner) {
            return owner;
        }
        if (entity instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer owner) {
            return owner;
        }
        return null;
    }
}

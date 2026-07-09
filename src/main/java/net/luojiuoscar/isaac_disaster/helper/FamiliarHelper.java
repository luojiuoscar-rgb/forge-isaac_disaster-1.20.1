package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.capability.player.FamiliarEntry;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Reconciles player familiar requirements with real familiar entities in the world.
 */
public final class FamiliarHelper {
    private FamiliarHelper() {
    }

    /**
     * Ensures every familiar entry on the player has the requested number of valid entities.
     */
    public static void reconcile(ServerPlayer player) {
        player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(data -> {
            List<FamiliarEntry> entries = new ArrayList<>(data.getEntries());
            for (FamiliarEntry entry : entries) {
                cleanupInvalidEntities(player, entry);
                discardExtraEntities(player, entry);
                spawnMissingEntities(player, entry);
                syncFormationIndexes(player, entry);
            }
            data.pruneEmptyEntries();
        });
    }

    /**
     * Checks whether a registered EntityType can create an Isaac familiar entity in this level.
     */
    public static boolean canCreateFamiliar(Level level, ResourceLocation typeId) {
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(typeId);
        if (entityType == null) return false;
        return entityType.create(level) instanceof AbstractIsaacFamiliarEntity;
    }

    /**
     * Returns registered EntityType ids that can create Isaac familiar entities.
     */
    public static Stream<ResourceLocation> getRegisteredFamiliarTypeIds(Level level) {
        return ForgeRegistries.ENTITY_TYPES.getKeys().stream()
                .filter(typeId -> canCreateFamiliar(level, typeId));
    }

    /**
     * Removes UUID records that no longer resolve to valid familiar entities.
     */
    public static void cleanupInvalidEntities(ServerPlayer player, FamiliarEntry entry) {
        List<UUID> entityIds = entry.getEntityIds();
        for (int i = entityIds.size() - 1; i >= 0; i--) {
            UUID entityId = entityIds.get(i);
            Entity entity = player.serverLevel().getEntity(entityId);
            if (!(entity instanceof AbstractIsaacFamiliarEntity familiar) ||
                    !entry.getType().equals(familiar.getFamiliarType()) ||
                    familiar.getOwner() != player ||
                    !familiar.isValidFamiliar()) {
                if (entity instanceof AbstractIsaacFamiliarEntity familiarEntity) {
                    familiarEntity.discardSilently();
                }
                entityIds.remove(i);
            }
        }
    }

    /**
     * Removes valid entities above the requested count from the end of the entry list.
     */
    public static void discardExtraEntities(ServerPlayer player, FamiliarEntry entry) {
        List<UUID> entityIds = entry.getEntityIds();
        while (entityIds.size() > entry.getCount()) {
            UUID entityId = entityIds.remove(entityIds.size() - 1);
            Entity entity = player.serverLevel().getEntity(entityId);
            if (entity instanceof AbstractIsaacFamiliarEntity familiar) {
                familiar.discardSilently();
            }
        }
    }

    /**
     * Spawns missing familiar entities until the entry reaches its requested count.
     */
    public static void spawnMissingEntities(ServerPlayer player, FamiliarEntry entry) {
        while (entry.getEntityIds().size() < entry.getCount()) {
            AbstractIsaacFamiliarEntity familiar = createFamiliar(player, entry.getType());
            if (familiar == null) return;
            familiar.setFormationIndex(entry.getEntityIds().size());
            entry.addEntity(familiar.getUUID());
            if (!player.level().addFreshEntity(familiar)) {
                entry.removeEntity(familiar.getUUID());
                return;
            }
        }
    }

    /**
     * Pushes the capability list order into live entities so clients can predict formations consistently.
     */
    public static void syncFormationIndexes(ServerPlayer player, FamiliarEntry entry) {
        List<UUID> entityIds = entry.getEntityIds();
        for (int i = 0; i < entityIds.size(); i++) {
            Entity entity = player.serverLevel().getEntity(entityIds.get(i));
            if (entity instanceof AbstractIsaacFamiliarEntity familiar) {
                familiar.setFormationIndex(i);
            }
        }
    }

    /**
     * Creates one familiar entity from an EntityType registry id.
     */
    public static AbstractIsaacFamiliarEntity spawnFamiliar(ServerPlayer player, ResourceLocation typeId) {
        AbstractIsaacFamiliarEntity familiar = createFamiliar(player, typeId);
        if (familiar == null) return null;

        final boolean[] registered = {false};
        player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(
                data -> {
                    FamiliarEntry entry = data.getOrCreateEntry(typeId);
                    entry.addEntity(familiar.getUUID());
                    familiar.setFormationIndex(entry.getIndex(familiar.getUUID()));
                    registered[0] = true;
                });
        if (!registered[0]) return null;

        if (!player.level().addFreshEntity(familiar)) {
            player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(
                    data -> data.removeEntity(typeId, familiar.getUUID()));
            return null;
        }
        return familiar;
    }

    /**
     * Creates one familiar entity without registering it in the owner capability or level.
     */
    private static AbstractIsaacFamiliarEntity createFamiliar(ServerPlayer player, ResourceLocation typeId) {
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(typeId);
        if (entityType == null) return null;

        Level level = player.level();
        Entity entity = entityType.create(level);
        if (!(entity instanceof AbstractIsaacFamiliarEntity familiar)) return null;

        familiar.setOwner(player);
        familiar.setPos(player.position().add(0, player.getBbHeight() * 0.55, 0));
        return familiar;
    }
}

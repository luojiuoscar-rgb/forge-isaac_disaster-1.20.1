package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.FamiliarEntry;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarLimitGroup;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarLimitGroups;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryManager;

import java.util.ArrayList;
import java.util.Collection;
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
     * Cleans invalid runtime records, removes excess entities, and refreshes formation indexes.
     * Missing entities remain queued for the per-tick spawn scheduler.
     */
    public static void maintainExistingFamiliars(ServerPlayer player) {
        player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(data -> {
            List<FamiliarEntry> entries = new ArrayList<>(data.getEntries());
            for (FamiliarEntry entry : entries) {
                cleanupInvalidEntities(player, entry);
                discardExtraEntities(player, entry);
                syncFormationIndexes(player, entry);
            }
            data.pruneEmptyEntries();
        });
    }

    /**
     * Checks whether the addon-facing familiar registry contains a valid descriptor with the same EntityType id.
     */
    public static boolean isRegisteredFamiliarType(ResourceLocation typeId) {
        FamiliarEntityType familiarType = getFamiliarType(typeId);
        return familiarType != null && typeId.equals(ForgeRegistries.ENTITY_TYPES.getKey(familiarType.getEntityType()));
    }

    /** Resolves registration policy without constructing entities. */
    public static FamiliarEntityType getFamiliarType(ResourceLocation typeId) {
        IForgeRegistry<FamiliarEntityType> registry = getFamiliarRegistry();
        return registry == null ? null : registry.getValue(typeId);
    }

    /** All registered definitions are instantiated once for each player's capability. */
    public static Collection<FamiliarLimitGroup> getRegisteredFamiliarLimitGroups() {
        IForgeRegistry<FamiliarLimitGroup> registry =
                RegistryManager.ACTIVE.getRegistry(ModFamiliarLimitGroups.FAMILIAR_LIMIT_GROUP_KEY);
        return registry == null ? List.of() : registry.getValues();
    }

    /**
     * Returns valid familiar descriptor ids without constructing throwaway entities.
     */
    public static Stream<ResourceLocation> getRegisteredFamiliarTypeIds() {
        IForgeRegistry<FamiliarEntityType> registry = getFamiliarRegistry();
        if (registry == null) return Stream.empty();
        return registry.getKeys().stream().filter(FamiliarHelper::isRegisteredFamiliarType);
    }

    /**
     * Removes UUID records that no longer resolve to valid familiar entities.
     */
    public static void cleanupInvalidEntities(ServerPlayer player, FamiliarEntry entry) {
        PlayerFamiliarData data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA)
                .resolve().orElseThrow();
        for (int i = entry.getEntityCount() - 1; i >= 0; i--) {
            UUID entityId = entry.getEntityId(i);
            Entity entity = player.serverLevel().getEntity(entityId);
            if (!(entity instanceof AbstractIsaacFamiliarEntity familiar)
                    || !entry.getType().equals(familiar.getFamiliarType())
                    || familiar.getOwner() != player) {
                data.removeEntity(entry.getType(), entityId);
                continue;
            }
            if (!familiar.isValidFamiliar()) {
                familiar.discardSilently();
                data.removeEntity(entry.getType(), entityId);
            }
        }
    }

    /**
     * Removes entities above the granted quota, preserving the earliest arrivals.
     */
    public static void discardExtraEntities(ServerPlayer player, FamiliarEntry entry) {
        PlayerFamiliarData data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA)
                .resolve().orElseThrow();
        while (entry.getEntityCount() > data.getEffectiveCount(entry.getType())) {
            UUID entityId = entry.getEntityId(entry.getEntityCount() - 1);
            data.removeEntity(entry.getType(), entityId);
            Entity entity = player.serverLevel().getEntity(entityId);
            if (entity instanceof AbstractIsaacFamiliarEntity familiar
                    && familiar.getOwner() == player
                    && entry.getType().equals(familiar.getFamiliarType())) {
                familiar.discardSilently();
            }
        }
    }

    /** Processes one queued teleport or one new spawn, even if called repeatedly in the same tick. */
    public static boolean tickFamiliarAdmissions(ServerPlayer player) {
        if (!player.isAlive()) return false;
        PlayerFamiliarData data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA)
                .resolve().orElse(null);
        if (data == null || !data.beginAdmission(player.level().getGameTime())) return false;
        discardExcessFamiliars(player, data);
        UUID entityId;
        while ((entityId = data.pollRecall()) != null) {
            Entity entity = player.serverLevel().getEntity(entityId);
            if (entity instanceof AbstractIsaacFamiliarEntity familiar && familiar.getOwner() == player
                    && familiar.recallToOwner()) return true;
        }
        return spawnNextMissingFamiliar(player, data);
    }

    /**
     * Creates one missing familiar. Normal player ticks use tickFamiliarAdmissions for shared pacing.
     *
     * @return true when one entity was successfully added to the level
     */
    public static boolean spawnNextMissingFamiliar(ServerPlayer player) {
        if (!player.isAlive()) return false;

        PlayerFamiliarData data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA)
                .resolve().orElse(null);
        if (data == null) return false;
        discardExcessFamiliars(player, data);
        return spawnNextMissingFamiliar(player, data);
    }

    private static boolean spawnNextMissingFamiliar(ServerPlayer player, PlayerFamiliarData data) {
        FamiliarEntry entry = data.getNextIncompleteEntry().orElse(null);
        if (entry == null) return false;
        AbstractIsaacFamiliarEntity familiar = createFamiliar(player, entry.getType());
        if (familiar == null) return false;
        boolean added = false;
        try {
            familiar.setFormationIndex(entry.getEntityCount());
            data.registerEntity(entry.getType(), familiar.getUUID());
            added = player.level().addFreshEntity(familiar) && !familiar.isRemoved()
                    && data.containsEntity(entry.getType(), familiar.getUUID())
                    && entry.getEntityCount() <= data.getEffectiveCount(entry.getType());
            return added;
        } finally {
            if (!added) {
                data.removeEntity(entry.getType(), familiar.getUUID());
                familiar.discardSilently();
            }
        }
    }

    /** Apply already-decided quota reductions before recalling or admitting another entity. */
    private static void discardExcessFamiliars(ServerPlayer player, PlayerFamiliarData data) {
        for (FamiliarEntry entry : new ArrayList<>(data.getEntries())) {
            if (entry.getEntityCount() <= data.getEffectiveCount(entry.getType())) continue;
            discardExtraEntities(player, entry);
            syncFormationIndexes(player, entry);
        }
        data.pruneEmptyEntries();
    }

    /**
     * Pushes the capability list order into live entities so server-side formation behavior stays deterministic.
     */
    public static void syncFormationIndexes(ServerPlayer player, FamiliarEntry entry) {
        for (int i = 0; i < entry.getEntityCount(); i++) {
            Entity entity = player.serverLevel().getEntity(entry.getEntityId(i));
            if (entity instanceof AbstractIsaacFamiliarEntity familiar
                    && familiar.getOwner() == player
                    && entry.getType().equals(familiar.getFamiliarType())) {
                if (familiar.getFormationIndex() != i) {
                    familiar.setFormationIndex(i);
                }
            }
        }
    }

    /**
     * Discards every runtime familiar owned by the player while preserving persistent requirements.
     */
    public static void discardAllRuntimeFamiliars(ServerPlayer player) {
        player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(data -> {
            data.clearPendingRecalls();
            for (FamiliarEntry entry : data.getEntries()) {
                for (UUID entityId : entry.getEntityIds()) {
                    Entity entity = player.serverLevel().getEntity(entityId);
                    if (entity instanceof AbstractIsaacFamiliarEntity familiar
                            && familiar.getOwner() == player
                            && entry.getType().equals(familiar.getFamiliarType())) {
                        familiar.discardSilently();
                    }
                }
            }
            data.clearRuntimeEntities();
        });
    }

    /**
     * Creates one familiar entity without registering it in the owner capability or level.
     */
    private static AbstractIsaacFamiliarEntity createFamiliar(ServerPlayer player, ResourceLocation typeId) {
        FamiliarEntityType familiarType = getFamiliarType(typeId);
        if (familiarType == null) return null;

        ResourceLocation entityTypeId = ForgeRegistries.ENTITY_TYPES.getKey(familiarType.getEntityType());
        if (!typeId.equals(entityTypeId)) {
            IsaacDisaster.LOGGER.error(
                    "Familiar descriptor {} must use the same id as its EntityType, found {}", typeId, entityTypeId);
            return null;
        }

        Level level = player.level();
        AbstractIsaacFamiliarEntity familiar = familiarType.create(level);
        if (familiar == null) return null;

        familiar.setOwner(player);
        familiar.placeAtOwner();
        return familiar;
    }

    private static IForgeRegistry<FamiliarEntityType> getFamiliarRegistry() {
        return RegistryManager.ACTIVE.getRegistry(ModFamiliarEntities.FAMILIAR_ENTITY_KEY);
    }
}

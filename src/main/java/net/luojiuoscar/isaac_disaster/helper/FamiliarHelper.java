package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.FamiliarEntry;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarData;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryManager;

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
        FamiliarEntityType familiarType = getFamiliarRegistryValue(typeId);
        return familiarType != null && typeId.equals(ForgeRegistries.ENTITY_TYPES.getKey(familiarType.getEntityType()));
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
        for (int i = entry.getEntityCount() - 1; i >= 0; i--) {
            UUID entityId = entry.getEntityId(i);
            Entity entity = player.serverLevel().getEntity(entityId);
            if (!(entity instanceof AbstractIsaacFamiliarEntity familiar)
                    || !entry.getType().equals(familiar.getFamiliarType())
                    || familiar.getOwner() != player) {
                entry.removeEntityAt(i);
                continue;
            }
            if (!familiar.isValidFamiliar()) {
                familiar.discardSilently();
                entry.removeEntityAt(i);
            }
        }
    }

    /**
     * Removes valid entities above the requested count from the end of the entry list.
     */
    public static void discardExtraEntities(ServerPlayer player, FamiliarEntry entry) {
        while (entry.getEntityCount() > entry.getCount()) {
            UUID entityId = entry.removeEntityAt(entry.getEntityCount() - 1);
            Entity entity = player.serverLevel().getEntity(entityId);
            if (entity instanceof AbstractIsaacFamiliarEntity familiar
                    && familiar.getOwner() == player
                    && entry.getType().equals(familiar.getFamiliarType())) {
                familiar.discardSilently();
            }
        }
    }

    /**
     * Spawns at most one familiar for this player using insertion-order round-robin scheduling.
     *
     * @return true when one entity was successfully added to the level
     */
    public static boolean spawnNextMissingFamiliar(ServerPlayer player) {
        if (!player.isAlive()) return false;

        PlayerFamiliarData data = player.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA)
                .resolve().orElse(null);
        if (data == null) return false;

        FamiliarEntry entry = data.getNextIncompleteEntry().orElse(null);
        if (entry == null) return false;

        AbstractIsaacFamiliarEntity familiar = createFamiliar(player, entry.getType());
        if (familiar == null) return false;

        familiar.setFormationIndex(entry.getEntityCount());
        entry.addEntity(familiar.getUUID());
        if (!player.level().addFreshEntity(familiar)) {
            entry.removeEntity(familiar.getUUID());
            return false;
        }
        return true;
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
            for (FamiliarEntry entry : data.getEntries()) {
                for (UUID entityId : entry.getEntityIds()) {
                    Entity entity = player.serverLevel().getEntity(entityId);
                    if (entity instanceof AbstractIsaacFamiliarEntity familiar
                            && familiar.getOwner() == player
                            && entry.getType().equals(familiar.getFamiliarType())) {
                        familiar.discardSilently();
                    }
                }
                entry.clearEntityIds();
            }
        });
    }

    /**
     * Creates one familiar entity without registering it in the owner capability or level.
     */
    private static AbstractIsaacFamiliarEntity createFamiliar(ServerPlayer player, ResourceLocation typeId) {
        FamiliarEntityType familiarType = getFamiliarRegistryValue(typeId);
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
        familiar.setPos(player.position().add(0, player.getBbHeight() * 0.55, 0));
        return familiar;
    }

    private static FamiliarEntityType getFamiliarRegistryValue(ResourceLocation typeId) {
        IForgeRegistry<FamiliarEntityType> registry = getFamiliarRegistry();
        return registry == null ? null : registry.getValue(typeId);
    }

    private static IForgeRegistry<FamiliarEntityType> getFamiliarRegistry() {
        return RegistryManager.ACTIVE.getRegistry(ModFamiliarEntities.FAMILIAR_ENTITY_KEY);
    }
}

package net.luojiuoscar.isaac_disaster.system.rockbottom;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItems;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerIsaacItemsProvider;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.RockBottomHistorySyncS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Coordinates Rock Bottom's persistent history, value resolution, and client synchronization. */
public final class RockBottomState {
    /** Attributes whose vanilla final values are protected by Rock Bottom. */
    public static final List<Attribute> TARGET_ATTRIBUTES = List.of(
            Attributes.ATTACK_DAMAGE,
            Attributes.MOVEMENT_SPEED,
            Attributes.LUCK,
            ModAttributes.BULLET_SPEED.get(),
            ModAttributes.TEARS.get(),
            ModAttributes.TEARS_CORRECTION.get(),
            ModAttributes.BULLET_RANGE.get(),
            Attributes.KNOCKBACK_RESISTANCE,
            Attributes.ARMOR,
            Attributes.ARMOR_TOUGHNESS,
            ModAttributes.BLOCK_BREAKING_SPEED.get(),
            ForgeMod.ENTITY_REACH.get(),
            ForgeMod.BLOCK_REACH.get()
    );

    /** Stable keys used for both server persistence and client history synchronization. */
    public static final List<ResourceLocation> ATTR_DATA_RESOURCE = new ArrayList<>();

    static {
        for (Attribute attribute : TARGET_ATTRIBUTES) {
            String safePath = attribute.getDescriptionId().replace('.', '_');
            ATTR_DATA_RESOURCE.add(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, safePath));
        }
    }

    /** Prevents instantiation of this static state utility. */
    private RockBottomState() {
    }

    /**
     * Checks whether an attribute is managed by Rock Bottom.
     *
     * @param attribute attribute to check
     * @return {@code true} when the attribute is one of the configured targets
     */
    public static boolean isTarget(Attribute attribute) {
        return TARGET_ATTRIBUTES.contains(attribute);
    }

    /**
     * Resolves the stable persistence key associated with a target attribute.
     *
     * @param attribute attribute whose key is requested
     * @return the history key, or {@code null} for an attribute outside the target list
     */
    public static ResourceLocation getHistoryKey(Attribute attribute) {
        int index = TARGET_ATTRIBUTES.indexOf(attribute);
        return index < 0 ? null : ATTR_DATA_RESOURCE.get(index);
    }

    /**
     * Applies Rock Bottom to an already-calculated vanilla final attribute value.
     * Non-player entities, non-target attributes, and attributes absent from the
     * entity's attribute map or supplier are returned unchanged. The server persists authoritative
     * history in {@code ExtraData}; the client uses only its synchronized cache.
     *
     * @param entity entity that owns the attribute instance
     * @param attribute attribute being read
     * @param vanillaValue final value calculated by vanilla attribute modifiers
     * @return the vanilla value or the historical maximum, whichever applies
     */
    public static double resolveValue(LivingEntity entity, Attribute attribute, double vanillaValue) {
        if (!(entity instanceof Player player) || !isTarget(attribute)) {
            return vanillaValue;
        }
        if (!player.getAttributes().hasAttribute(attribute)) {
            return vanillaValue;
        }

        ResourceLocation historyKey = getHistoryKey(attribute);
        if (historyKey == null) return vanillaValue;

        if (player.level().isClientSide()) {
            if (!hasRockBottom(player)) return vanillaValue;
            return RockBottomValueHistory.resolve(
                    vanillaValue,
                    ClientDataManager.getInstance().getRockBottomHistory(historyKey)
            );
        }

        if (!(player instanceof ServerPlayer serverPlayer)) return vanillaValue;
        if (getRockBottomCount(serverPlayer) == 0) {
            clearHistoryIfInactive(serverPlayer);
            return vanillaValue;
        }

        final double[] resolvedValue = {vanillaValue};
        serverPlayer.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(extraData -> {
            Double historical = extraData.getDouble(historyKey);
            resolvedValue[0] = RockBottomValueHistory.resolve(vanillaValue, historical);

            if (historical == null || vanillaValue > historical) {
                extraData.setDouble(historyKey, vanillaValue);
                syncHistoryToClient(serverPlayer);
            }
        });
        return resolvedValue[0];
    }

    /**
     * Checks whether the player currently has at least one effective Rock Bottom.
     * The client reads its synchronized item-count cache, while the server reads
     * the player's applied passive-item and Curios state.
     *
     * @param player player to check
     * @return {@code true} when Rock Bottom is currently effective
     */
    public static boolean hasRockBottom(Player player) {
        if (player.level().isClientSide()) {
            return ClientDataManager.getInstance().getCountFromId(ItemId.ROCK_BOTTOM.getId()) > 0;
        }

        return player instanceof ServerPlayer serverPlayer && getRockBottomCount(serverPlayer) > 0;
    }

    /** Returns the number of effective Rock Bottom copies recorded for a server player. */
    private static int getRockBottomCount(ServerPlayer player) {
        return player.getCapability(PlayerIsaacItemsProvider.PLAYER_ISAAC_ITEMS)
                .map(PlayerIsaacItems::getRockBottomCount)
                .orElse(0);
    }

    /**
     * Sends a complete server-side history snapshot to the player.
     * Missing values are omitted, and the client packet handler replaces its
     * previous cache with this snapshot.
     *
     * @param player recipient of the history snapshot
     */
    public static void syncHistoryToClient(ServerPlayer player) {
        Map<ResourceLocation, Double> history = new LinkedHashMap<>();
        player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(extraData -> {
            for (ResourceLocation key : ATTR_DATA_RESOURCE) {
                Double value = extraData.getDouble(key);
                if (value != null) history.put(key, value);
            }
        });
        ModMessages.sentToPlayer(new RockBottomHistorySyncS2CPacket(history), player);
    }

    /**
     * Idempotently removes all Rock Bottom history after the last effective copy
     * has been removed and sends an empty client snapshot when anything changed.
     *
     * @param player player whose history may be cleared
     */
    public static void clearHistoryIfInactive(ServerPlayer player) {
        if (getRockBottomCount(player) > 0) return;

        final boolean[] hadHistory = {false};
        player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(extraData -> {
            for (ResourceLocation key : ATTR_DATA_RESOURCE) {
                if (extraData.getDouble(key) != null) {
                    extraData.removeDouble(key);
                    hadHistory[0] = true;
                }
            }
        });

        if (hadHistory[0]) {
            ModMessages.sentToPlayer(new RockBottomHistorySyncS2CPacket(Map.of()), player);
        }
    }
}

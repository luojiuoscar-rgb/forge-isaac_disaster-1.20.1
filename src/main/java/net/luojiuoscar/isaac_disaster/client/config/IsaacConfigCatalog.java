package net.luojiuoscar.isaac_disaster.client.config;

import net.luojiuoscar.isaac_disaster.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manual catalog of config entries exposed by the first custom config screen.
 */
public final class IsaacConfigCatalog {
    private static final List<IsaacConfigEntry<?>> ENTRIES = createEntries();

    private IsaacConfigCatalog() {
    }

    /**
     * Returns all known config entries.
     */
    public static List<IsaacConfigEntry<?>> entries() {
        return ENTRIES;
    }

    /**
     * Returns entries that belong to the supplied category.
     */
    public static List<IsaacConfigEntry<?>> entriesFor(IsaacConfigCategory category) {
        return ENTRIES.stream().filter(entry -> entry.category() == category).toList();
    }

    private static List<IsaacConfigEntry<?>> createEntries() {
        List<IsaacConfigEntry<?>> entries = new ArrayList<>();

        entries.addAll(Arrays.asList(
                doubleEntry("health_bonus", IsaacConfigCategory.PLAYER_STATS, Config.HEALTH_BONUS, 10.0),
                doubleEntry("movement_speed_bonus", IsaacConfigCategory.PLAYER_STATS, Config.MOVEMENT_SPEED_BONUS, 0.02),
                doubleEntry("movement_speed_limit", IsaacConfigCategory.PLAYER_STATS, Config.MOVEMENT_SPEED_LIMIT, 0.1),
                doubleEntry("damage_bonus", IsaacConfigCategory.PLAYER_STATS, Config.DAMAGE_BONUS, 2.0),
                doubleEntry("luck_bonus", IsaacConfigCategory.PLAYER_STATS, Config.LUCK_BONUS, 1.0),
                doubleEntry("fly_time", IsaacConfigCategory.PLAYER_STATS, Config.FLY_TIME, 100.0),
                doubleEntry("scale_bonus", IsaacConfigCategory.PLAYER_STATS, Config.SCALE_BONUS, 0.1),
                doubleEntry("range_bonus", IsaacConfigCategory.PLAYER_STATS, Config.RANGE_BONUS, 4.5),
                doubleEntry("entity_reach_bonus", IsaacConfigCategory.PLAYER_STATS, Config.ENTITY_REACH_BONUS, 0.5),
                doubleEntry("block_reach_bonus", IsaacConfigCategory.PLAYER_STATS, Config.BLOCK_REACH_BONUS, 1.0),
                doubleEntry("bullet_speed_bonus", IsaacConfigCategory.PLAYER_STATS, Config.BULLET_SPEED_BONUS, 0.2),
                doubleEntry("tears_bonus", IsaacConfigCategory.PLAYER_STATS, Config.TEARS_BONUS, 0.7),
                doubleEntry("tears_correction_bonus", IsaacConfigCategory.PLAYER_STATS, Config.TEARS_CORRECTION_BONUS, 1.0),
                doubleEntry("attack_speed_bonus", IsaacConfigCategory.PLAYER_STATS, Config.ATTACK_SPEED_BONUS, 1.0),
                doubleEntry("block_breaking_speed_bonus", IsaacConfigCategory.PLAYER_STATS, Config.BLOCK_BREAKING_SPEED_BONUS, 5.0),
                doubleEntry("bullet_scale_bonus", IsaacConfigCategory.PLAYER_STATS, Config.BULLET_SCALE_BONUS, 0.1),
                doubleEntry("attack_knockback_bonus", IsaacConfigCategory.PLAYER_STATS, Config.ATTACK_KNOCKBACK_BONUS, 0.5),
                doubleEntry("damage_multiplier_base_bonus", IsaacConfigCategory.PLAYER_STATS, Config.DAMAGE_MULTIPLIER_BASE, 1.0),
                doubleEntry("nearby_range", IsaacConfigCategory.PLAYER_STATS, Config.NEARBY_RANGE, 12.0),
                doubleEntry("basic_time_interval", IsaacConfigCategory.PLAYER_STATS, Config.BASIC_TIME_INTERVAL, 10.0)
        ));

        entries.addAll(Arrays.asList(
                intEntry("active_item_durability_restore_rate", IsaacConfigCategory.ITEM_RELATED,
                        Config.ACTIVE_ITEM_DURABILITY_RESTORE_RATE, 4),
                booleanEntry("limited_active_item_durability_restore", IsaacConfigCategory.ITEM_RELATED,
                        Config.LIMITED_ACTIVE_ITEM_DURABILITY_RESTORE, false),
                booleanEntry("active_item_auto_restore", IsaacConfigCategory.ITEM_RELATED,
                        Config.ACTIVE_ITEM_AUTO_RESTORE, true),
                doubleEntry("holy_shield_strength", IsaacConfigCategory.ITEM_RELATED,
                        Config.HOLY_SHIELD_STRENGTH, 3.0),
                doubleEntry("money_is_power_strength", IsaacConfigCategory.ITEM_RELATED,
                        Config.MONEY_IS_POWER_STRENGTH, 0.007)
        ));

        entries.addAll(Arrays.asList(
                intEntry("passive_item_limit", IsaacConfigCategory.MISC, Config.PASSIVE_ITEM_LIMIT, 999),
                booleanEntry("extra_passive_item_backpack", IsaacConfigCategory.MISC,
                        Config.EXTRA_PASSIVE_ITEM_BACKPACK, false),
                booleanEntry("allow_curio_unequip", IsaacConfigCategory.MISC, Config.ALLOW_CURIO_UNEQUIP, true),
                booleanEntry("auto_adapt_curio_slot", IsaacConfigCategory.MISC,
                        Config.AUTO_ADAPT_CURIO_SLOT, true),
                booleanEntry("item_removal_from_pool", IsaacConfigCategory.MISC,
                        Config.ITEM_REMOVAL_FROM_POOL, false),
                booleanEntry("item_removal_from_all_pool", IsaacConfigCategory.MISC,
                        Config.ITEM_REMOVAL_FROM_ALL_POOL, false),
                booleanEntry("players_share_item_pools", IsaacConfigCategory.MISC,
                        Config.PLAYERS_SHARE_ITEM_POOLS, false),
                booleanEntry("auto_use_passive_item", IsaacConfigCategory.MISC,
                        Config.AUTO_USE_PASSIVE_ITEM, false)
        ));

        entries.addAll(Arrays.asList(
                stringEntry("coin_tier_1_id", IsaacConfigCategory.COINS, Config.COIN_TIER_1_ID,
                        "isaac_disaster:penny"),
                stringEntry("coin_tier_2_id", IsaacConfigCategory.COINS, Config.COIN_TIER_2_ID,
                        "isaac_disaster:nickel"),
                stringEntry("coin_tier_3_id", IsaacConfigCategory.COINS, Config.COIN_TIER_3_ID,
                        "isaac_disaster:dime"),
                intEntry("coin_tier_1_weight", IsaacConfigCategory.COINS, Config.COIN_TIER_1_WEIGHT, 93),
                intEntry("coin_tier_2_weight", IsaacConfigCategory.COINS, Config.COIN_TIER_2_WEIGHT, 6),
                intEntry("coin_tier_3_weight", IsaacConfigCategory.COINS, Config.COIN_TIER_3_WEIGHT, 1),
                intEntry("coin_tier_1_value", IsaacConfigCategory.COINS, Config.COIN_TIER_1_VALUE, 1),
                intEntry("coin_tier_2_value", IsaacConfigCategory.COINS, Config.COIN_TIER_2_VALUE, 5),
                intEntry("coin_tier_3_value", IsaacConfigCategory.COINS, Config.COIN_TIER_3_VALUE, 10)
        ));

        return List.copyOf(entries);
    }

    private static IsaacConfigEntry<Boolean> booleanEntry(String id, IsaacConfigCategory category,
                                                          net.minecraftforge.common.ForgeConfigSpec.BooleanValue value,
                                                          boolean defaultValue) {
        return new IsaacConfigEntry<>(id, category, value, IsaacConfigEntryType.BOOLEAN, defaultValue, false);
    }

    private static IsaacConfigEntry<Integer> intEntry(String id, IsaacConfigCategory category,
                                                      net.minecraftforge.common.ForgeConfigSpec.IntValue value,
                                                      int defaultValue) {
        return new IsaacConfigEntry<>(id, category, value, IsaacConfigEntryType.INTEGER, defaultValue, false);
    }

    private static IsaacConfigEntry<Double> doubleEntry(String id, IsaacConfigCategory category,
                                                        net.minecraftforge.common.ForgeConfigSpec.DoubleValue value,
                                                        double defaultValue) {
        return new IsaacConfigEntry<>(id, category, value, IsaacConfigEntryType.DOUBLE, defaultValue, false);
    }

    private static IsaacConfigEntry<String> stringEntry(String id, IsaacConfigCategory category,
                                                        net.minecraftforge.common.ForgeConfigSpec.ConfigValue<String> value,
                                                        String defaultValue) {
        return new IsaacConfigEntry<>(id, category, value, IsaacConfigEntryType.STRING, defaultValue, false);
    }
}

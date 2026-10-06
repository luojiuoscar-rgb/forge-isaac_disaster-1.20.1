package net.luojiuoscar.isaac_disaster.client.config;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.config.IsaacConfigDefaults;

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
                doubleEntry("default_max_health", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_MAX_HEALTH,
                        IsaacConfigDefaults.DEFAULT_MAX_HEALTH),
                doubleEntry("default_movement_speed", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_MOVEMENT_SPEED,
                        IsaacConfigDefaults.DEFAULT_MOVEMENT_SPEED),
                doubleEntry("default_attack_damage", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_ATTACK_DAMAGE,
                        IsaacConfigDefaults.DEFAULT_ATTACK_DAMAGE),
                doubleEntry("default_luck", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_LUCK,
                        IsaacConfigDefaults.DEFAULT_LUCK),
                doubleEntry("default_scale", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_SCALE,
                        IsaacConfigDefaults.DEFAULT_SCALE),
                doubleEntry("default_bullet_range", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_BULLET_RANGE,
                        IsaacConfigDefaults.DEFAULT_BULLET_RANGE),
                doubleEntry("default_entity_reach", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_ENTITY_REACH,
                        IsaacConfigDefaults.DEFAULT_ENTITY_REACH),
                doubleEntry("default_block_reach", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_BLOCK_REACH,
                        IsaacConfigDefaults.DEFAULT_BLOCK_REACH),
                doubleEntry("default_tears", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_TEARS,
                        IsaacConfigDefaults.DEFAULT_TEARS),
                doubleEntry("default_tears_correction", IsaacConfigCategory.PLAYER_STATS,
                        Config.DEFAULT_TEARS_CORRECTION, IsaacConfigDefaults.DEFAULT_TEARS_CORRECTION),
                doubleEntry("default_bullet_speed", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_BULLET_SPEED,
                        IsaacConfigDefaults.DEFAULT_BULLET_SPEED),
                doubleEntry("default_attack_speed", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_ATTACK_SPEED,
                        IsaacConfigDefaults.DEFAULT_ATTACK_SPEED),
                doubleEntry("default_block_breaking_speed", IsaacConfigCategory.PLAYER_STATS,
                        Config.DEFAULT_BLOCK_BREAKING_SPEED, IsaacConfigDefaults.DEFAULT_BLOCK_BREAKING_SPEED),
                doubleEntry("default_attack_knockback", IsaacConfigCategory.PLAYER_STATS,
                        Config.DEFAULT_ATTACK_KNOCKBACK, IsaacConfigDefaults.DEFAULT_ATTACK_KNOCKBACK),
                doubleEntry("default_bullet_scale", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_BULLET_SCALE,
                        IsaacConfigDefaults.DEFAULT_BULLET_SCALE),
                doubleEntry("default_bullet_count", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_BULLET_COUNT,
                        IsaacConfigDefaults.DEFAULT_BULLET_COUNT),
                doubleEntry("default_pill_quality", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_PILL_QUALITY,
                        IsaacConfigDefaults.DEFAULT_PILL_QUALITY),
                doubleEntry("default_fly_time", IsaacConfigCategory.PLAYER_STATS, Config.DEFAULT_FLY_TIME,
                        IsaacConfigDefaults.DEFAULT_FLY_TIME),
                doubleEntry("health_bonus", IsaacConfigCategory.PLAYER_STATS, Config.HEALTH_BONUS,
                        IsaacConfigDefaults.HEALTH_BONUS),
                doubleEntry("movement_speed_bonus", IsaacConfigCategory.PLAYER_STATS, Config.MOVEMENT_SPEED_BONUS,
                        IsaacConfigDefaults.MOVEMENT_SPEED_BONUS),
                doubleEntry("movement_speed_limit", IsaacConfigCategory.PLAYER_STATS, Config.MOVEMENT_SPEED_LIMIT,
                        IsaacConfigDefaults.MOVEMENT_SPEED_LIMIT),
                doubleEntry("damage_bonus", IsaacConfigCategory.PLAYER_STATS, Config.DAMAGE_BONUS,
                        IsaacConfigDefaults.DAMAGE_BONUS),
                doubleEntry("luck_bonus", IsaacConfigCategory.PLAYER_STATS, Config.LUCK_BONUS,
                        IsaacConfigDefaults.LUCK_BONUS),
                doubleEntry("fly_time", IsaacConfigCategory.PLAYER_STATS, Config.FLY_TIME,
                        IsaacConfigDefaults.FLY_TIME),
                doubleEntry("flight_speed_multiplier", IsaacConfigCategory.PLAYER_STATS,
                        Config.FLIGHT_SPEED_MULTIPLIER, IsaacConfigDefaults.FLIGHT_SPEED_MULTIPLIER),
                doubleEntry("flight_absolute_speed_cap", IsaacConfigCategory.PLAYER_STATS,
                        Config.FLIGHT_ABSOLUTE_SPEED_CAP, IsaacConfigDefaults.FLIGHT_ABSOLUTE_SPEED_CAP),
                doubleEntry("scale_bonus", IsaacConfigCategory.PLAYER_STATS, Config.SCALE_BONUS,
                        IsaacConfigDefaults.SCALE_BONUS),
                doubleEntry("range_bonus", IsaacConfigCategory.PLAYER_STATS, Config.RANGE_BONUS,
                        IsaacConfigDefaults.RANGE_BONUS),
                doubleEntry("entity_reach_bonus", IsaacConfigCategory.PLAYER_STATS, Config.ENTITY_REACH_BONUS,
                        IsaacConfigDefaults.ENTITY_REACH_BONUS),
                doubleEntry("block_reach_bonus", IsaacConfigCategory.PLAYER_STATS, Config.BLOCK_REACH_BONUS,
                        IsaacConfigDefaults.BLOCK_REACH_BONUS),
                doubleEntry("bullet_speed_bonus", IsaacConfigCategory.PLAYER_STATS, Config.BULLET_SPEED_BONUS,
                        IsaacConfigDefaults.BULLET_SPEED_BONUS),
                doubleEntry("tears_bonus", IsaacConfigCategory.PLAYER_STATS, Config.TEARS_BONUS,
                        IsaacConfigDefaults.TEARS_BONUS),
                doubleEntry("tears_correction_bonus", IsaacConfigCategory.PLAYER_STATS, Config.TEARS_CORRECTION_BONUS,
                        IsaacConfigDefaults.TEARS_CORRECTION_BONUS),
                doubleEntry("attack_speed_bonus", IsaacConfigCategory.PLAYER_STATS, Config.ATTACK_SPEED_BONUS,
                        IsaacConfigDefaults.ATTACK_SPEED_BONUS),
                doubleEntry("block_breaking_speed_bonus", IsaacConfigCategory.PLAYER_STATS, Config.BLOCK_BREAKING_SPEED_BONUS,
                        IsaacConfigDefaults.BLOCK_BREAKING_SPEED_BONUS),
                doubleEntry("bullet_scale_bonus", IsaacConfigCategory.PLAYER_STATS, Config.BULLET_SCALE_BONUS,
                        IsaacConfigDefaults.BULLET_SCALE_BONUS),
                doubleEntry("attack_knockback_bonus", IsaacConfigCategory.PLAYER_STATS, Config.ATTACK_KNOCKBACK_BONUS,
                        IsaacConfigDefaults.ATTACK_KNOCKBACK_BONUS),
                doubleEntry("nearby_range", IsaacConfigCategory.PLAYER_STATS, Config.NEARBY_RANGE,
                        IsaacConfigDefaults.NEARBY_RANGE),
                doubleEntry("basic_time_interval", IsaacConfigCategory.PLAYER_STATS, Config.BASIC_TIME_INTERVAL,
                        IsaacConfigDefaults.BASIC_TIME_INTERVAL)
        ));

        entries.addAll(Arrays.asList(
                intEntry("active_item_durability_restore_rate", IsaacConfigCategory.ITEM_RELATED,
                        Config.ACTIVE_ITEM_DURABILITY_RESTORE_RATE,
                        IsaacConfigDefaults.ACTIVE_ITEM_DURABILITY_RESTORE_RATE),
                booleanEntry("limited_active_item_durability_restore", IsaacConfigCategory.ITEM_RELATED,
                        Config.LIMITED_ACTIVE_ITEM_DURABILITY_RESTORE,
                        IsaacConfigDefaults.LIMITED_ACTIVE_ITEM_DURABILITY_RESTORE),
                booleanEntry("active_item_auto_restore", IsaacConfigCategory.ITEM_RELATED,
                        Config.ACTIVE_ITEM_AUTO_RESTORE, IsaacConfigDefaults.ACTIVE_ITEM_AUTO_RESTORE),
                doubleEntry("holy_shield_strength", IsaacConfigCategory.ITEM_RELATED,
                        Config.HOLY_SHIELD_STRENGTH, IsaacConfigDefaults.HOLY_SHIELD_STRENGTH),
                doubleEntry("money_is_power_strength", IsaacConfigCategory.ITEM_RELATED,
                        Config.MONEY_IS_POWER_STRENGTH, IsaacConfigDefaults.MONEY_IS_POWER_STRENGTH)
        ));

        entries.addAll(Arrays.asList(
                intEntry("passive_item_limit", IsaacConfigCategory.MISC, Config.PASSIVE_ITEM_LIMIT,
                        IsaacConfigDefaults.PASSIVE_ITEM_LIMIT),
                booleanEntry("extra_passive_item_backpack", IsaacConfigCategory.MISC,
                        Config.EXTRA_PASSIVE_ITEM_BACKPACK, IsaacConfigDefaults.EXTRA_PASSIVE_ITEM_BACKPACK),
                booleanEntry("allow_curio_unequip", IsaacConfigCategory.MISC, Config.ALLOW_CURIO_UNEQUIP,
                        IsaacConfigDefaults.ALLOW_CURIO_UNEQUIP),
                booleanEntry("auto_adapt_curio_slot", IsaacConfigCategory.MISC,
                        Config.AUTO_ADAPT_CURIO_SLOT, IsaacConfigDefaults.AUTO_ADAPT_CURIO_SLOT),
                booleanEntry("item_removal_from_pool", IsaacConfigCategory.MISC,
                        Config.ITEM_REMOVAL_FROM_POOL, IsaacConfigDefaults.ITEM_REMOVAL_FROM_POOL),
                booleanEntry("item_removal_from_all_pool", IsaacConfigCategory.MISC,
                        Config.ITEM_REMOVAL_FROM_ALL_POOL, IsaacConfigDefaults.ITEM_REMOVAL_FROM_ALL_POOL),
                booleanEntry("players_share_item_pools", IsaacConfigCategory.MISC,
                        Config.PLAYERS_SHARE_ITEM_POOLS, IsaacConfigDefaults.PLAYERS_SHARE_ITEM_POOLS),
                booleanEntry("auto_use_passive_item", IsaacConfigCategory.MISC,
                        Config.AUTO_USE_PASSIVE_ITEM, IsaacConfigDefaults.AUTO_USE_PASSIVE_ITEM),
                booleanEntry("time_stop_exclude_friendly", IsaacConfigCategory.MISC,
                        Config.TIME_STOP_EXCLUDE_FRIENDLY, IsaacConfigDefaults.TIME_STOP_EXCLUDE_FRIENDLY),
                booleanEntry("allow_same_multiplier_entry_stacking", IsaacConfigCategory.MISC,
                        Config.ALLOW_SAME_MULTIPLIER_ENTRY_STACKING,
                        IsaacConfigDefaults.ALLOW_SAME_MULTIPLIER_ENTRY_STACKING)
        ));

        entries.addAll(Arrays.asList(
                stringEntry("coin_tier_1_id", IsaacConfigCategory.COINS, Config.COIN_TIER_1_ID,
                        IsaacConfigDefaults.COIN_TIER_1_ID),
                stringEntry("coin_tier_2_id", IsaacConfigCategory.COINS, Config.COIN_TIER_2_ID,
                        IsaacConfigDefaults.COIN_TIER_2_ID),
                stringEntry("coin_tier_3_id", IsaacConfigCategory.COINS, Config.COIN_TIER_3_ID,
                        IsaacConfigDefaults.COIN_TIER_3_ID),
                intEntry("coin_tier_1_weight", IsaacConfigCategory.COINS, Config.COIN_TIER_1_WEIGHT,
                        IsaacConfigDefaults.COIN_TIER_1_WEIGHT),
                intEntry("coin_tier_2_weight", IsaacConfigCategory.COINS, Config.COIN_TIER_2_WEIGHT,
                        IsaacConfigDefaults.COIN_TIER_2_WEIGHT),
                intEntry("coin_tier_3_weight", IsaacConfigCategory.COINS, Config.COIN_TIER_3_WEIGHT,
                        IsaacConfigDefaults.COIN_TIER_3_WEIGHT),
                intEntry("coin_tier_1_value", IsaacConfigCategory.COINS, Config.COIN_TIER_1_VALUE,
                        IsaacConfigDefaults.COIN_TIER_1_VALUE),
                intEntry("coin_tier_2_value", IsaacConfigCategory.COINS, Config.COIN_TIER_2_VALUE,
                        IsaacConfigDefaults.COIN_TIER_2_VALUE),
                intEntry("coin_tier_3_value", IsaacConfigCategory.COINS, Config.COIN_TIER_3_VALUE,
                        IsaacConfigDefaults.COIN_TIER_3_VALUE)
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

package net.luojiuoscar.isaac_disaster.datapack.gameplay;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.luojiuoscar.isaac_disaster.Config;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Typed runtime handles. Ranges and defaults come from the existing COMMON schema. */
public final class GameplayConfig {
    private static final Map<String, ConfigKey<?>> KEYS = discover();
    public static final ConfigKey<String> MAGIC_NUMBER_INTRODUCTION = key("magicNumberIntroduction");
    public static final ConfigKey<Double> DEFAULT_MAX_HEALTH = key("default_max_health");
    public static final ConfigKey<Double> DEFAULT_MOVEMENT_SPEED = key("default_movement_speed");
    public static final ConfigKey<Double> DEFAULT_ATTACK_DAMAGE = key("default_attack_damage");
    public static final ConfigKey<Double> DEFAULT_LUCK = key("default_luck");
    public static final ConfigKey<Double> DEFAULT_SCALE = key("default_scale");
    public static final ConfigKey<Double> DEFAULT_BULLET_RANGE = key("default_bullet_range");
    public static final ConfigKey<Double> DEFAULT_ENTITY_REACH = key("default_entity_reach");
    public static final ConfigKey<Double> DEFAULT_BLOCK_REACH = key("default_block_reach");
    public static final ConfigKey<Double> DEFAULT_TEARS = key("default_tears");
    public static final ConfigKey<Double> DEFAULT_FIRE_RATE_ADDITION = key("default_fire_rate_addition");
    public static final ConfigKey<Double> DEFAULT_BULLET_SPEED = key("default_bullet_speed");
    public static final ConfigKey<Double> DEFAULT_ATTACK_SPEED = key("default_attack_speed");
    public static final ConfigKey<Double> DEFAULT_BLOCK_BREAKING_SPEED = key("default_block_breaking_speed");
    public static final ConfigKey<Double> DEFAULT_ATTACK_KNOCKBACK = key("default_attack_knockback");
    public static final ConfigKey<Double> DEFAULT_BULLET_SCALE = key("default_bullet_scale");
    public static final ConfigKey<Double> DEFAULT_BULLET_COUNT = key("default_bullet_count");
    public static final ConfigKey<Double> DEFAULT_PILL_QUALITY = key("default_pill_quality");
    public static final ConfigKey<Double> DEFAULT_FLY_TIME = key("default_fly_time");
    public static final ConfigKey<Integer> DEFAULT_TRINKET_SLOTS = key("default_trinket_slots");
    public static final ConfigKey<Integer> DEFAULT_PASSIVE_ITEM_SLOTS = key("default_passive_item_slots");
    public static final ConfigKey<Double> HEALTH_BONUS = key("health_bonus");
    public static final ConfigKey<Double> MOVEMENT_SPEED_BONUS = key("movement_speed_bonus");
    public static final ConfigKey<Double> MOVEMENT_SPEED_LIMIT = key("movement_speed_limit");
    public static final ConfigKey<Double> DAMAGE_BONUS = key("damage_bonus");
    public static final ConfigKey<Double> LUCK_BONUS = key("luck_bonus");
    public static final ConfigKey<Double> FLY_TIME = key("fly_time");
    public static final ConfigKey<Double> FLIGHT_SPEED_MULTIPLIER = key("flight_speed_multiplier");
    public static final ConfigKey<Double> FLIGHT_ABSOLUTE_SPEED_CAP = key("flight_absolute_speed_cap");
    public static final ConfigKey<Double> SCALE_BONUS = key("scale_bonus");
    public static final ConfigKey<Double> RANGE_BONUS = key("range_bonus");
    public static final ConfigKey<Double> BLOCK_REACH_BONUS = key("block_reach_bonus");
    public static final ConfigKey<Double> ENTITY_REACH_BONUS = key("entity_reach_bonus");
    public static final ConfigKey<Double> BULLET_SPEED_BONUS = key("bullet_speed_bonus");
    public static final ConfigKey<Double> TEARS_BONUS = key("tears_bonus");
    public static final ConfigKey<Double> FIRE_RATE_BONUS = key("fire_rate_bonus");
    public static final ConfigKey<Double> ATTACK_SPEED_BONUS = key("attack_speed_bonus");
    public static final ConfigKey<Double> BLOCK_BREAKING_SPEED_BONUS = key("block_breaking_speed_bonus");
    public static final ConfigKey<Double> ATTACK_KNOCKBACK_BONUS = key("attack_knockback_bonus");
    public static final ConfigKey<Double> BULLET_SCALE_BONUS = key("bullet_scale_bonus");
    public static final ConfigKey<Integer> MAX_ACTIVE_BULLETS = key("max_active_bullets");
    public static final ConfigKey<Integer> PASSIVE_ITEM_LIMIT = key("passive_item_limit");
    public static final ConfigKey<Double> NEARBY_RANGE = key("nearby_range");
    public static final ConfigKey<Double> BASIC_TIME_INTERVAL = key("basic_time_interval");
    public static final ConfigKey<Boolean> EXTRA_PASSIVE_ITEM_BACKPACK = key("extra_passive_item_backpack");
    public static final ConfigKey<Boolean> ALLOW_CURIO_UNEQUIP = key("allow_curio_unequip");
    public static final ConfigKey<Integer> ACTIVE_ITEM_DURABILITY_RESTORE_RATE = key("active_item_durability_restore_rate");
    public static final ConfigKey<Boolean> LIMITED_ACTIVE_ITEM_DURABILITY_RESTORE = key("limited_active_item_durability_restore");
    public static final ConfigKey<Boolean> ACTIVE_ITEM_AUTO_RESTORE = key("active_item_auto_restore");
    public static final ConfigKey<Boolean> ITEM_REMOVAL_FROM_POOL = key("item_removal_from_pool");
    public static final ConfigKey<Boolean> ITEM_REMOVAL_FROM_ALL_POOL = key("item_removal_from_all_pool");
    public static final ConfigKey<Boolean> PLAYERS_SHARE_ITEM_POOLS = key("players_share_item_pools");
    public static final ConfigKey<Boolean> AUTO_USE_PASSIVE_ITEM = key("auto_use_passive_item");
    public static final ConfigKey<Boolean> TIME_STOP_EXCLUDE_FRIENDLY = key("time_stop_exclude_friendly");
    public static final ConfigKey<Boolean> ALLOW_SAME_MULTIPLIER_ENTRY_STACKING = key("allow_same_multiplier_entry_stacking");
    public static final ConfigKey<String> COIN_TIER_1_ID = key("coin_tier_1_id");
    public static final ConfigKey<String> COIN_TIER_2_ID = key("coin_tier_2_id");
    public static final ConfigKey<String> COIN_TIER_3_ID = key("coin_tier_3_id");
    public static final ConfigKey<Integer> COIN_TIER_1_VALUE = key("coin_tier_1_value");
    public static final ConfigKey<Integer> COIN_TIER_2_VALUE = key("coin_tier_2_value");
    public static final ConfigKey<Integer> COIN_TIER_3_VALUE = key("coin_tier_3_value");

    private GameplayConfig() {}

    public static Map<String, ConfigKey<?>> keys() { return KEYS; }

    @SuppressWarnings("unchecked")
    private static <T> ConfigKey<T> key(String id) { return (ConfigKey<T>) KEYS.get(id); }

    private static Map<String, ConfigKey<?>> discover() {
        Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();
        collect(Config.spec().getValues(), keys);
        return Collections.unmodifiableMap(keys);
    }

    private static void collect(UnmodifiableConfig node, Map<String, ConfigKey<?>> keys) {
        node.valueMap().values().forEach(value -> {
            if (value instanceof UnmodifiableConfig nested) collect(nested, keys);
            else if (value instanceof ForgeConfigSpec.ConfigValue<?> setting) {
                String id = setting.getPath().get(setting.getPath().size() - 1);
                ForgeConfigSpec.ValueSpec spec = Config.spec().getSpec().get(setting.getPath());
                if (keys.putIfAbsent(id, new ConfigKey<>(id, setting, spec)) != null) {
                    throw new IllegalStateException("Duplicate datapack setting: " + id);
                }
            }
        });
    }
}

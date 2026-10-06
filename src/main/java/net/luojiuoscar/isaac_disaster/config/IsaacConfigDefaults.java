package net.luojiuoscar.isaac_disaster.config;

/**
 * Canonical defaults shared by Forge config registration and the config screen.
 */
public final class IsaacConfigDefaults {
    public static final double DEFAULT_MAX_HEALTH = 20.0;
    public static final double DEFAULT_MOVEMENT_SPEED = 0.1;
    public static final double DEFAULT_ATTACK_DAMAGE = 1.0;
    public static final double DEFAULT_LUCK = 0.0;
    public static final double DEFAULT_SCALE = 1.0;
    public static final double DEFAULT_BULLET_RANGE = 12.0;
    public static final double DEFAULT_ENTITY_REACH = 3.0;
    public static final double DEFAULT_BLOCK_REACH = 4.5;
    public static final double DEFAULT_TEARS = 0.0;
    public static final double DEFAULT_TEARS_CORRECTION = 0.0;
    public static final double DEFAULT_BULLET_SPEED = 1.0;
    public static final double DEFAULT_ATTACK_SPEED = 4.0;
    public static final double DEFAULT_BLOCK_BREAKING_SPEED = 0.0;
    public static final double DEFAULT_ATTACK_KNOCKBACK = 0.0;
    public static final double DEFAULT_BULLET_SCALE = 0.0;
    public static final double DEFAULT_BULLET_COUNT = 1.0;
    public static final double DEFAULT_PILL_QUALITY = 0.0;
    public static final double DEFAULT_FLY_TIME = 0.0;
    public static final double HEALTH_BONUS = 10.0;
    public static final double MOVEMENT_SPEED_BONUS = 0.02;
    public static final double MOVEMENT_SPEED_LIMIT = 0.1;
    public static final double DAMAGE_BONUS = 2.0;
    public static final double LUCK_BONUS = 1.0;
    public static final double FLY_TIME = 100.0;
    public static final double FLIGHT_SPEED_MULTIPLIER = 2.0;
    public static final double FLIGHT_ABSOLUTE_SPEED_CAP = 1.0;
    public static final double SCALE_BONUS = 0.1;
    public static final double RANGE_BONUS = 3;
    public static final double ENTITY_REACH_BONUS = 0.5;
    public static final double BLOCK_REACH_BONUS = 1.0;
    public static final double BULLET_SPEED_BONUS = 0.2;
    public static final double TEARS_BONUS = 0.7;
    public static final double TEARS_CORRECTION_BONUS = 1.0;
    public static final double ATTACK_SPEED_BONUS = 1.0;
    public static final double BLOCK_BREAKING_SPEED_BONUS = 5.0;
    public static final double BULLET_SCALE_BONUS = 0.1;
    public static final double ATTACK_KNOCKBACK_BONUS = 0.5;
    public static final double NEARBY_RANGE = 12.0;
    public static final double BASIC_TIME_INTERVAL = 10.0;

    public static final int ACTIVE_ITEM_DURABILITY_RESTORE_RATE = 4;
    public static final boolean LIMITED_ACTIVE_ITEM_DURABILITY_RESTORE = false;
    public static final boolean ACTIVE_ITEM_AUTO_RESTORE = true;
    public static final double HOLY_SHIELD_STRENGTH = 3.0;
    public static final double MONEY_IS_POWER_STRENGTH = 0.007;

    public static final int PASSIVE_ITEM_LIMIT = 999;
    public static final boolean EXTRA_PASSIVE_ITEM_BACKPACK = false;
    public static final boolean ALLOW_CURIO_UNEQUIP = true;
    public static final boolean AUTO_ADAPT_CURIO_SLOT = true;
    public static final boolean ITEM_REMOVAL_FROM_POOL = false;
    public static final boolean ITEM_REMOVAL_FROM_ALL_POOL = false;
    public static final boolean PLAYERS_SHARE_ITEM_POOLS = false;
    public static final boolean AUTO_USE_PASSIVE_ITEM = false;
    public static final boolean TIME_STOP_EXCLUDE_FRIENDLY = false;
    public static final boolean ALLOW_SAME_MULTIPLIER_ENTRY_STACKING = false;

    public static final String COIN_TIER_1_ID = "isaac_disaster:penny";
    public static final String COIN_TIER_2_ID = "isaac_disaster:nickel";
    public static final String COIN_TIER_3_ID = "isaac_disaster:dime";
    public static final int COIN_TIER_1_WEIGHT = 93;
    public static final int COIN_TIER_2_WEIGHT = 6;
    public static final int COIN_TIER_3_WEIGHT = 1;
    public static final int COIN_TIER_1_VALUE = 1;
    public static final int COIN_TIER_2_VALUE = 5;
    public static final int COIN_TIER_3_VALUE = 10;

    private IsaacConfigDefaults() {
    }
}

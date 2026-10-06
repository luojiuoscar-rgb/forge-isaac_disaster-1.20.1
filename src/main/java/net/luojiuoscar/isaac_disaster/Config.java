package net.luojiuoscar.isaac_disaster;

import net.luojiuoscar.isaac_disaster.config.IsaacConfigDefaults;
import net.luojiuoscar.isaac_disaster.manager.DefaultAttributeManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.luojiuoscar.isaac_disaster.IsaacDisaster.MOD_ID;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);




    // 可配置的属性
    public static ForgeConfigSpec.DoubleValue DEFAULT_MAX_HEALTH;
    public static ForgeConfigSpec.DoubleValue DEFAULT_MOVEMENT_SPEED;
    public static ForgeConfigSpec.DoubleValue DEFAULT_ATTACK_DAMAGE;
    public static ForgeConfigSpec.DoubleValue DEFAULT_LUCK;
    public static ForgeConfigSpec.DoubleValue DEFAULT_SCALE;
    public static ForgeConfigSpec.DoubleValue DEFAULT_BULLET_RANGE;
    public static ForgeConfigSpec.DoubleValue DEFAULT_ENTITY_REACH;
    public static ForgeConfigSpec.DoubleValue DEFAULT_BLOCK_REACH;
    public static ForgeConfigSpec.DoubleValue DEFAULT_TEARS;
    public static ForgeConfigSpec.DoubleValue DEFAULT_TEARS_CORRECTION;
    public static ForgeConfigSpec.DoubleValue DEFAULT_BULLET_SPEED;
    public static ForgeConfigSpec.DoubleValue DEFAULT_ATTACK_SPEED;
    public static ForgeConfigSpec.DoubleValue DEFAULT_BLOCK_BREAKING_SPEED;
    public static ForgeConfigSpec.DoubleValue DEFAULT_ATTACK_KNOCKBACK;
    public static ForgeConfigSpec.DoubleValue DEFAULT_BULLET_SCALE;
    public static ForgeConfigSpec.DoubleValue DEFAULT_BULLET_COUNT;
    public static ForgeConfigSpec.DoubleValue DEFAULT_PILL_QUALITY;
    public static ForgeConfigSpec.DoubleValue DEFAULT_FLY_TIME;
    public static ForgeConfigSpec.DoubleValue HEALTH_BONUS;
    public static ForgeConfigSpec.DoubleValue MOVEMENT_SPEED_BONUS;
    public static ForgeConfigSpec.DoubleValue MOVEMENT_SPEED_LIMIT;
    public static ForgeConfigSpec.DoubleValue DAMAGE_BONUS;
    public static ForgeConfigSpec.DoubleValue LUCK_BONUS;
    public static ForgeConfigSpec.DoubleValue FLY_TIME;
    public static ForgeConfigSpec.DoubleValue FLIGHT_SPEED_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue FLIGHT_ABSOLUTE_SPEED_CAP;
    public static ForgeConfigSpec.DoubleValue SCALE_BONUS;
    public static ForgeConfigSpec.DoubleValue RANGE_BONUS;
    public static ForgeConfigSpec.DoubleValue BLOCK_REACH_BONUS;
    public static ForgeConfigSpec.DoubleValue ENTITY_REACH_BONUS;
    public static ForgeConfigSpec.DoubleValue BULLET_SPEED_BONUS;
    public static ForgeConfigSpec.DoubleValue TEARS_BONUS;
    public static ForgeConfigSpec.DoubleValue TEARS_CORRECTION_BONUS;
    public static ForgeConfigSpec.DoubleValue ATTACK_SPEED_BONUS;
    public static ForgeConfigSpec.DoubleValue BLOCK_BREAKING_SPEED_BONUS;
    public static ForgeConfigSpec.DoubleValue ATTACK_KNOCKBACK_BONUS;
    public static ForgeConfigSpec.DoubleValue BULLET_SCALE_BONUS;

    // 其他可配置项目
    public static ForgeConfigSpec.IntValue PASSIVE_ITEM_LIMIT;
    public static ForgeConfigSpec.DoubleValue NEARBY_RANGE;
    public static ForgeConfigSpec.DoubleValue BASIC_TIME_INTERVAL;
    public static ForgeConfigSpec.DoubleValue HOLY_SHIELD_STRENGTH;
    public static ForgeConfigSpec.DoubleValue MONEY_IS_POWER_STRENGTH;
    public static ForgeConfigSpec.BooleanValue EXTRA_PASSIVE_ITEM_BACKPACK;
    public static ForgeConfigSpec.BooleanValue ALLOW_CURIO_UNEQUIP;
    public static ForgeConfigSpec.BooleanValue AUTO_ADAPT_CURIO_SLOT;
    public static ForgeConfigSpec.IntValue ACTIVE_ITEM_DURABILITY_RESTORE_RATE;
    public static ForgeConfigSpec.BooleanValue LIMITED_ACTIVE_ITEM_DURABILITY_RESTORE;
    public static ForgeConfigSpec.BooleanValue ACTIVE_ITEM_AUTO_RESTORE;
    public static ForgeConfigSpec.BooleanValue ITEM_REMOVAL_FROM_POOL;
    public static ForgeConfigSpec.BooleanValue ITEM_REMOVAL_FROM_ALL_POOL;
    public static ForgeConfigSpec.BooleanValue PLAYERS_SHARE_ITEM_POOLS;
    public static ForgeConfigSpec.BooleanValue AUTO_USE_PASSIVE_ITEM;
    public static ForgeConfigSpec.BooleanValue TIME_STOP_EXCLUDE_FRIENDLY;
    public static ForgeConfigSpec.BooleanValue ALLOW_SAME_MULTIPLIER_ENTRY_STACKING;

    // 临时
    public static ForgeConfigSpec.BooleanValue ENABLE_WANDERING_TRADER_SHOP;

    // 钱币
    public static ForgeConfigSpec.ConfigValue<String> COIN_TIER_1_ID;
    public static ForgeConfigSpec.ConfigValue<String> COIN_TIER_2_ID;
    public static ForgeConfigSpec.ConfigValue<String> COIN_TIER_3_ID;

    public static ForgeConfigSpec.IntValue COIN_TIER_1_WEIGHT;
    public static ForgeConfigSpec.IntValue COIN_TIER_2_WEIGHT;
    public static ForgeConfigSpec.IntValue COIN_TIER_3_WEIGHT;
    public static ForgeConfigSpec.IntValue COIN_TIER_1_VALUE;
    public static ForgeConfigSpec.IntValue COIN_TIER_2_VALUE;
    public static ForgeConfigSpec.IntValue COIN_TIER_3_VALUE;

    static {
        // 配置数值的默认值和范围
        BUILDER.push("Player Defaults");

        DEFAULT_MAX_HEALTH = BUILDER
                .comment("Default player maximum health. One unit equals one health point.")
                .defineInRange("default_max_health", IsaacConfigDefaults.DEFAULT_MAX_HEALTH, 1.0, 99999.0);

        DEFAULT_MOVEMENT_SPEED = BUILDER
                .comment("Default player movement speed.")
                .defineInRange("default_movement_speed", IsaacConfigDefaults.DEFAULT_MOVEMENT_SPEED, 0.0, 99999.0);

        DEFAULT_ATTACK_DAMAGE = BUILDER
                .comment("Default player attack damage.")
                .defineInRange("default_attack_damage", IsaacConfigDefaults.DEFAULT_ATTACK_DAMAGE, 0.0, 99999.0);

        DEFAULT_LUCK = BUILDER
                .comment("Default player luck.")
                .defineInRange("default_luck", IsaacConfigDefaults.DEFAULT_LUCK, -99999.0, 99999.0);

        DEFAULT_SCALE = BUILDER
                .comment("Default player scale.")
                .defineInRange("default_scale", IsaacConfigDefaults.DEFAULT_SCALE, 0.2, 8.0);

        DEFAULT_BULLET_RANGE = BUILDER
                .comment("Default player bullet range.")
                .defineInRange("default_bullet_range", IsaacConfigDefaults.DEFAULT_BULLET_RANGE, -1024.0, 1024.0);

        DEFAULT_ENTITY_REACH = BUILDER
                .comment("Default player entity reach.")
                .defineInRange("default_entity_reach", IsaacConfigDefaults.DEFAULT_ENTITY_REACH, -2.0, 99999.0);

        DEFAULT_BLOCK_REACH = BUILDER
                .comment("Default player block reach.")
                .defineInRange("default_block_reach", IsaacConfigDefaults.DEFAULT_BLOCK_REACH, -2.0, 99999.0);

        DEFAULT_TEARS = BUILDER
                .comment("Default player tears value.")
                .defineInRange("default_tears", IsaacConfigDefaults.DEFAULT_TEARS, -1024.0, 1024.0);

        DEFAULT_TEARS_CORRECTION = BUILDER
                .comment("Default player tears correction.")
                .defineInRange("default_tears_correction", IsaacConfigDefaults.DEFAULT_TEARS_CORRECTION,
                        -1024.0, 1024.0);

        DEFAULT_BULLET_SPEED = BUILDER
                .comment("Default player bullet speed.")
                .defineInRange("default_bullet_speed", IsaacConfigDefaults.DEFAULT_BULLET_SPEED, -1024.0, 1024.0);

        DEFAULT_ATTACK_SPEED = BUILDER
                .comment("Default player attack speed.")
                .defineInRange("default_attack_speed", IsaacConfigDefaults.DEFAULT_ATTACK_SPEED, 0.0, 99999.0);

        DEFAULT_BLOCK_BREAKING_SPEED = BUILDER
                .comment("Default player block breaking speed.")
                .defineInRange("default_block_breaking_speed", IsaacConfigDefaults.DEFAULT_BLOCK_BREAKING_SPEED,
                        -1024.0, 1024.0);

        DEFAULT_ATTACK_KNOCKBACK = BUILDER
                .comment("Default player attack knockback.")
                .defineInRange("default_attack_knockback", IsaacConfigDefaults.DEFAULT_ATTACK_KNOCKBACK,
                        -99999.0, 99999.0);

        DEFAULT_BULLET_SCALE = BUILDER
                .comment("Default player bullet scale.")
                .defineInRange("default_bullet_scale", IsaacConfigDefaults.DEFAULT_BULLET_SCALE, -1024.0, 1024.0);

        DEFAULT_BULLET_COUNT = BUILDER
                .comment("Default player bullet count.")
                .defineInRange("default_bullet_count", IsaacConfigDefaults.DEFAULT_BULLET_COUNT, 1.0, 1024.0);

        DEFAULT_PILL_QUALITY = BUILDER
                .comment("Default player pill quality.")
                .defineInRange("default_pill_quality", IsaacConfigDefaults.DEFAULT_PILL_QUALITY, -1024.0, 1024.0);

        DEFAULT_FLY_TIME = BUILDER
                .comment("Default player flight time.")
                .defineInRange("default_fly_time", IsaacConfigDefaults.DEFAULT_FLY_TIME, 0.0, 1024.0);

        BUILDER.pop();

        BUILDER.push("Player Stats"); // 配置分组
        // 生命值增量基准  默认10
        HEALTH_BONUS = BUILDER
                .comment("Base value of health increment")
                .defineInRange("health_bonus", IsaacConfigDefaults.HEALTH_BONUS, 1, 99999);
        // 移动速度基准值  默认0.02
        MOVEMENT_SPEED_BONUS = BUILDER
                .comment("Base value of movement speed increment")
                .defineInRange("movement_speed_bonus", IsaacConfigDefaults.MOVEMENT_SPEED_BONUS, 0.0, 99999.0);
        // 移动速度最大值  默认0.1
        MOVEMENT_SPEED_LIMIT = BUILDER
                .comment("Limitation of movement speed bonus value")
                .defineInRange("movement_speed_limit", IsaacConfigDefaults.MOVEMENT_SPEED_LIMIT, 0.0, 99999.0);
        // 攻击伤害  默认2.0
        DAMAGE_BONUS = BUILDER
                .comment("Base value of attack damage increment")
                .defineInRange("damage_bonus", IsaacConfigDefaults.DAMAGE_BONUS, 0.0, 99999.0);
        // 幸运值  默认1.0
        LUCK_BONUS = BUILDER
                .comment("Base value of luck increment")
                .defineInRange("luck_bonus", IsaacConfigDefaults.LUCK_BONUS, 0.0, 99999.0);

        // 飞行时间  默认100.0
        FLY_TIME = BUILDER
                .comment("Increment of fly time for each fly provided by item (tick)")
                .defineInRange("fly_time", IsaacConfigDefaults.FLY_TIME, 0, 99999);

        FLIGHT_SPEED_MULTIPLIER = BUILDER
                .comment("Isaac flight speed as a multiplier of the player's movement speed")
                .defineInRange("flight_speed_multiplier", IsaacConfigDefaults.FLIGHT_SPEED_MULTIPLIER, 0.0, 100.0);
        FLIGHT_ABSOLUTE_SPEED_CAP = BUILDER
                .comment("Absolute per-tick speed cap for Isaac flight")
                .defineInRange("flight_absolute_speed_cap", IsaacConfigDefaults.FLIGHT_ABSOLUTE_SPEED_CAP, 0.0, 100.0);

        // 体型  默认0.1
        SCALE_BONUS = BUILDER
                .comment("Base value of scale increment")
                .defineInRange("scale_bonus", IsaacConfigDefaults.SCALE_BONUS, 0.0, 99999.0);

        // 射程  默认0.1
        RANGE_BONUS = BUILDER
                .comment("Base value of range increment")
                .defineInRange("range_bonus", IsaacConfigDefaults.RANGE_BONUS, 0.0, 99999.0);

        // 实体触及距离  默认0.1
        ENTITY_REACH_BONUS = BUILDER
                .comment("Base value of entity reach increment")
                .defineInRange("entity_reach_bonus", IsaacConfigDefaults.ENTITY_REACH_BONUS, 0.0, 99999.0);

        // 方块触及距离  默认0.1
        BLOCK_REACH_BONUS = BUILDER
                .comment("Base value of block reach increment")
                .defineInRange("block_reach_bonus", IsaacConfigDefaults.BLOCK_REACH_BONUS, 0.0, 99999.0);

        // 方块触及距离  默认0.2
        BULLET_SPEED_BONUS = BUILDER
                .comment("Base value of bullet speed increment")
                .defineInRange("bullet_speed_bonus", IsaacConfigDefaults.BULLET_SPEED_BONUS, 0.0, 99999.0);

        // 射速  默认0.7
        TEARS_BONUS = BUILDER
                .comment("Base value of tears increment")
                .defineInRange("tears_bonus", IsaacConfigDefaults.TEARS_BONUS, 0.0, 99999.0);

        // 射速修正  默认1
        TEARS_CORRECTION_BONUS = BUILDER
                .comment("Reduction of shot delay")
                .defineInRange("tears_correction_bonus", IsaacConfigDefaults.TEARS_CORRECTION_BONUS, 0.0, 99999.0);

        // 攻击速度  默认1
        ATTACK_SPEED_BONUS = BUILDER
                .comment("Base value of attack speed increment")
                .defineInRange("attack_speed_bonus", IsaacConfigDefaults.ATTACK_SPEED_BONUS, 0.0, 99999.0);

        // 方块破坏速度（最终倍率）  默认5
        BLOCK_BREAKING_SPEED_BONUS = BUILDER
                .comment("Base value of block breaking speed increment")
                .defineInRange("block_breaking_speed_bonus",
                        IsaacConfigDefaults.BLOCK_BREAKING_SPEED_BONUS, 0.0, 99999.0);

        // 额外子弹大小（倍率）  默认0.1f
        BULLET_SCALE_BONUS = BUILDER
                .comment("Base value of bullet scale increment")
                .defineInRange("bullet_scale_bonus", IsaacConfigDefaults.BULLET_SCALE_BONUS, 0.0, 99999.0);

        // 攻击击退  默认0.5
        ATTACK_KNOCKBACK_BONUS = BUILDER
                .comment("Base value of attack knockback increment")
                .defineInRange("attack_knockback_bonus", IsaacConfigDefaults.ATTACK_KNOCKBACK_BONUS, 0.0, 99999.0);

        // 周围（定义周围的范围）  默认12
        NEARBY_RANGE = BUILDER
                .comment("Defines the range of NEARBY." +
                        "Affects most items with a description of NEARBY")
                .defineInRange("nearby_range", IsaacConfigDefaults.NEARBY_RANGE, 0.0, 99999.0);

        // 基础单次时间间隔
        BASIC_TIME_INTERVAL = BUILDER
                .comment("Defines time interval for items with recursive effects.(s)")
                .defineInRange("basic_time_interval", IsaacConfigDefaults.BASIC_TIME_INTERVAL, 0.0, 99999.0);


        BUILDER.pop();
    }
    static {
        BUILDER.push("Item related");

        ACTIVE_ITEM_DURABILITY_RESTORE_RATE = BUILDER
                .comment("How many durability is restored every 4 ticks (0.2 seconds)")
                .defineInRange("active_item_durability_restore_rate",
                        IsaacConfigDefaults.ACTIVE_ITEM_DURABILITY_RESTORE_RATE, 0, 99999);

        LIMITED_ACTIVE_ITEM_DURABILITY_RESTORE = BUILDER
                .comment("Active item will auto restore durability ONLY in player's offhand and mainhand.")
                .define("limited_active_item_durability_restore",
                        IsaacConfigDefaults.LIMITED_ACTIVE_ITEM_DURABILITY_RESTORE);

        ACTIVE_ITEM_AUTO_RESTORE = BUILDER
                .comment("Active item will auto restore durability with time.")
                .define("active_item_auto_restore", IsaacConfigDefaults.ACTIVE_ITEM_AUTO_RESTORE);

        // 神圣护盾强度  默认3
        HOLY_SHIELD_STRENGTH = BUILDER
                .comment("Amount * (Amplifier + 1);" +
                        "Damages that holy shield effect can immune.")
                .defineInRange("holy_shield_strength", IsaacConfigDefaults.HOLY_SHIELD_STRENGTH, 0.0, 99999.0);

        // 钱力强度  默认0.008
        MONEY_IS_POWER_STRENGTH = BUILDER
                .comment("Damage increment of money is power. (each coin)")
                .defineInRange("money_is_power_strength", IsaacConfigDefaults.MONEY_IS_POWER_STRENGTH, 0.0, 99999.0);


        BUILDER.pop();
    }
    static {
        BUILDER.push("Misc");
        // 可携带的道具总数  默认999
        PASSIVE_ITEM_LIMIT = BUILDER
                .comment("How many passive items a player can carry in the extra backpack.")
                .defineInRange("passive_item_limit", IsaacConfigDefaults.PASSIVE_ITEM_LIMIT, 1, 99999);

        EXTRA_PASSIVE_ITEM_BACKPACK = BUILDER
                .comment("Enable a extra passive item backpack for each player." +
                        "Player can store their items into this backpack through a right-click")
                .define("extra_passive_item_backpack", IsaacConfigDefaults.EXTRA_PASSIVE_ITEM_BACKPACK);

        ALLOW_CURIO_UNEQUIP = BUILDER
                .comment("If the item can be unequipped from curio slot")
                .define("allow_curio_unequip", IsaacConfigDefaults.ALLOW_CURIO_UNEQUIP);

        AUTO_ADAPT_CURIO_SLOT = BUILDER
                .comment("Clamp the extra Curios slot count to zero when item effects try to remove more slots than the player has.")
                .define("auto_adapt_curio_slot", IsaacConfigDefaults.AUTO_ADAPT_CURIO_SLOT);

        ITEM_REMOVAL_FROM_POOL = BUILDER
                .comment("Item will be removed from the exact pool when it spawns.")
                .define("item_removal_from_pool", IsaacConfigDefaults.ITEM_REMOVAL_FROM_POOL);

        ITEM_REMOVAL_FROM_ALL_POOL = BUILDER
                .comment("Item will be removed from the ALL pools when it spawns." +
                        "This option will overrides \"item_removal_from_pool\" when activate.")
                .define("item_removal_from_all_pool", IsaacConfigDefaults.ITEM_REMOVAL_FROM_ALL_POOL);

        PLAYERS_SHARE_ITEM_POOLS = BUILDER
                .comment("All player shares the same item pool.")
                .define("players_share_item_pools", IsaacConfigDefaults.PLAYERS_SHARE_ITEM_POOLS);

        AUTO_USE_PASSIVE_ITEM = BUILDER
                .comment("Automatically use passive item once it is acquired by player.")
                .define("auto_use_passive_item", IsaacConfigDefaults.AUTO_USE_PASSIVE_ITEM);

        TIME_STOP_EXCLUDE_FRIENDLY = BUILDER
                .comment("Whether time stop excludes entities friendly to at least one time stop source.")
                .define("time_stop_exclude_friendly", IsaacConfigDefaults.TIME_STOP_EXCLUDE_FRIENDLY);

        ALLOW_SAME_MULTIPLIER_ENTRY_STACKING = BUILDER
                .comment("Allow each copy of a source-owned multiplier entry to apply independently.")
                .define("allow_same_multiplier_entry_stacking",
                        IsaacConfigDefaults.ALLOW_SAME_MULTIPLIER_ENTRY_STACKING);

        BUILDER.pop();
    }
    static {
        BUILDER.push("Coins");

        // 钱币物品 ID
        COIN_TIER_1_ID = BUILDER
                .comment("Item ID for Tier 1 Coin (e.g., isaac_disaster:penny)")
                .define("coin_tier_1_id", IsaacConfigDefaults.COIN_TIER_1_ID);

        COIN_TIER_2_ID = BUILDER
                .comment("Item ID for Tier 2 Coin (e.g., isaac_disaster:nickel)")
                .define("coin_tier_2_id", IsaacConfigDefaults.COIN_TIER_2_ID);

        COIN_TIER_3_ID = BUILDER
                .comment("Item ID for Tier 3 Coin (e.g., isaac_disaster:dime)")
                .define("coin_tier_3_id", IsaacConfigDefaults.COIN_TIER_3_ID);

        // 权重定义（用于战利品表动态概率）
        COIN_TIER_1_WEIGHT = BUILDER
                .comment("Weight for Tier 1 Coin")
                .defineInRange("coin_tier_1_weight", IsaacConfigDefaults.COIN_TIER_1_WEIGHT, 0, 99999);

        COIN_TIER_2_WEIGHT = BUILDER
                .comment("Weight for Tier 2 Coin")
                .defineInRange("coin_tier_2_weight", IsaacConfigDefaults.COIN_TIER_2_WEIGHT, 0, 99999);

        COIN_TIER_3_WEIGHT = BUILDER
                .comment("Weight for Tier 3 Coin")
                .defineInRange("coin_tier_3_weight", IsaacConfigDefaults.COIN_TIER_3_WEIGHT, 0, 99999);

        // 价值定义
        COIN_TIER_1_VALUE = BUILDER
                .comment("Value for Tier 1 Coin")
                .defineInRange("coin_tier_1_value", IsaacConfigDefaults.COIN_TIER_1_VALUE, 0, 99999);

        COIN_TIER_2_VALUE = BUILDER
                .comment("Value for Tier 2 Coin")
                .defineInRange("coin_tier_2_value", IsaacConfigDefaults.COIN_TIER_2_VALUE, 0, 99999);

        COIN_TIER_3_VALUE = BUILDER
                .comment("Value for Tier 3 Coin")
                .defineInRange("coin_tier_3_value", IsaacConfigDefaults.COIN_TIER_3_VALUE, 0, 99999);

        BUILDER.pop();
    }



    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(ResourceLocation.parse(itemName));
    }

    /**
     * Persists the current common config values to disk.
     */
    public static void save() {
        SPEC.save();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == SPEC) {
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) server.execute(() -> server.getPlayerList().getPlayers()
                    .forEach(player -> {
                        DefaultAttributeManager.apply(player);
                        StatManager.refreshMultipliers(player);
                    }));
        }
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());
    }
}

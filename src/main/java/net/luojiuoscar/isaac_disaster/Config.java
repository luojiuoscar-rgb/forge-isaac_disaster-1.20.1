package net.luojiuoscar.isaac_disaster;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

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
    public static ForgeConfigSpec.DoubleValue HEALTH_BONUS;
    public static ForgeConfigSpec.DoubleValue MOVEMENT_SPEED_BONUS;
    public static ForgeConfigSpec.DoubleValue MOVEMENT_SPEED_LIMIT;
    public static ForgeConfigSpec.DoubleValue DAMAGE_BONUS;
    public static ForgeConfigSpec.DoubleValue LUCK_BONUS;
    public static ForgeConfigSpec.DoubleValue FLY_TIME;
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
    public static ForgeConfigSpec.DoubleValue DAMAGE_MULTIPLIER_BASE;
    public static ForgeConfigSpec.DoubleValue NEARBY_RANGE;
    public static ForgeConfigSpec.DoubleValue HOLY_SHIELD_STRENGTH;
    public static ForgeConfigSpec.DoubleValue MONEY_IS_POWER_STRENGTH;
    public static ForgeConfigSpec.BooleanValue USABLE_PASSIVE_ITEM;
    public static ForgeConfigSpec.BooleanValue ALLOW_CURIO_UNEQUIP;
    public static ForgeConfigSpec.BooleanValue AUTO_ADAPT_CURIO_SLOT;

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
        BUILDER.push("Passive Item Stats"); // 配置分组
        // 生命值增量基准  默认10
        HEALTH_BONUS = BUILDER
                .comment("Base value of health increment")
                .defineInRange("health_bonus", 10.0, 1, 99999);
        // 移动速度基准值  默认0.02
        MOVEMENT_SPEED_BONUS = BUILDER
                .comment("Base value of movement speed increment")
                .defineInRange("movement_speed_bonus", 0.02, 0.0, 99999.0);
        // 移动速度最大值  默认0.1
        MOVEMENT_SPEED_LIMIT = BUILDER
                .comment("Limitation of movement speed bonus value")
                .defineInRange("movement_speed_limit", 0.1, 0.0, 99999.0);
        // 攻击伤害  默认2.0
        DAMAGE_BONUS = BUILDER
                .comment("Base value of attack damage increment")
                .defineInRange("damage_bonus", 2.0, 0.0, 99999.0);
        // 幸运值  默认1.0
        LUCK_BONUS = BUILDER
                .comment("Base value of luck increment")
                .defineInRange("luck_bonus", 1.0, 0.0, 99999.0);

        // 飞行时间  默认100.0
        FLY_TIME = BUILDER
                .comment("Increment of fly time for each fly provided by item (tick)")
                .defineInRange("fly_time", 100.0, 0, 99999);

        // 体型  默认0.1
        SCALE_BONUS = BUILDER
                .comment("Base value of scale increment")
                .defineInRange("scale_bonus", 0.1, 0.0, 99999.0);

        // 射程  默认0.1
        RANGE_BONUS = BUILDER
                .comment("Base value of range increment")
                .defineInRange("range_bonus", 4.5, 0.0, 99999.0);

        // 实体触及距离  默认0.1
        ENTITY_REACH_BONUS = BUILDER
                .comment("Base value of entity reach increment")
                .defineInRange("entity_reach_bonus", 0.5, 0.0, 99999.0);

        // 方块触及距离  默认0.1
        BLOCK_REACH_BONUS = BUILDER
                .comment("Base value of block reach increment")
                .defineInRange("block_reach_bonus", 1.0, 0.0, 99999.0);

        // 方块触及距离  默认0.2
        BULLET_SPEED_BONUS = BUILDER
                .comment("Base value of bullet speed increment")
                .defineInRange("bullet_speed_bonus", 0.2, 0.0, 99999.0);

        // 射速  默认0.7
        TEARS_BONUS = BUILDER
                .comment("Base value of tears increment")
                .defineInRange("tears_bonus", 0.7, 0.0, 99999.0);

        // 射速修正  默认1
        TEARS_CORRECTION_BONUS = BUILDER
                .comment("Base value of tears correction increment")
                .defineInRange("tears_correction_bonus", 1.0, 0.0, 99999.0);

        // 攻击速度  默认1
        ATTACK_SPEED_BONUS = BUILDER
                .comment("Base value of attack speed increment")
                .defineInRange("attack_speed_bonus", 1.0, 0.0, 99999.0);

        // 方块破坏速度（最终倍率）  默认5
        BLOCK_BREAKING_SPEED_BONUS = BUILDER
                .comment("Base value of block breaking speed increment")
                .defineInRange("block_breaking_speed_bonus", 5, 0.0, 99999.0);

        // 额外子弹大小（倍率）  默认0.1f
        BULLET_SCALE_BONUS = BUILDER
                .comment("Base value of bullet scale increment")
                .defineInRange("bullet_scale_bonus", 0.1, 0.0, 99999.0);

        // 攻击击退  默认0.5
        ATTACK_KNOCKBACK_BONUS = BUILDER
                .comment("Base value of attack knockback increment")
                .defineInRange("attack_knockback_bonus", 0.5, 0.0, 99999.0);

        // 伤害倍率提升  默认1
        DAMAGE_MULTIPLIER_BASE = BUILDER
                .comment("Base value of damage multiplier base increment")
                .defineInRange("damage_multiplier_base_bonus", 1.0, 0.0, 99999.0);


        BUILDER.pop();
    }
    static {
        BUILDER.push("Item relevant");
        // 可携带的道具总数  默认999
        PASSIVE_ITEM_LIMIT = BUILDER
                .comment("How many passive items a player can carry. (NOT CURIOS)")
                .defineInRange("passive_item_limit", 999, 1, 99999);

        USABLE_PASSIVE_ITEM = BUILDER
                .comment("If the item can be added to player's backpack with a rightClick. " +
                        "If you want to enable passive item's curios slot ONLY, then you should disable this option.")
                .define("usable_passive_item", true);

        ALLOW_CURIO_UNEQUIP = BUILDER
                .comment("If the item can be unequipped")
                .define("allow_curio_unequip", true);

        AUTO_ADAPT_CURIO_SLOT = BUILDER
                .comment("Equip curio passive item will also add 1 passive item slot. (NEED to enable at least one slot)")
                .define("auto_adapt_curio_slot", false);

        // 周围（定义周围的范围）  默认12
        NEARBY_RANGE = BUILDER
                .comment("Defines the range of NEARBY." +
                        "Affects most items with a description of NEARBY")
                .defineInRange("nearby_range", 12.0, 0.0, 99999.0);

        // 神圣护盾强度  默认3
        HOLY_SHIELD_STRENGTH = BUILDER
                .comment("Amount * (Amplifier + 1);" +
                        "Damages that holy shield effect can immune.")
                .defineInRange("holy_shield_strength", 3.0, 0.0, 99999.0);

        // 钱力强度  默认0.008
        MONEY_IS_POWER_STRENGTH = BUILDER
                .comment("Damage increment of money is power. (each coin)")
                .defineInRange("money_is_power_strength", 0.007, 0.0, 99999.0);


        BUILDER.pop();
    }
    static {
        BUILDER.push("Coins");

        // 钱币物品 ID
        COIN_TIER_1_ID = BUILDER
                .comment("Item ID for Tier 1 Coin (e.g., minecraft:iron_nugget)")
                .define("coin_tier_1_id", "isaac_disaster:penny");

        COIN_TIER_2_ID = BUILDER
                .comment("Item ID for Tier 2 Coin (e.g., minecraft:gold_nugget)")
                .define("coin_tier_2_id", "isaac_disaster:nickel");

        COIN_TIER_3_ID = BUILDER
                .comment("Item ID for Tier 3 Coin (e.g., minecraft:diamond)")
                .define("coin_tier_3_id", "isaac_disaster:dime");

        // 权重定义（用于战利品表动态概率）
        COIN_TIER_1_WEIGHT = BUILDER
                .comment("Weight for Tier 1 Coin")
                .defineInRange("coin_tier_1_weight", 93, 0, 99999);

        COIN_TIER_2_WEIGHT = BUILDER
                .comment("Weight for Tier 2 Coin")
                .defineInRange("coin_tier_2_weight", 6, 0, 99999);

        COIN_TIER_3_WEIGHT = BUILDER
                .comment("Weight for Tier 3 Coin")
                .defineInRange("coin_tier_3_weight", 1, 0, 99999);

        // 价值定义
        COIN_TIER_1_VALUE = BUILDER
                .comment("Value for Tier 1 Coin")
                .defineInRange("coin_tier_1_value", 1, 0, 99999);

        COIN_TIER_2_VALUE = BUILDER
                .comment("Value for Tier 2 Coin")
                .defineInRange("coin_tier_2_value", 5, 0, 99999);

        COIN_TIER_3_VALUE = BUILDER
                .comment("Value for Tier 3 Coin")
                .defineInRange("coin_tier_3_value", 10, 0, 99999);

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

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());
    }
}

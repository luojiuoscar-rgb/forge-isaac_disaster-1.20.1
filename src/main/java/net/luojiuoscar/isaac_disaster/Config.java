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
    public static ForgeConfigSpec.IntValue HEALTH_BONUS;
    public static ForgeConfigSpec.DoubleValue MOVEMENT_SPEED_BONUS;
    public static ForgeConfigSpec.DoubleValue MOVEMENT_SPEED_LIMIT;
    public static ForgeConfigSpec.DoubleValue DAMAGE_BONUS;
    public static ForgeConfigSpec.DoubleValue LUCK_BONUS;
    // 其他可配置项目
    public static ForgeConfigSpec.IntValue PASSIVE_ITEM_LIMIT;

    static {
        // 配置数值的默认值和范围
        BUILDER.push("Passive Item Stats"); // 配置分组
        // 生命值增量基准  默认10
        HEALTH_BONUS = BUILDER
                .comment("Base value of health increment")
                .defineInRange("health_bonus", 10, 1, 99999);
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

        BUILDER.pop();
    }
    static {
        BUILDER.push("Other");
        // 可携带的道具总数  默认999
        PASSIVE_ITEM_LIMIT = BUILDER
                .comment("How many passive items can a player carry")
                .defineInRange("passive_item_limit", 999, 1, 99999);

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

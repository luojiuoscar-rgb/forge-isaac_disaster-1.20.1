package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item.item.custom.ExperimentalTreatmentItem;
import net.luojiuoscar.isaac_disaster.item.item.custom.FoodPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbility;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModPassiveItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

    public static final RegistryObject<Item> BREAKFAST = ITEMS.register("breakfast",
            () -> new FoodPassiveItem(new Item.Properties(), ModPassiveAbility.BREAKFAST));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(BREAKFAST);}

    public static final RegistryObject<Item> DESSERT = ITEMS.register("dessert",
            () -> new FoodPassiveItem(new Item.Properties(), ModPassiveAbility.DESSERT));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(DESSERT);}

    public static final RegistryObject<Item> DINNER = ITEMS.register("dinner",
            () -> new FoodPassiveItem(new Item.Properties(), ModPassiveAbility.DINNER));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(DINNER);}

    public static final RegistryObject<Item> LUNCH = ITEMS.register("lunch",
            () -> new FoodPassiveItem(new Item.Properties(), ModPassiveAbility.LUNCH));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(LUNCH);}

    public static final RegistryObject<Item> SUPPER = ITEMS.register("supper",
            () -> new FoodPassiveItem(new Item.Properties(), ModPassiveAbility.SUPPER));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SUPPER);}

    public static final RegistryObject<Item> MIDNIGHT_SNACK = ITEMS.register("midnight_snack",
            () -> new FoodPassiveItem(new Item.Properties(), ModPassiveAbility.MIDNIGHT_SNACK));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MIDNIGHT_SNACK);}

    public static final RegistryObject<Item> ROTTEN_MEAT = ITEMS.register("rotten_meat",
            () -> new FoodPassiveItem(new Item.Properties(), ModPassiveAbility.ROTTEN_MEAT));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(ROTTEN_MEAT);}

    public static final RegistryObject<Item> A_SNACK = ITEMS.register("a_snack",
            () -> new FoodPassiveItem(new Item.Properties(), ModPassiveAbility.A_SNACK));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(A_SNACK);}

    public static final RegistryObject<Item> WOODEN_SPOON = ITEMS.register("wooden_spoon",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.WOODEN_SPOON));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(WOODEN_SPOON);}

    public static final RegistryObject<Item> STEVEN = ITEMS.register("steven",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.STEVEN));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(STEVEN);}

    public static final RegistryObject<Item> CRICKETS_HEAD = ITEMS.register("crickets_head",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.CRICKETS_HEAD));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(CRICKETS_HEAD);}

    public static final RegistryObject<Item> THE_COMMON_COLD = ITEMS.register("the_common_cold",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_COMMON_COLD));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_COMMON_COLD);}

    public static final RegistryObject<Item> GLASS_EYE = ITEMS.register("glass_eye",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.GLASS_EYE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(GLASS_EYE);}

    public static final RegistryObject<Item> CAR_BATTERY = ITEMS.register("car_battery",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.CAR_BATTERY));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(CAR_BATTERY);}

    public static final RegistryObject<Item> THE_BATTERY = ITEMS.register("the_battery",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_BATTERY));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_BATTERY);}

    public static final RegistryObject<Item> VOLT_9 = ITEMS.register("volt_9",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.VOLT_9));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(VOLT_9);}

    public static final RegistryObject<Item> VOLT_4P5 = ITEMS.register("volt_4p5",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.VOLT_4P5));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(VOLT_4P5);}

    public static final RegistryObject<Item> BOOM = ITEMS.register("boom",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.BOOM));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(BOOM);}

    public static final RegistryObject<Item> MR_MEGA = ITEMS.register("mr_mega",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MR_MEGA));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MR_MEGA);}

    public static final RegistryObject<Item> BOMBER_BOY = ITEMS.register("bomber_boy",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.BOMBER_BOY));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(BOMBER_BOY);}

    public static final RegistryObject<Item> SCATTER_BOMB = ITEMS.register("scatter_bomb",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SCATTER_BOMB));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SCATTER_BOMB);}

    public static final RegistryObject<Item> FAST_BOMB = ITEMS.register("fast_bomb",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.FAST_BOMB));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(FAST_BOMB);}

    public static final RegistryObject<Item> BOBBY_BOMB = ITEMS.register("bobby_bomb",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.BOBBY_BOMB));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(BOBBY_BOMB);}

    public static final RegistryObject<Item> HOT_BOMB = ITEMS.register("hot_bomb",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.HOT_BOMB));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(HOT_BOMB);}

    public static final RegistryObject<Item> TRANSCENDENCE = ITEMS.register("transcendence",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.TRANSCENDENCE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(TRANSCENDENCE);}

    public static final RegistryObject<Item> BLOOD_OF_THE_MARTYR = ITEMS.register("blood_of_the_martyr",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.BLOOD_OF_THE_MARTYR));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(BLOOD_OF_THE_MARTYR);}

    public static final RegistryObject<Item> HOLY_MANTLE = ITEMS.register("holy_mantle",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.HOLY_MANTLE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(HOLY_MANTLE);}

    public static final RegistryObject<Item> THE_WAFER = ITEMS.register("the_wafer",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_WAFER));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_WAFER);}

    public static final RegistryObject<Item> MONEY_IS_POWER = ITEMS.register("money_is_power",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MONEY_IS_POWER));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MONEY_IS_POWER);}

    public static final RegistryObject<Item> DEAD_DOVE = ITEMS.register("dead_dove",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.DEAD_DOVE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(DEAD_DOVE);}

    public static final RegistryObject<Item> CUPIDS_ARROW = ITEMS.register("cupids_arrow",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.CUPIDS_ARROW));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(CUPIDS_ARROW);}

    public static final RegistryObject<Item> SPOON_BENDER = ITEMS.register("spoon_bender",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SPOON_BENDER));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SPOON_BENDER);}

    public static final RegistryObject<Item> ROID_RAGE = ITEMS.register("roid_rage",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.ROID_RAGE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(ROID_RAGE);}

    public static final RegistryObject<Item> THE_SAD_ONION = ITEMS.register("the_sad_onion",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_SAD_ONION));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_SAD_ONION);}

    public static final RegistryObject<Item> WIRE_COAT_HANGER = ITEMS.register("wire_coat_hanger",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.WIRE_COAT_HANGER));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(WIRE_COAT_HANGER);}

    public static final RegistryObject<Item> SPEED_BALL = ITEMS.register("speed_ball",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SPEED_BALL));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SPEED_BALL);}

    public static final RegistryObject<Item> PISCES = ITEMS.register("pisces",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.PISCES));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(PISCES);}

    public static final RegistryObject<Item> MINI_MUSH = ITEMS.register("mini_mush",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MINI_MUSH));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MINI_MUSH);}

    public static final RegistryObject<Item> PHD = ITEMS.register("phd",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.PHD));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(PHD);}

    public static final RegistryObject<Item> FALSE_PHD = ITEMS.register("false_phd",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.FALSE_PHD));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(FALSE_PHD);}

    public static final RegistryObject<Item> A_QUARTER = ITEMS.register("a_quarter",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.A_QUARTER));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(A_QUARTER);}

    public static final RegistryObject<Item> A_DOLLAR = ITEMS.register("a_dollar",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.A_DOLLAR));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(A_DOLLAR);}

    public static final RegistryObject<Item> THE_INNER_EYE = ITEMS.register("the_inner_eye",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_INNER_EYE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_INNER_EYE);}

    public static final RegistryObject<Item> PERFECT_VISION = ITEMS.register("perfect_vision",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.PERFECT_VISION));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(PERFECT_VISION);}

    public static final RegistryObject<Item> MUTANT_SPIDER = ITEMS.register("mutant_spider",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MUTANT_SPIDER));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MUTANT_SPIDER);}

    public static final RegistryObject<Item> POLYPHEMUS = ITEMS.register("polyphemus",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.POLYPHEMUS));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(POLYPHEMUS);}

    public static final RegistryObject<Item> HEART = ITEMS.register("heart",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.HEART));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(HEART);}

    public static final RegistryObject<Item> RAW_LIVER = ITEMS.register("raw_liver",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.RAW_LIVER));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(RAW_LIVER);}

    public static final RegistryObject<Item> THE_BODY = ITEMS.register("the_body",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_BODY));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_BODY);}

    public static final RegistryObject<Item> GROWTH_HORMONES = ITEMS.register("growth_hormones",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.GROWTH_HORMONES));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(GROWTH_HORMONES);}

    public static final RegistryObject<Item> SYNTHOIL = ITEMS.register("synthoil",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SYNTHOIL));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SYNTHOIL);}

    public static final RegistryObject<Item> EXPERIMENTAL_TREATMENT = ITEMS.register("experimental_treatment",
            () -> new ExperimentalTreatmentItem(new Item.Properties(), ModPassiveAbility.EXPERIMENTAL_TREATMENT));
    static { ItemListManager.PASSIVE_ITEM_LIST.add(EXPERIMENTAL_TREATMENT); ItemId.registerItem(ItemId.EXPERIMENTAL_TREATMENT.getId(), EXPERIMENTAL_TREATMENT);}

    public static final RegistryObject<Item> TORN_PHOTO = ITEMS.register("torn_photo",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.TORN_PHOTO));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(TORN_PHOTO);}

    public static final RegistryObject<Item> CAFFEINE_PILL = ITEMS.register("caffeine_pill",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.CAFFEINE_PILL));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(CAFFEINE_PILL);}

    public static final RegistryObject<Item> SAFETY_PIN = ITEMS.register("safety_pin",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SAFETY_PIN));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SAFETY_PIN);}

    public static final RegistryObject<Item> MAGIC_MUSHROOM = ITEMS.register("magic_mushroom",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MAGIC_MUSHROOM));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MAGIC_MUSHROOM);}

    public static final RegistryObject<Item> BLUE_CAP = ITEMS.register("blue_cap",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.BLUE_CAP));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(BLUE_CAP);}

    public static final RegistryObject<Item> HABIT = ITEMS.register("habit",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.HABIT));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(HABIT);}

    public static final RegistryObject<Item> RUBBER_CEMENT = ITEMS.register("rubber_cement",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.RUBBER_CEMENT));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(RUBBER_CEMENT);}

    public static final RegistryObject<Item> HOST_HAT = ITEMS.register("host_hat",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.HOST_HAT));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(HOST_HAT);}

    public static final RegistryObject<Item> PYROMANIAC = ITEMS.register("pyromaniac",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.PYROMANIAC));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(PYROMANIAC);}

    public static final RegistryObject<Item> PYRO = ITEMS.register("pyro",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.PYRO));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(PYRO);}

    public static final RegistryObject<Item> PIGGY_BANK = ITEMS.register("piggy_bank",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.PIGGY_BANK));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(PIGGY_BANK);}

    public static final RegistryObject<Item> TINY_PLANET = ITEMS.register("tiny_planet",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.TINY_PLANET));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(TINY_PLANET);}

    public static final RegistryObject<Item> MAGIC_SCAB = ITEMS.register("magic_scab",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MAGIC_SCAB));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MAGIC_SCAB);}

    public static final RegistryObject<Item> SCREW = ITEMS.register("screw",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SCREW));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SCREW);}

    public static final RegistryObject<Item> BLACK_CANDLE = ITEMS.register("black_candle",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.BLACK_CANDLE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(BLACK_CANDLE);}

    public static final RegistryObject<Item> TAROT_CLOTH = ITEMS.register("tarot_cloth",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.TAROT_CLOTH));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(TAROT_CLOTH);}

    public static final RegistryObject<Item> WHORE_OF_BABYLON = ITEMS.register("whore_of_babylon",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.WHORE_OF_BABYLON));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(WHORE_OF_BABYLON);}

    public static final RegistryObject<Item> CURSE_OF_THE_TOWER = ITEMS.register("curse_of_the_tower",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.CURSE_OF_THE_TOWER));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(CURSE_OF_THE_TOWER);}

    public static final RegistryObject<Item> THE_SOUL = ITEMS.register("the_soul",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_SOUL));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_SOUL);}

    public static final RegistryObject<Item> SACRED_ORB = ITEMS.register("sacred_orb",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SACRED_ORB));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SACRED_ORB);}

    public static final RegistryObject<Item> SACK_HEAD = ITEMS.register("sack_head",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SACK_HEAD));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SACK_HEAD);}

    public static final RegistryObject<Item> MITRE = ITEMS.register("mitre",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MITRE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MITRE);}

    public static final RegistryObject<Item> GLITCHED_CROWN = ITEMS.register("glitched_crown",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.GLITCHED_CROWN));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(GLITCHED_CROWN);}

    public static final RegistryObject<Item> BINGE_EATER = ITEMS.register("binge_eater",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.BINGE_EATER));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(BINGE_EATER);}

    public static final RegistryObject<Item> ECHO_CHAMBER = ITEMS.register("echo_chamber",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.ECHO_CHAMBER));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(ECHO_CHAMBER);}

    public static final RegistryObject<Item> CHAOS = ITEMS.register("chaos",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.CHAOS));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(CHAOS);}

    public static final RegistryObject<Item> TOOTH_PICKS = ITEMS.register("tooth_picks",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.TOOTH_PICKS));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(TOOTH_PICKS);}

    public static final RegistryObject<Item> TECHNOLOGY = ITEMS.register("technology",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.TECHNOLOGY));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(TECHNOLOGY);}

    public static final RegistryObject<Item> MARKED = ITEMS.register("marked",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MARKED));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MARKED);}

    public static final RegistryObject<Item> THE_WIZ = ITEMS.register("the_wiz",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_WIZ));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_WIZ);}

    public static final RegistryObject<Item> MY_REFLECTION = ITEMS.register("my_reflection",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MY_REFLECTION));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MY_REFLECTION);}

    public static final RegistryObject<Item> IPECAC = ITEMS.register("ipecac",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.IPECAC));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(IPECAC);}

    public static final RegistryObject<Item> BRIMSTONE = ITEMS.register("brimstone",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.BRIMSTONE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(BRIMSTONE);}

    public static final RegistryObject<Item> C_SECTION = ITEMS.register("c_section",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.C_SECTION));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(C_SECTION);}

    public static final RegistryObject<Item> CURSED_EYE = ITEMS.register("cursed_eye",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.CURSED_EYE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(CURSED_EYE);}

    public static final RegistryObject<Item> NEPTUNUS = ITEMS.register("neptunus",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.NEPTUNUS));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(NEPTUNUS);}

    public static final RegistryObject<Item> TECHNOLOGY2 = ITEMS.register("technology2",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.TECHNOLOGY2));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(TECHNOLOGY2);}

    public static final RegistryObject<Item> ROCK_BOTTOM = ITEMS.register("rock_bottom",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.ROCK_BOTTOM));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(ROCK_BOTTOM);}

    public static final RegistryObject<Item> TERRA = ITEMS.register("terra",
                    () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.TERRA));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(TERRA);}

    public static final RegistryObject<Item> VENUS = ITEMS.register("venus",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.VENUS));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(VENUS);}

    public static final RegistryObject<Item> THE_VIRUS = ITEMS.register("the_virus",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_VIRUS));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_VIRUS);}

    public static final RegistryObject<Item> SKELETON_KEY = ITEMS.register("skeleton_key",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SKELETON_KEY));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SKELETON_KEY);}

    public static final RegistryObject<Item> THE_BELT = ITEMS.register("the_belt",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_BELT));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_BELT);}

    public static final RegistryObject<Item> LUCKY_FOOT = ITEMS.register("lucky_foot",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.LUCKY_FOOT));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(LUCKY_FOOT);}

    public static final RegistryObject<Item> CHARM_OF_THE_VAMPIRE = ITEMS.register("charm_of_the_vampire",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.CHARM_OF_THE_VAMPIRE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(CHARM_OF_THE_VAMPIRE);}

    public static final RegistryObject<Item> SUPER_BANDAGE = ITEMS.register("super_bandage",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SUPER_BANDAGE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SUPER_BANDAGE);}

    public static final RegistryObject<Item> THE_SMALL_ROCK = ITEMS.register("the_small_rock",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_SMALL_ROCK));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_SMALL_ROCK);}

    public static final RegistryObject<Item> SACK_OF_PENNIES = ITEMS.register("sack_of_pennies",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SACK_OF_PENNIES));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SACK_OF_PENNIES);}

    public static final RegistryObject<Item> THE_RELIC = ITEMS.register("the_relic",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_RELIC));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_RELIC);}

    public static final RegistryObject<Item> BOMB_BAG = ITEMS.register("bomb_bag",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.BOMB_BAG));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(BOMB_BAG);}

    public static final RegistryObject<Item> THE_HALO = ITEMS.register("the_halo",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_HALO));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_HALO);}

    public static final RegistryObject<Item> OUIJA_BOARD = ITEMS.register("ouija_board",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.OUIJA_BOARD));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(OUIJA_BOARD);}

    public static final RegistryObject<Item> THIN_ODD_MUSHROOM = ITEMS.register("thin_odd_mushroom",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THIN_ODD_MUSHROOM));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THIN_ODD_MUSHROOM);}

    public static final RegistryObject<Item> LARGE_ODD_MUSHROOM = ITEMS.register("large_odd_mushroom",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.LARGE_ODD_MUSHROOM));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(LARGE_ODD_MUSHROOM);}

    public static final RegistryObject<Item> PENTAGRAM = ITEMS.register("pentagram",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.PENTAGRAM));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(PENTAGRAM);}

    public static final RegistryObject<Item> MAGNETO = ITEMS.register("magneto",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MAGNETO));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MAGNETO);}

    public static final RegistryObject<Item> THE_MARK = ITEMS.register("the_mark",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_MARK));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_MARK);}

    public static final RegistryObject<Item> THE_PACT = ITEMS.register("the_pact",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.THE_PACT));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(THE_PACT);}

    public static final RegistryObject<Item> LORD_OF_THE_PIT = ITEMS.register("lord_of_the_pit",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.LORD_OF_THE_PIT));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(LORD_OF_THE_PIT);}

    public static final RegistryObject<Item> BUCKET_OF_LARD = ITEMS.register("bucket_of_lard",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.BUCKET_OF_LARD));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(BUCKET_OF_LARD);}

    public static final RegistryObject<Item> STIGMATA = ITEMS.register("stigmata",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.STIGMATA));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(STIGMATA);}

    public static final RegistryObject<Item> PAGEANT_BOY = ITEMS.register("pageant_boy",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.PAGEANT_BOY));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(PAGEANT_BOY);}

    public static final RegistryObject<Item> SPIRIT_OF_THE_NIGHT = ITEMS.register("spirit_of_the_night",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SPIRIT_OF_THE_NIGHT));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SPIRIT_OF_THE_NIGHT);}

    public static final RegistryObject<Item> STEAM_SALE = ITEMS.register("steam_sale",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.STEAM_SALE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(STEAM_SALE);}

    public static final RegistryObject<Item> IRON_BAR = ITEMS.register("iron_bar",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.IRON_BAR));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(IRON_BAR);}

    public static final RegistryObject<Item> MIDAS_TOUCH = ITEMS.register("midas_touch",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MIDAS_TOUCH));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MIDAS_TOUCH);}

    public static final RegistryObject<Item> BOGO_BOMBS = ITEMS.register("bogo_bombs",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.BOGO_BOMBS));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(BOGO_BOMBS);}

    public static final RegistryObject<Item> LOKIS_HORNS = ITEMS.register("lokis_horns",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.LOKIS_HORNS));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(LOKIS_HORNS);}

    public static final RegistryObject<Item> SACRED_HEART = ITEMS.register("sacred_heart",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SACRED_HEART));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SACRED_HEART);}

    public static final RegistryObject<Item> CAT_O_NINE_TAILS = ITEMS.register("cat_o_nine_tails",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.CAT_O_NINE_TAILS));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(CAT_O_NINE_TAILS);}

    public static final RegistryObject<Item> STEM_CELLS = ITEMS.register("stem_cells",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.STEM_CELLS));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(STEM_CELLS);}

    public static final RegistryObject<Item> FATE = ITEMS.register("fate",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.FATE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(FATE);}

    public static final RegistryObject<Item> HOLY_GRAIL = ITEMS.register("holy_grail",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.HOLY_GRAIL));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(HOLY_GRAIL);}

    public static final RegistryObject<Item> SMB_SUPER_FAN = ITEMS.register("smb_super_fan",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SMB_SUPER_FAN));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SMB_SUPER_FAN);}

    public static final RegistryObject<Item> MEAT = ITEMS.register("meat",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MEAT));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MEAT);}

    public static final RegistryObject<Item> MAGIC_8_BALL = ITEMS.register("magic_8_ball",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MAGIC_8_BALL));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MAGIC_8_BALL);}

    public static final RegistryObject<Item> MOMS_COIN_PURSE = ITEMS.register("moms_coin_purse",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MOMS_COIN_PURSE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MOMS_COIN_PURSE);}

    public static final RegistryObject<Item> SQUEEZY = ITEMS.register("squeezy",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.SQUEEZY));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(SQUEEZY);}

    public static final RegistryObject<Item> JESUS_JUICE = ITEMS.register("jesus_juice",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.JESUS_JUICE));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(JESUS_JUICE);}

    public static final RegistryObject<Item> MOMS_KEY = ITEMS.register("moms_key",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MOMS_KEY));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MOMS_KEY);}

    public static final RegistryObject<Item> MOMS_EYESHADOW = ITEMS.register("moms_eyeshadow",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.MOMS_EYESHADOW));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(MOMS_EYESHADOW);}

    public static final RegistryObject<Item> FANNY_PACK = ITEMS.register("fanny_pack",
            () -> new PassiveItem(new Item.Properties(), ModPassiveAbility.FANNY_PACK));
    static {ItemListManager.PASSIVE_ITEM_LIST.add(FANNY_PACK);}


}
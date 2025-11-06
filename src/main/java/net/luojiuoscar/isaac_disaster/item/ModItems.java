package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlocks;
import net.luojiuoscar.isaac_disaster.item.block.IsaacChestBlockItem;
import net.luojiuoscar.isaac_disaster.item.custom.DebugStick;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.item.item.custom.ExperimentalTreatmentItem;
import net.luojiuoscar.isaac_disaster.item.item.custom.FoodPassiveItem;
import net.luojiuoscar.isaac_disaster.item.pickup.*;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

import static net.luojiuoscar.isaac_disaster.manager.ItemListManager.*;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

    // Passive items
    private static RegistryObject<Item> registerPassiveItem(
            String name, int itemLevel, ItemId id){
        return registerPassiveItem(name, itemLevel, id, false, false);
    }
    private static RegistryObject<Item> registerPassiveItem(
            String name, int itemLevel, ItemId id,
            boolean hasSpecialEffect){
        return registerPassiveItem(name, itemLevel, id, hasSpecialEffect, false);
    }
    private static RegistryObject<Item> registerPassiveItem(
            String name, int itemLevel, ItemId id,
            boolean hasSpecialEffect, boolean useOriginalColor) {
        RegistryObject<Item> reg =
                ITEMS.register(name, () -> new PassiveItem(
                        new Item.Properties(),
                        itemLevel,
                        id.getId(),
                        hasSpecialEffect,
                        useOriginalColor
        ));
        PASSIVE_ITEM_LIST.add(reg);
        ItemId.registerItem(id.getId(), reg);
        return reg;
    }
    private static RegistryObject<Item> registerFoodPassiveItem(
            String name, int itemLevel, ItemId id) {
        RegistryObject<Item> reg =
                ITEMS.register(name, () -> new FoodPassiveItem(
                        new Item.Properties(),
                        itemLevel,
                        id.getId(),
                        false,
                        false
                ));
        PASSIVE_ITEM_LIST.add(reg);
        ItemId.registerItem(id.getId(), reg);
        return reg;
    }


    public static final RegistryObject<Item> BREAKFAST          = registerFoodPassiveItem("breakfast", 1, ItemId.BREAKFAST);
    public static final RegistryObject<Item> DESSERT            = registerFoodPassiveItem("dessert", 1, ItemId.DESSERT);
    public static final RegistryObject<Item> DINNER             = registerFoodPassiveItem("dinner", 1, ItemId.DINNER);
    public static final RegistryObject<Item> LUNCH              = registerFoodPassiveItem("lunch", 1, ItemId.LUNCH);
    public static final RegistryObject<Item> SUPPER             = registerFoodPassiveItem("supper", 1, ItemId.SUPPER);
    public static final RegistryObject<Item> MIDNIGHT_SNACK     = registerFoodPassiveItem("midnight_snack", 1, ItemId.MIDNIGHT_SNACK);
    public static final RegistryObject<Item> ROTTEN_MEAT        = registerFoodPassiveItem("rotten_meat", 1, ItemId.ROTTEN_MEAT);
    public static final RegistryObject<Item> A_SNACK            = registerFoodPassiveItem("a_snack", 1, ItemId.A_SNACK);
    public static final RegistryObject<Item> WOODEN_SPOON       = registerPassiveItem("wooden_spoon", 1, ItemId.WOODEN_SPOON);
    public static final RegistryObject<Item> STEVEN             = registerPassiveItem("steven", 3, ItemId.STEVEN);
    public static final RegistryObject<Item> CRICKETS_HEAD      = registerPassiveItem("crickets_head", 4, ItemId.CRICKETS_HEAD);
    public static final RegistryObject<Item> THE_COMMON_COLD    = registerPassiveItem("the_common_cold", 1, ItemId.THE_COMMON_COLD, true);
    public static final RegistryObject<Item> GLASS_EYE          = registerPassiveItem("glass_eye", 3, ItemId.GLASS_EYE);
    public static final RegistryObject<Item> CAR_BATTERY        = registerPassiveItem("car_battery", 3, ItemId.CAR_BATTERY);
    public static final RegistryObject<Item> THE_BATTERY        = registerPassiveItem("the_battery", 3, ItemId.THE_BATTERY);
    public static final RegistryObject<Item> VOLT_9             = registerPassiveItem("volt_9", 2, ItemId.VOLT_9);
    public static final RegistryObject<Item> VOLT_4P5           = registerPassiveItem("volt_4p5", 2, ItemId.VOLT_4P5);
    public static final RegistryObject<Item> BOOM               = registerPassiveItem("boom", 0, ItemId.BOOM);
    public static final RegistryObject<Item> MR_MEGA            = registerPassiveItem("mr_mega", 3, ItemId.MR_MEGA);
    public static final RegistryObject<Item> BOMBER_BOY         = registerPassiveItem("bomber_boy", 3, ItemId.BOMBER_BOY);
    public static final RegistryObject<Item> SCATTER_BOMB       = registerPassiveItem("scatter_bomb", 2, ItemId.SCATTER_BOMB);
    public static final RegistryObject<Item> FAST_BOMB          = registerPassiveItem("fast_bomb", 2, ItemId.FAST_BOMB);
    public static final RegistryObject<Item> BOBBY_BOMB         = registerPassiveItem("bobby_bomb", 2, ItemId.BOBBY_BOMB);
    public static final RegistryObject<Item> HOT_BOMB           = registerPassiveItem("hot_bomb", 1, ItemId.HOT_BOMB);
    public static final RegistryObject<Item> TRANSCENDENCE      = registerPassiveItem("transcendence", 3, ItemId.TRANSCENDENCE, true);
    public static final RegistryObject<Item> BLOOD_OF_THE_MARTYR = registerPassiveItem("blood_of_the_martyr", 3, ItemId.BLOOD_OF_THE_MARTYR);
    public static final RegistryObject<Item> HOLY_MANTLE        = registerPassiveItem("holy_mantle", 4, ItemId.HOLY_MANTLE, true);
    public static final RegistryObject<Item> THE_WAFER          = registerPassiveItem("the_wafer", 4, ItemId.THE_WAFER);
    public static final RegistryObject<Item> MONEY_IS_POWER     = registerPassiveItem("money_is_power", 3, ItemId.MONEY_IS_POWER);
    public static final RegistryObject<Item> DEAD_DOVE          = registerPassiveItem("dead_dove", 3, ItemId.DEAD_DOVE);
    public static final RegistryObject<Item> CUPIDS_ARROW       = registerPassiveItem("cupids_arrow", 2, ItemId.CUPIDS_ARROW);
    public static final RegistryObject<Item> SPOON_BENDER       = registerPassiveItem("spoon_bender", 3, ItemId.SPOON_BENDER);
    public static final RegistryObject<Item> ROID_RAGE          = registerPassiveItem("roid_rage", 2, ItemId.ROID_RAGE, true);
    public static final RegistryObject<Item> THE_SAD_ONION      = registerPassiveItem("the_sad_onion", 3, ItemId.THE_SAD_ONION);
    public static final RegistryObject<Item> WIRE_COAT_HANGER   = registerPassiveItem("wire_coat_hanger", 3, ItemId.WIRE_COAT_HANGER);
    public static final RegistryObject<Item> SPEED_BALL         = registerPassiveItem("speed_ball", 2, ItemId.SPEED_BALL, true);
    public static final RegistryObject<Item> PISCES             = registerPassiveItem("pisces", 2, ItemId.PISCES);
    public static final RegistryObject<Item> MINI_MUSH          = registerPassiveItem("mini_mush", 2, ItemId.MINI_MUSH, true);
    public static final RegistryObject<Item> PHD                = registerPassiveItem("phd", 2, ItemId.PHD);
    public static final RegistryObject<Item> FALSE_PHD          = registerPassiveItem("false_phd", 2, ItemId.FALSE_PHD);
    public static final RegistryObject<Item> A_QUARTER          = registerPassiveItem("a_quarter", 0, ItemId.A_QUARTER);
    public static final RegistryObject<Item> A_DOLLAR           = registerPassiveItem("a_dollar", 1, ItemId.A_DOLLAR);
    public static final RegistryObject<Item> THE_INNER_EYE      = registerPassiveItem("the_inner_eye", 2, ItemId.THE_INNER_EYE);
    public static final RegistryObject<Item> PERFECT_VISION     = registerPassiveItem("perfect_vision", 4, ItemId.PERFECT_VISION);
    public static final RegistryObject<Item> MUTANT_SPIDER      = registerPassiveItem("mutant_spider", 3, ItemId.MUTANT_SPIDER);
    public static final RegistryObject<Item> POLYPHEMUS         = registerPassiveItem("polyphemus", 4, ItemId.POLYPHEMUS);
    public static final RegistryObject<Item> HEART              = registerPassiveItem("heart", 1, ItemId.HEART);
    public static final RegistryObject<Item> RAW_LIVER          = registerPassiveItem("raw_liver", 2, ItemId.RAW_LIVER);
    public static final RegistryObject<Item> THE_BODY           = registerPassiveItem("the_body", 3, ItemId.THE_BODY);
    public static final RegistryObject<Item> GROWTH_HORMONES    = registerPassiveItem("growth_hormones", 3, ItemId.GROWTH_HORMONES, true);
    public static final RegistryObject<Item> SYNTHOIL           = registerPassiveItem("synthoil", 3, ItemId.SYNTHOIL, true);
    public static final RegistryObject<Item> TORN_PHOTO         = registerPassiveItem("torn_photo", 3, ItemId.TORN_PHOTO);
    public static final RegistryObject<Item> CAFFEINE_PILL      = registerPassiveItem("caffeine_pill", 1, ItemId.CAFFEINE_PILL);
    public static final RegistryObject<Item> SAFETY_PIN         = registerPassiveItem("safety_pin", 1, ItemId.SAFETY_PIN);
    public static final RegistryObject<Item> MAGIC_MUSHROOM     = registerPassiveItem("magic_mushroom", 4, ItemId.MAGIC_MUSHROOM, true);
    public static final RegistryObject<Item> BLUE_CAP           = registerPassiveItem("blue_cap", 3, ItemId.BLUE_CAP, true);
    public static final RegistryObject<Item> HABIT              = registerPassiveItem("habit", 3, ItemId.HABIT);
    public static final RegistryObject<Item> RUBBER_CEMENT      = registerPassiveItem("rubber_cement", 3, ItemId.RUBBER_CEMENT);
    public static final RegistryObject<Item> HOST_HAT           = registerPassiveItem("host_hat", 3, ItemId.HOST_HAT);
    public static final RegistryObject<Item> PYROMANIAC         = registerPassiveItem("pyromaniac", 4, ItemId.PYROMANIAC);
    public static final RegistryObject<Item> PYRO               = registerPassiveItem("pyro", 3, ItemId.PYRO);
    public static final RegistryObject<Item> PIGGY_BANK         = registerPassiveItem("piggy_bank", 2, ItemId.PIGGY_BANK);
    public static final RegistryObject<Item> TINY_PLANET        = registerPassiveItem("tiny_planet", 0, ItemId.TINY_PLANET);
    public static final RegistryObject<Item> MAGIC_SCAB         = registerPassiveItem("magic_scab", 1, ItemId.MAGIC_SCAB);
    public static final RegistryObject<Item> SCREW              = registerPassiveItem("screw", 2, ItemId.SCREW);
    public static final RegistryObject<Item> BLACK_CANDLE       = registerPassiveItem("black_candle", 3, ItemId.BLACK_CANDLE);
    public static final RegistryObject<Item> TAROT_CLOTH        = registerPassiveItem("tarot_cloth", 2, ItemId.TAROT_CLOTH);
    public static final RegistryObject<Item> WHORE_OF_BABYLON   = registerPassiveItem("whore_of_babylon", 2, ItemId.WHORE_OF_BABYLON, true);
    public static final RegistryObject<Item> CURSE_OF_THE_TOWER = registerPassiveItem("curse_of_the_tower", 0, ItemId.CURSE_OF_THE_TOWER);
    public static final RegistryObject<Item> THE_SOUL           = registerPassiveItem("the_soul", 2, ItemId.THE_SOUL, true);
    public static final RegistryObject<Item> SACRED_ORB         = registerPassiveItem("sacred_orb", 4, ItemId.SACRED_ORB);
    public static final RegistryObject<Item> EXPERIMENTAL_TREATMENT = ITEMS.register("experimental_treatment",
            () -> new ExperimentalTreatmentItem(new Item.Properties(),2, ItemId.EXPERIMENTAL_TREATMENT.getId()));
    static { PASSIVE_ITEM_LIST.add(EXPERIMENTAL_TREATMENT); ItemId.registerItem(ItemId.EXPERIMENTAL_TREATMENT.getId(), EXPERIMENTAL_TREATMENT);}
    public static final RegistryObject<Item> SACK_HEAD          = registerPassiveItem("sack_head", 3, ItemId.SACK_HEAD);
    public static final RegistryObject<Item> MITRE              = registerPassiveItem("mitre", 2, ItemId.MITRE);
    public static final RegistryObject<Item> GLITCHED_CROWN     = registerPassiveItem("glitched_crown", 4, ItemId.GLITCHED_CROWN);
    public static final RegistryObject<Item> BINGE_EATER        = registerPassiveItem("binge_eater", 4, ItemId.BINGE_EATER);



    // passive end
    // Active item
    private static RegistryObject<Item> registerActiveItem(int useCharge, int totalCharge, String name, int itemLevel, ItemId id) {
        return registerActiveItem(useCharge, totalCharge, name, itemLevel, id, false,false);
    }
    private static RegistryObject<Item> registerActiveItem(int useCharge, int totalCharge, String name, int itemLevel, ItemId id, boolean hasSpecialEffect) {
        return registerActiveItem(useCharge, totalCharge, name, itemLevel, id, hasSpecialEffect,false);
    }
    private static RegistryObject<Item> registerActiveItem(int useCharge, int totalCharge, String name, int itemLevel, ItemId id,
                                                           boolean hasSpecialEffect, boolean useOriginalColor) {
        RegistryObject<Item> reg = ITEMS.register(name, () -> new ActiveItem(
                new Item.Properties(),
                itemLevel,
                id.getId(),
                useCharge,
                totalCharge,
                hasSpecialEffect,
                useOriginalColor
        ));
        ACTIVE_ITEM_LIST.add(reg);
        ItemId.registerItem(id.getId(), reg);
        return reg;
    }
    public static final RegistryObject<Item> YUM_HEART             = registerActiveItem(8, 8, "yum_heart", 1, ItemId.YUM_HEART);
    public static final RegistryObject<Item> THE_BOOK_OF_BELIAL    = registerActiveItem(8, 8, "the_book_of_belial", 2, ItemId.THE_BOOK_OF_BELIAL, true);
    public static final RegistryObject<Item> BOOK_OF_SHADOW        = registerActiveItem(12, 12, "book_of_shadow", 3, ItemId.BOOK_OF_SHADOW, true);
    public static final RegistryObject<Item> THE_BIBLE             = registerActiveItem(12, 12, "the_bible", 2, ItemId.THE_BIBLE, true);
    public static final RegistryObject<Item> THE_NECRONMICON       = registerActiveItem(6, 6, "the_necronmicon", 2, ItemId.THE_NECRONMICON, true);
    public static final RegistryObject<Item> WOODEN_NICKEL         = registerActiveItem(3, 3, "wooden_nickel", 1, ItemId.WOODEN_NICKEL);
    public static final RegistryObject<Item> TELEPORT              = registerActiveItem(4, 4, "teleport", 0, ItemId.TELEPORT);
    public static final RegistryObject<Item> LEMON_MISHAP          = registerActiveItem(4, 4, "lemon_mishap", 1, ItemId.LEMON_MISHAP);
    public static final RegistryObject<Item> FREE_LEMONADE         = registerActiveItem(6, 6, "free_lemonade", 2, ItemId.FREE_LEMONADE);
    public static final RegistryObject<Item> THE_GAMEKID           = registerActiveItem(12, 12, "the_gamekid", 1, ItemId.THE_GAMEKID, true);
    public static final RegistryObject<Item> UNICORN_STUMP         = registerActiveItem(12, 12, "unicorn_stump", 1, ItemId.UNICORN_STUMP, true);
    public static final RegistryObject<Item> MY_LITTLE_UNICORN     = registerActiveItem(12, 12, "my_little_unicorn", 1, ItemId.MY_LITTLE_UNICORN, true);
    public static final RegistryObject<Item> PLACEBO               = registerActiveItem(24, 24, "placebo", 1, ItemId.PLACEBO);
    public static final RegistryObject<Item> DIPLOPIA              = registerActiveItem(0, 0, "diplopia", 4, ItemId.DIPLOPIA);
    public static final RegistryObject<Item> CROOKED_PENNY         = registerActiveItem(18, 18, "crooked_penny", 3, ItemId.CROOKED_PENNY);
    public static final RegistryObject<Item> DULL_RAZOR            = registerActiveItem(2, 2, "dull_razor", 2, ItemId.DULL_RAZOR);
    public static final RegistryObject<Item> TELEPATHY_FOR_DUMMIES = registerActiveItem(8, 8, "telepathy_for_dummies", 1, ItemId.TELEPATHY_FOR_DUMMIES, true);
    public static final RegistryObject<Item> ANARCHIST_COOKBOOK    = registerActiveItem(6, 6, "anarchist_cookbook", 1, ItemId.ANARCHIST_COOKBOOK, true);
    public static final RegistryObject<Item> SMELTER               = registerActiveItem(12, 12, "smelter", 2, ItemId.SMELTER);
    public static final RegistryObject<Item> THE_D6                = registerActiveItem(12, 12, "the_d6", 4, ItemId.THE_D6);



    // active end
    // trinket

    private static RegistryObject<Item> registerTrinket(String name, TrinketId id) {
        RegistryObject<Item> reg = ITEMS.register(name, () -> new Trinket(new Item.Properties(), id.getId()));
        TRINKET_LIST.add(reg);
        TrinketId.registerItem(id.getId(), reg);
        return reg;
    }

    public static final RegistryObject<Item> SWALLOWED_PENNY = registerTrinket("swallowed_penny", TrinketId.SWALLOWED_PENNY);
    public static final RegistryObject<Item> AAA_BATTERY    = registerTrinket("aaa_battery", TrinketId.AAA_BATTERY);
    public static final RegistryObject<Item> BROKEN_REMOTE  = registerTrinket("broken_remote", TrinketId.BROKEN_REMOTE);
    public static final RegistryObject<Item> CARTRIDGE      = registerTrinket("cartridge", TrinketId.CARTRIDGE);
    public static final RegistryObject<Item> LUCKY_ROCK     = registerTrinket("lucky_rock", TrinketId.LUCKY_ROCK);
    public static final RegistryObject<Item> LUCKY_TOE      = registerTrinket("lucky_toe", TrinketId.LUCKY_TOE);
    public static final RegistryObject<Item> CANCER_TRINKET = registerTrinket("cancer_trinket", TrinketId.CANCER_TRINKET);
    public static final RegistryObject<Item> BLIND_RAGE     = registerTrinket("blind_rage", TrinketId.BLIND_RAGE);
    public static final RegistryObject<Item> PERFECTION     = registerTrinket("perfection", TrinketId.PERFECTION);
    public static final RegistryObject<Item> DAEMONS_TAIL   = registerTrinket("daemons_tail", TrinketId.DAEMONS_TAIL);
    public static final RegistryObject<Item> PAPER_CLIP     = registerTrinket("paper_clip", TrinketId.PAPER_CLIP);
    public static final RegistryObject<Item> SAFETY_CAP     = registerTrinket("safety_cap", TrinketId.SAFETY_CAP);
    public static final RegistryObject<Item> ACE_OF_SPADES_TRINKET = registerTrinket("ace_of_spades_trinket", TrinketId.ACE_OF_SPADES_TRINKET);
    public static final RegistryObject<Item> CHILDS_HEART   = registerTrinket("childs_heart", TrinketId.CHILDS_HEART);
    public static final RegistryObject<Item> MATCH_STICK    = registerTrinket("match_stick", TrinketId.MATCH_STICK);
    public static final RegistryObject<Item> RUSTED_KEY     = registerTrinket("rusted_key", TrinketId.RUSTED_KEY);
    public static final RegistryObject<Item> POKER_CHIP     = registerTrinket("poker_chip", TrinketId.POKER_CHIP);
    public static final RegistryObject<Item> GILDED_KEY     = registerTrinket("gilded_key", TrinketId.GILDED_KEY);
    public static final RegistryObject<Item> THE_LEFT_HAND  = registerTrinket("the_left_hand", TrinketId.THE_LEFT_HAND);


    // trinket end
    // pickups
    public static final RegistryObject<Item> ISAAC_HEAD = ITEMS.register("isaac_head",
            () -> new IsaacHead(new Item.Properties(), PickupId.ISAAC_HEAD.getId()));
    static { PICKUP_LIST.add(ISAAC_HEAD); }

    public static final RegistryObject<Item> PENNY = ITEMS.register("penny",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(PENNY); }

    public static final RegistryObject<Item> NICKEL = ITEMS.register("nickel",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(NICKEL); }

    public static final RegistryObject<Item> DIME = ITEMS.register("dime",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(DIME); }

    public static final RegistryObject<Item> HALF_RED_HEART = ITEMS.register("half_red_heart",
            () -> new Heart(new Item.Properties(), PickupId.HALF_RED_HEART.getId(), Rarity.COMMON));
    static { PICKUP_LIST.add(HALF_RED_HEART); }

    public static final RegistryObject<Item> RED_HEART = ITEMS.register("red_heart",
            () -> new Heart(new Item.Properties(), PickupId.RED_HEART.getId(), Rarity.COMMON));
    static { PICKUP_LIST.add(RED_HEART); }

    public static final RegistryObject<Item> DOUBLE_RED_HEART = ITEMS.register("double_red_heart",
            () -> new Heart(new Item.Properties(), PickupId.DOUBLE_RED_HEART.getId(), Rarity.COMMON));
    static { PICKUP_LIST.add(DOUBLE_RED_HEART); }

    public static final RegistryObject<Item> HALF_SOUL_HEART = ITEMS.register("half_soul_heart",
            () -> new Heart(new Item.Properties(), PickupId.HALF_SOUL_HEART.getId(), Rarity.UNCOMMON));
    static { PICKUP_LIST.add(HALF_SOUL_HEART); }

    public static final RegistryObject<Item> SOUL_HEART = ITEMS.register("soul_heart",
            () -> new Heart(new Item.Properties(), PickupId.SOUL_HEART.getId(), Rarity.RARE));
    static { PICKUP_LIST.add(SOUL_HEART); }

    public static final RegistryObject<Item> BLENDED_HEART = ITEMS.register("blended_heart",
            () -> new Heart(new Item.Properties(), PickupId.BLENDED_HEART.getId(), Rarity.RARE));
    static { PICKUP_LIST.add(BLENDED_HEART); }

    public static final RegistryObject<Item> BLACK_HEART = ITEMS.register("black_heart",
            () -> new Heart(new Item.Properties(), PickupId.BLACK_HEART.getId(), Rarity.EPIC));
    static { PICKUP_LIST.add(BLACK_HEART); }

    public static final RegistryObject<Item> BONE_HEART = ITEMS.register("bone_heart",
            () -> new Heart(new Item.Properties(), PickupId.BONE_HEART.getId(), Rarity.EPIC));
    static { PICKUP_LIST.add(BONE_HEART); }

    public static final RegistryObject<Item> ETERNAL_HEART = ITEMS.register("eternal_heart",
            () -> new Heart(new Item.Properties(), PickupId.ETERNAL_HEART.getId(), Rarity.EPIC));
    static { PICKUP_LIST.add(ETERNAL_HEART); }

    public static final RegistryObject<Item> GOLDEN_HEART = ITEMS.register("golden_heart",
            () -> new Heart(new Item.Properties(), PickupId.GOLDEN_HEART.getId(), Rarity.RARE));
    static { PICKUP_LIST.add(GOLDEN_HEART); }


    public static final RegistryObject<Item> BOMB = ITEMS.register("bomb",
            () -> new Bomb(new Item.Properties(), PickupId.BOMB.getId()));
    static { PICKUP_LIST.add(BOMB); }

    public static final RegistryObject<Item> GIGA_BOMB = ITEMS.register("giga_bomb",
            () -> new Bomb(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), PickupId.GIGA_BOMB.getId()));
    static { PICKUP_LIST.add(GIGA_BOMB); }

    public static final RegistryObject<Item> GOLDEN_BOMB = ITEMS.register("golden_bomb",
            () -> new Bomb(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), PickupId.GOLDEN_BOMB.getId()));
    static { PICKUP_LIST.add(GOLDEN_BOMB); }


    public static final RegistryObject<Item> KEY = ITEMS.register("key",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(KEY); }

    public static final RegistryObject<Item> GOLDEN_KEY = ITEMS.register("golden_key",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));
    static { PICKUP_LIST.add(GOLDEN_KEY); }

    public static final RegistryObject<Item> SMALL_BATTERY = ITEMS.register("small_battery",
            () -> new Battery(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(4), PickupId.SMALL_BATTERY.getId()));
    static { PICKUP_LIST.add(SMALL_BATTERY); }

    public static final RegistryObject<Item> BATTERY = ITEMS.register("battery",
            () -> new Battery(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(4), PickupId.BATTERY.getId()));
    static { PICKUP_LIST.add(BATTERY); }

    public static final RegistryObject<Item> MEGA_BATTERY = ITEMS.register("mega_battery",
            () -> new Battery(new Item.Properties().rarity(Rarity.RARE).stacksTo(1), PickupId.MEGA_BATTERY.getId()));
    static { PICKUP_LIST.add(MEGA_BATTERY); }

    public static final RegistryObject<Item> GOLDEN_BATTERY = ITEMS.register("golden_battery",
            () -> new Battery(new Item.Properties().rarity(Rarity.RARE).stacksTo(1), PickupId.GOLDEN_BATTERY.getId()));
    static { PICKUP_LIST.add(GOLDEN_BATTERY); }

    public static final RegistryObject<Item> GRAB_BAG = ITEMS.register("grab_bag",
            () -> new Sack(new Item.Properties(), PickupId.GRAB_BAG.getId()));
    static { PICKUP_LIST.add(GRAB_BAG); }

    public static final RegistryObject<Item> BLACK_SACK = ITEMS.register("black_sack",
            () -> new Sack(new Item.Properties(), PickupId.BLACK_SACK.getId()));
    static { PICKUP_LIST.add(BLACK_SACK); }


    public static final RegistryObject<Item> THE_FOOL = ITEMS.register("the_fool",
            () -> new Card(new Item.Properties(), PickupId.THE_FOOL.getId()));
    static { PICKUP_LIST.add(THE_FOOL); }

    public static final RegistryObject<Item> THE_MAGICIAN = ITEMS.register("the_magician",
            () -> new Card(new Item.Properties(), PickupId.THE_MAGICIAN.getId()));
    static { PICKUP_LIST.add(THE_MAGICIAN); }

    public static final RegistryObject<Item> THE_HIGH_PRIESTESS = ITEMS.register("the_high_priestess",
            () -> new Card(new Item.Properties(), PickupId.THE_HIGH_PRIESTESS.getId()));
    static { PICKUP_LIST.add(THE_HIGH_PRIESTESS); }

    public static final RegistryObject<Item> THE_EMPRESS = ITEMS.register("the_empress",
            () -> new Card(new Item.Properties(), PickupId.THE_EMPRESS.getId()));
    static { PICKUP_LIST.add(THE_EMPRESS); }

    public static final RegistryObject<Item> THE_HIEROPHANT = ITEMS.register("the_hierophant",
            () -> new Card(new Item.Properties(), PickupId.THE_HIEROPHANT.getId()));
    static { PICKUP_LIST.add(THE_HIEROPHANT); }

    public static final RegistryObject<Item> THE_LOVERS = ITEMS.register("the_lovers",
            () -> new Card(new Item.Properties(), PickupId.THE_LOVERS.getId()));
    static { PICKUP_LIST.add(THE_LOVERS); }

    public static final RegistryObject<Item> THE_CHARIOT = ITEMS.register("the_chariot",
            () -> new Card(new Item.Properties(), PickupId.THE_CHARIOT.getId()));
    static { PICKUP_LIST.add(THE_CHARIOT); }

    public static final RegistryObject<Item> JUSTICE = ITEMS.register("justice",
            () -> new Card(new Item.Properties(), PickupId.JUSTICE.getId()));
    static { PICKUP_LIST.add(JUSTICE); }

    public static final RegistryObject<Item> THE_HANGED_MAN = ITEMS.register("the_hanged_man",
            () -> new Card(new Item.Properties(), PickupId.THE_HANGED_MAN.getId()));
    static { PICKUP_LIST.add(THE_HANGED_MAN); }

    public static final RegistryObject<Item> DEATH = ITEMS.register("death",
            () -> new Card(new Item.Properties(), PickupId.DEATH.getId()));
    static { PICKUP_LIST.add(DEATH); }

    public static final RegistryObject<Item> THE_DEVIL = ITEMS.register("the_devil",
            () -> new Card(new Item.Properties(), PickupId.THE_DEVIL.getId()));
    static { PICKUP_LIST.add(THE_DEVIL); }

    public static final RegistryObject<Item> THE_TOWER = ITEMS.register("the_tower",
            () -> new Card(new Item.Properties(), PickupId.THE_TOWER.getId()));
    static { PICKUP_LIST.add(THE_TOWER); }

    public static final RegistryObject<Item> THE_STARS = ITEMS.register("the_stars",
            () -> new Card(new Item.Properties(), PickupId.THE_STARS.getId()));
    static { PICKUP_LIST.add(THE_STARS); }

    public static final RegistryObject<Item> THE_SUN = ITEMS.register("the_sun",
            () -> new Card(new Item.Properties(), PickupId.THE_SUN.getId()));
    static { PICKUP_LIST.add(THE_SUN); }

    public static final RegistryObject<Item> THE_WORLD = ITEMS.register("the_world",
            () -> new Card(new Item.Properties(), PickupId.THE_WORLD.getId()));
    static { PICKUP_LIST.add(THE_WORLD); }

    public static final RegistryObject<Item> THE_MAGICIAN_R = ITEMS.register("the_magician_r",
            () -> new Card(new Item.Properties(), PickupId.THE_MAGICIAN_R.getId()));
    static { PICKUP_LIST.add(THE_MAGICIAN_R); }

    public static final RegistryObject<Item> THE_HIGH_PRIESTESS_R = ITEMS.register("the_high_priestess_r",
            () -> new Card(new Item.Properties(), PickupId.THE_HIGH_PRIESTESS_R.getId()));
    static { PICKUP_LIST.add(THE_HIGH_PRIESTESS_R); }

    public static final RegistryObject<Item> THE_EMPRESS_R = ITEMS.register("the_empress_r",
            () -> new Card(new Item.Properties(), PickupId.THE_EMPRESS_R.getId()));
    static { PICKUP_LIST.add(THE_EMPRESS_R); }

    public static final RegistryObject<Item> THE_HIEROPHANT_R = ITEMS.register("the_hierophant_r",
            () -> new Card(new Item.Properties(), PickupId.THE_HIEROPHANT_R.getId()));
    static { PICKUP_LIST.add(THE_HIEROPHANT_R); }

    public static final RegistryObject<Item> THE_CHARIOT_R = ITEMS.register("the_chariot_r",
            () -> new Card(new Item.Properties(), PickupId.THE_CHARIOT_R.getId()));
    static { PICKUP_LIST.add(THE_CHARIOT_R); }

    public static final RegistryObject<Item> JUSTICE_R = ITEMS.register("justice_r",
            () -> new Card(new Item.Properties(), PickupId.JUSTICE_R.getId()));
    static { PICKUP_LIST.add(JUSTICE_R); }

    public static final RegistryObject<Item> STRENGTH_R = ITEMS.register("strength_r",
            () -> new Card(new Item.Properties(), PickupId.STRENGTH_R.getId()));
    static { PICKUP_LIST.add(STRENGTH_R); }

    public static final RegistryObject<Item> TEMPERANCE_R = ITEMS.register("temperance_r",
            () -> new Card(new Item.Properties(), PickupId.TEMPERANCE_R.getId()));
    static { PICKUP_LIST.add(TEMPERANCE_R); }

    public static final RegistryObject<Item> TWO_OF_DIAMONDS = ITEMS.register("2_of_diamonds",
            () -> new Card(new Item.Properties(), PickupId.TWO_OF_DIAMONDS.getId()));
    static { PICKUP_LIST.add(TWO_OF_DIAMONDS); }

    public static final RegistryObject<Item> TWO_OF_CLUBS = ITEMS.register("2_of_clubs",
            () -> new Card(new Item.Properties(), PickupId.TWO_OF_CLUBS.getId()));
    static { PICKUP_LIST.add(TWO_OF_CLUBS); }

    public static final RegistryObject<Item> TWO_OF_HEARTS = ITEMS.register("2_of_hearts",
            () -> new Card(new Item.Properties(), PickupId.TWO_OF_HEARTS.getId()));
    static { PICKUP_LIST.add(TWO_OF_HEARTS); }

    public static final RegistryObject<Item> TWO_OF_SPADES = ITEMS.register("2_of_spades",
            () -> new Card(new Item.Properties(), PickupId.TWO_OF_SPADES.getId()));
    static { PICKUP_LIST.add(TWO_OF_SPADES); }

    public static final RegistryObject<Item> ACE_OF_CLUBS = ITEMS.register("ace_of_clubs",
            () -> new Card(new Item.Properties(), PickupId.ACE_OF_CLUBS.getId()));
    static { PICKUP_LIST.add(ACE_OF_CLUBS); }

    public static final RegistryObject<Item> ACE_OF_DIAMONDS = ITEMS.register("ace_of_diamonds",
            () -> new Card(new Item.Properties(), PickupId.ACE_OF_DIAMONDS.getId()));
    static { PICKUP_LIST.add(ACE_OF_DIAMONDS); }

    public static final RegistryObject<Item> ACE_OF_HEARTS = ITEMS.register("ace_of_hearts",
            () -> new Card(new Item.Properties(), PickupId.ACE_OF_HEARTS.getId()));
    static { PICKUP_LIST.add(ACE_OF_HEARTS); }

    public static final RegistryObject<Item> ACE_OF_SPADES = ITEMS.register("ace_of_spades",
            () -> new Card(new Item.Properties(), PickupId.ACE_OF_SPADES.getId()));
    static { PICKUP_LIST.add(ACE_OF_SPADES); }

    public static final RegistryObject<Item> QUEEN_OF_HEARTS = ITEMS.register("queen_of_hearts",
            () -> new Card(new Item.Properties(), PickupId.QUEEN_OF_HEARTS.getId()));
    static { PICKUP_LIST.add(QUEEN_OF_HEARTS); }

    public static final RegistryObject<Item> HOLY_CARD = ITEMS.register("holy_card",
            () -> new Card(new Item.Properties(), PickupId.HOLY_CARD.getId()));
    static { PICKUP_LIST.add(HOLY_CARD); }

    public static final RegistryObject<Item> ANCIENT_RECALL = ITEMS.register("ancient_recall",
            () -> new Card(new Item.Properties(), PickupId.ANCIENT_RECALL.getId()));
    static { PICKUP_LIST.add(ANCIENT_RECALL); }




    public static final RegistryObject<Item> PILL1 = ITEMS.register("pill1",
            () -> new Pill(new Item.Properties(), 1, false));
    public static final RegistryObject<Item> PILL2 = ITEMS.register("pill2",
            () -> new Pill(new Item.Properties(), 2, false));
    public static final RegistryObject<Item> PILL3 = ITEMS.register("pill3",
            () -> new Pill(new Item.Properties(), 3, false));
    public static final RegistryObject<Item> PILL4 = ITEMS.register("pill4",
            () -> new Pill(new Item.Properties(), 4, false));
    public static final RegistryObject<Item> PILL5 = ITEMS.register("pill5",
            () -> new Pill(new Item.Properties(), 5, false));
    public static final RegistryObject<Item> PILL6 = ITEMS.register("pill6",
            () -> new Pill(new Item.Properties(), 6, false));
    public static final RegistryObject<Item> PILL7 = ITEMS.register("pill7",
            () -> new Pill(new Item.Properties(), 7, false));
    public static final RegistryObject<Item> PILL8 = ITEMS.register("pill8",
            () -> new Pill(new Item.Properties(), 8, false));
    public static final RegistryObject<Item> PILL9 = ITEMS.register("pill9",
            () -> new Pill(new Item.Properties(), 9, false));
    public static final RegistryObject<Item> PILL10 = ITEMS.register("pill10",
            () -> new Pill(new Item.Properties(), 10, false));
    public static final RegistryObject<Item> PILL11 = ITEMS.register("pill11",
            () -> new Pill(new Item.Properties(), 11, false));
    public static final RegistryObject<Item> PILL12 = ITEMS.register("pill12",
            () -> new Pill(new Item.Properties(), 12, false));
    public static final RegistryObject<Item> PILL13 = ITEMS.register("pill13",
            () -> new Pill(new Item.Properties(), 13, false));
    public static final RegistryObject<Item> PILL1_H = ITEMS.register("pill1_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 1, true));
    public static final RegistryObject<Item> PILL2_H = ITEMS.register("pill2_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 2, true));
    public static final RegistryObject<Item> PILL3_H = ITEMS.register("pill3_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 3, true));
    public static final RegistryObject<Item> PILL4_H = ITEMS.register("pill4_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 4, true));
    public static final RegistryObject<Item> PILL5_H = ITEMS.register("pill5_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 5, true));
    public static final RegistryObject<Item> PILL6_H = ITEMS.register("pill6_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 6, true));
    public static final RegistryObject<Item> PILL7_H = ITEMS.register("pill7_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 7, true));
    public static final RegistryObject<Item> PILL8_H = ITEMS.register("pill8_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 8, true));
    public static final RegistryObject<Item> PILL9_H = ITEMS.register("pill9_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 9, true));
    public static final RegistryObject<Item> PILL10_H = ITEMS.register("pill10_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 10, true));
    public static final RegistryObject<Item> PILL11_H = ITEMS.register("pill11_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 11, true));
    public static final RegistryObject<Item> PILL12_H = ITEMS.register("pill12_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 12, true));
    public static final RegistryObject<Item> PILL13_H = ITEMS.register("pill13_h",
            () -> new Pill(new Item.Properties().stacksTo(16), 13, true));
    public static final RegistryObject<Item> GOLDEN_PILL = ITEMS.register("golden_pill",
            () -> new GoldenPill(new Item.Properties(), false));
    public static final RegistryObject<Item> GOLDEN_PILL_H = ITEMS.register("golden_pill_h",
            () -> new GoldenPill(new Item.Properties(), true));
    static {
        PICKUP_LIST.addAll(List.of(
                PILL1, PILL2, PILL3, PILL4, PILL5, PILL6, PILL7, PILL8, PILL9, PILL10, PILL11, PILL12, PILL13,
                PILL1_H, PILL2_H, PILL3_H, PILL4_H, PILL5_H, PILL6_H, PILL7_H, PILL8_H, PILL9_H, PILL10_H, PILL11_H, PILL12_H, PILL13_H,
                GOLDEN_PILL, GOLDEN_PILL_H
        ));
    }





    // pickup end
    // misc
    public static final RegistryObject<Item> DEBUG_STICK = ITEMS.register("debug_stick",
            () -> new DebugStick(new Item.Properties()));
    static { MISC_LIST.add(DEBUG_STICK); }

    public static final RegistryObject<Item> PEDESTAL_ITEM = ITEMS.register("pedestal",
            () -> new BlockItem(ModBlocks.PEDESTAL_BLOCK.get(), new Item.Properties()));
    static { MISC_LIST.add(PEDESTAL_ITEM); }

    public static final RegistryObject<Item> LOCK = ITEMS.register("lock",
            () -> new Item(new Item.Properties()));
    static { MISC_LIST.add(LOCK); }

    public static final RegistryObject<Item> NORMAL_CHEST_ITEM = ITEMS.register("chest",
            () -> new IsaacChestBlockItem(ModBlocks.NORMAL_CHEST_BLOCK.get(), new Item.Properties()));
    static { MISC_LIST.add(NORMAL_CHEST_ITEM); }

    public static final RegistryObject<Item> LOCKED_CHEST_ITEM = ITEMS.register("locked_chest",
            () -> new IsaacChestBlockItem(ModBlocks.LOCKED_CHEST_BLOCK.get(), new Item.Properties().rarity(Rarity.RARE)));
    static { MISC_LIST.add(LOCKED_CHEST_ITEM); }

    public static final RegistryObject<Item> OLD_CHEST_ITEM = ITEMS.register("old_chest",
            () -> new IsaacChestBlockItem(ModBlocks.OLD_CHEST_BLOCK.get(), new Item.Properties().rarity(Rarity.RARE)));
    static { MISC_LIST.add(OLD_CHEST_ITEM); }

    public static final RegistryObject<Item> ETERNAL_CHEST_ITEM = ITEMS.register("eternal_chest",
            () -> new IsaacChestBlockItem(ModBlocks.ETERNAL_CHEST_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));
    static { MISC_LIST.add(ETERNAL_CHEST_ITEM); }

    public static final RegistryObject<Item> RED_CHEST_ITEM = ITEMS.register("red_chest",
            () -> new IsaacChestBlockItem(ModBlocks.RED_CHEST_BLOCK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    static { MISC_LIST.add(RED_CHEST_ITEM); }

    // misc end


}

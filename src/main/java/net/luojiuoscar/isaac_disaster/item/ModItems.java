package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlocks;
import net.luojiuoscar.isaac_disaster.item.custom.DebugStick;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item.item.Trinket;
import net.luojiuoscar.isaac_disaster.item.item.custom.ExperimentalTreatmentItem;
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

import static net.luojiuoscar.isaac_disaster.manager.ItemListManager.*;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }


    // Passive items
    public static final RegistryObject<Item> BREAKFAST = ITEMS.register("breakfast",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.BREAKFAST.getId()));
    static { PASSIVE_ITEM_LIST.add(BREAKFAST); }

    public static final RegistryObject<Item> DESSERT = ITEMS.register("dessert",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.DESSERT.getId()));
    static { PASSIVE_ITEM_LIST.add(DESSERT); }

    public static final RegistryObject<Item> DINNER = ITEMS.register("dinner",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.DINNER.getId()));
    static { PASSIVE_ITEM_LIST.add(DINNER); }

    public static final RegistryObject<Item> LUNCH = ITEMS.register("lunch",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.LUNCH.getId()));
    static { PASSIVE_ITEM_LIST.add(LUNCH); }

    public static final RegistryObject<Item> SUPPER = ITEMS.register("supper",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.SUPPER.getId()));
    static { PASSIVE_ITEM_LIST.add(SUPPER); }

    public static final RegistryObject<Item> MIDNIGHT_SNACK = ITEMS.register("midnight_snack",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.MIDNIGHT_SNACK.getId()));
    static { PASSIVE_ITEM_LIST.add(MIDNIGHT_SNACK); }

    public static final RegistryObject<Item> ROTTEN_MEAT = ITEMS.register("rotten_meat",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.ROTTEN_MEAT.getId()));
    static { PASSIVE_ITEM_LIST.add(ROTTEN_MEAT); }

    public static final RegistryObject<Item> A_SNACK = ITEMS.register("a_snack",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.A_SNACK.getId()));
    static { PASSIVE_ITEM_LIST.add(A_SNACK); }

    public static final RegistryObject<Item> WOODEN_SPOON = ITEMS.register("wooden_spoon",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.WOODEN_SPOON.getId()));
    static { PASSIVE_ITEM_LIST.add(WOODEN_SPOON); }

    public static final RegistryObject<Item> STEVEN = ITEMS.register("steven",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.STEVEN.getId()));
    static { PASSIVE_ITEM_LIST.add(STEVEN); }

    public static final RegistryObject<Item> CRICKETS_HEAD = ITEMS.register("crickets_head",
            () -> new PassiveItem(new Item.Properties(),4, ItemId.CRICKETS_HEAD.getId()));
    static { PASSIVE_ITEM_LIST.add(CRICKETS_HEAD); }

    public static final RegistryObject<Item> THE_COMMON_COLD = ITEMS.register("the_common_cold",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.THE_COMMON_COLD.getId(), true));
    static { PASSIVE_ITEM_LIST.add(THE_COMMON_COLD); }

    public static final RegistryObject<Item> GLASS_EYE = ITEMS.register("glass_eye",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.GLASS_EYE.getId()));
    static { PASSIVE_ITEM_LIST.add(GLASS_EYE); }

    public static final RegistryObject<Item> CAR_BATTERY = ITEMS.register("car_battery",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.CAR_BATTERY.getId()));
    static { PASSIVE_ITEM_LIST.add(CAR_BATTERY); }

    public static final RegistryObject<Item> THE_BATTERY = ITEMS.register("the_battery",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.THE_BATTERY.getId()));
    static { PASSIVE_ITEM_LIST.add(THE_BATTERY); }

    public static final RegistryObject<Item> VOLT_9 = ITEMS.register("volt_9",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.VOLT_9.getId()));
    static { PASSIVE_ITEM_LIST.add(VOLT_9); }

    public static final RegistryObject<Item> VOLT_4P5 = ITEMS.register("volt_4p5",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.VOLT_4P5.getId()));
    static { PASSIVE_ITEM_LIST.add(VOLT_4P5); }

    public static final RegistryObject<Item> BOOM = ITEMS.register("boom",
            () -> new PassiveItem(new Item.Properties(),0, ItemId.BOOM.getId()));
    static { PASSIVE_ITEM_LIST.add(BOOM); }

    public static final RegistryObject<Item> MR_MEGA = ITEMS.register("mr_mega",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.MR_MEGA.getId()));
    static { PASSIVE_ITEM_LIST.add(MR_MEGA); }

    public static final RegistryObject<Item> BOMBER_BOY = ITEMS.register("bomber_boy",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.BOMBER_BOY.getId()));
    static { PASSIVE_ITEM_LIST.add(BOMBER_BOY); }

    public static final RegistryObject<Item> SCATTER_BOMB = ITEMS.register("scatter_bomb",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.SCATTER_BOMB.getId()));
    static { PASSIVE_ITEM_LIST.add(SCATTER_BOMB); }

    public static final RegistryObject<Item> FAST_BOMB = ITEMS.register("fast_bomb",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.FAST_BOMB.getId()));
    static { PASSIVE_ITEM_LIST.add(FAST_BOMB); }

    public static final RegistryObject<Item> BOBBY_BOMB = ITEMS.register("bobby_bomb",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.BOBBY_BOMB.getId()));
    static { PASSIVE_ITEM_LIST.add(BOBBY_BOMB); }

    public static final RegistryObject<Item> HOT_BOMB = ITEMS.register("hot_bomb",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.HOT_BOMB.getId()));
    static { PASSIVE_ITEM_LIST.add(HOT_BOMB); }

    public static final RegistryObject<Item> TRANSCENDENCE = ITEMS.register("transcendence",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.TRANSCENDENCE.getId(), true));
    static { PASSIVE_ITEM_LIST.add(TRANSCENDENCE); }

    public static final RegistryObject<Item> BLOOD_OF_THE_MARTYR = ITEMS.register("blood_of_the_martyr",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.BLOOD_OF_THE_MARTYR.getId()));
    static { PASSIVE_ITEM_LIST.add(BLOOD_OF_THE_MARTYR); }

    public static final RegistryObject<Item> HOLY_MANTLE = ITEMS.register("holy_mantle",
            () -> new PassiveItem(new Item.Properties(),4, ItemId.HOLY_MANTLE.getId(), true));
    static { PASSIVE_ITEM_LIST.add(HOLY_MANTLE); }

    public static final RegistryObject<Item> THE_WAFER = ITEMS.register("the_wafer",
            () -> new PassiveItem(new Item.Properties(),4, ItemId.THE_WAFER.getId()));
    static { PASSIVE_ITEM_LIST.add(THE_WAFER); }

    public static final RegistryObject<Item> MONEY_IS_POWER = ITEMS.register("money_is_power",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.MONEY_IS_POWER.getId()));
    static { PASSIVE_ITEM_LIST.add(MONEY_IS_POWER); }

    public static final RegistryObject<Item> DEAD_DOVE = ITEMS.register("dead_dove",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.DEAD_DOVE.getId()));
    static { PASSIVE_ITEM_LIST.add(DEAD_DOVE); }

    public static final RegistryObject<Item> CUPIDS_ARROW = ITEMS.register("cupids_arrow",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.CUPIDS_ARROW.getId()));
    static { PASSIVE_ITEM_LIST.add(CUPIDS_ARROW); }

    public static final RegistryObject<Item> SPOON_BENDER = ITEMS.register("spoon_bender",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.SPOON_BENDER.getId()));
    static { PASSIVE_ITEM_LIST.add(SPOON_BENDER); }

    public static final RegistryObject<Item> ROID_RAGE = ITEMS.register("roid_rage",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.ROID_RAGE.getId(), true));
    static { PASSIVE_ITEM_LIST.add(ROID_RAGE); }

    public static final RegistryObject<Item> THE_SAD_ONION = ITEMS.register("the_sad_onion",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.THE_SAD_ONION.getId()));
    static { PASSIVE_ITEM_LIST.add(THE_SAD_ONION); }

    public static final RegistryObject<Item> WIRE_COAT_HANGER = ITEMS.register("wire_coat_hanger",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.WIRE_COAT_HANGER.getId()));
    static { PASSIVE_ITEM_LIST.add(WIRE_COAT_HANGER); }

    public static final RegistryObject<Item> SPEED_BALL = ITEMS.register("speed_ball",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.SPEED_BALL.getId(),true));
    static { PASSIVE_ITEM_LIST.add(SPEED_BALL); }

    public static final RegistryObject<Item> PISCES = ITEMS.register("pisces",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.PISCES.getId()));
    static { PASSIVE_ITEM_LIST.add(PISCES); }

    public static final RegistryObject<Item> MINI_MUSH = ITEMS.register("mini_mush",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.MINI_MUSH.getId(), true));
    static { PASSIVE_ITEM_LIST.add(MINI_MUSH); }

    public static final RegistryObject<Item> PHD = ITEMS.register("phd",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.PHD.getId()));
    static { PASSIVE_ITEM_LIST.add(PHD); }

    public static final RegistryObject<Item> FALSE_PHD = ITEMS.register("false_phd",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.FALSE_PHD.getId()));
    static { PASSIVE_ITEM_LIST.add(FALSE_PHD); }

    public static final RegistryObject<Item> A_QUARTER = ITEMS.register("a_quarter",
            () -> new PassiveItem(new Item.Properties(),0, ItemId.A_QUARTER.getId()));
    static { PASSIVE_ITEM_LIST.add(A_QUARTER); }

    public static final RegistryObject<Item> A_DOLLAR = ITEMS.register("a_dollar",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.A_DOLLAR.getId()));
    static { PASSIVE_ITEM_LIST.add(A_DOLLAR); }

    public static final RegistryObject<Item> THE_INNER_EYE = ITEMS.register("the_inner_eye",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.THE_INNER_EYE.getId()));
    static { PASSIVE_ITEM_LIST.add(THE_INNER_EYE); }

    public static final RegistryObject<Item> PERFECT_VISION = ITEMS.register("perfect_vision",
            () -> new PassiveItem(new Item.Properties(),4, ItemId.PERFECT_VISION.getId()));
    static { PASSIVE_ITEM_LIST.add(PERFECT_VISION); }

    public static final RegistryObject<Item> MUTANT_SPIDER = ITEMS.register("mutant_spider",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.MUTANT_SPIDER.getId()));
    static { PASSIVE_ITEM_LIST.add(MUTANT_SPIDER); }

    public static final RegistryObject<Item> POLYPHEMUS = ITEMS.register("polyphemus",
            () -> new PassiveItem(new Item.Properties(),4, ItemId.POLYPHEMUS.getId()));
    static { PASSIVE_ITEM_LIST.add(POLYPHEMUS); }

    public static final RegistryObject<Item> HEART = ITEMS.register("heart",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.HEART.getId()));
    static { PASSIVE_ITEM_LIST.add(HEART); }

    public static final RegistryObject<Item> RAW_LIVER = ITEMS.register("raw_liver",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.RAW_LIVER.getId()));
    static { PASSIVE_ITEM_LIST.add(RAW_LIVER); }

    public static final RegistryObject<Item> THE_BODY = ITEMS.register("the_body",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.THE_BODY.getId()));
    static { PASSIVE_ITEM_LIST.add(THE_BODY); }

    public static final RegistryObject<Item> GROWTH_HORMONES = ITEMS.register("growth_hormones",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.GROWTH_HORMONES.getId(), true));
    static { PASSIVE_ITEM_LIST.add(GROWTH_HORMONES); }

    public static final RegistryObject<Item> SYNTHOIL = ITEMS.register("synthoil",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.SYNTHOIL.getId(), true));
    static { PASSIVE_ITEM_LIST.add(SYNTHOIL); }

    public static final RegistryObject<Item> EXPERIMENTAL_TREATMENT = ITEMS.register("experimental_treatment",
            () -> new ExperimentalTreatmentItem(new Item.Properties(),2, ItemId.EXPERIMENTAL_TREATMENT.getId(), true));
    static { PASSIVE_ITEM_LIST.add(EXPERIMENTAL_TREATMENT); }

    public static final RegistryObject<Item> TORN_PHOTO = ITEMS.register("torn_photo",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.TORN_PHOTO.getId()));
    static { PASSIVE_ITEM_LIST.add(TORN_PHOTO); }

    public static final RegistryObject<Item> CAFFEINE_PILL = ITEMS.register("caffeine_pill",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.CAFFEINE_PILL.getId()));
    static { PASSIVE_ITEM_LIST.add(CAFFEINE_PILL); }

    public static final RegistryObject<Item> SAFETY_PIN = ITEMS.register("safety_pin",
            () -> new PassiveItem(new Item.Properties(),1, ItemId.SAFETY_PIN.getId()));
    static { PASSIVE_ITEM_LIST.add(SAFETY_PIN); }

    public static final RegistryObject<Item> MAGIC_MUSHROOM = ITEMS.register("magic_mushroom",
            () -> new PassiveItem(new Item.Properties(),4, ItemId.MAGIC_MUSHROOM.getId(), true));
    static { PASSIVE_ITEM_LIST.add(MAGIC_MUSHROOM); }

    public static final RegistryObject<Item> BLUE_CAP = ITEMS.register("blue_cap",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.BLUE_CAP.getId(), true));
    static { PASSIVE_ITEM_LIST.add(BLUE_CAP); }

    public static final RegistryObject<Item> HABIT = ITEMS.register("habit",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.HABIT.getId()));
    static { PASSIVE_ITEM_LIST.add(HABIT); }

    public static final RegistryObject<Item> RUBBER_CEMENT = ITEMS.register("rubber_cement",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.RUBBER_CEMENT.getId()));
    static { PASSIVE_ITEM_LIST.add(RUBBER_CEMENT); }

    public static final RegistryObject<Item> HOST_HAT = ITEMS.register("host_hat",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.HOST_HAT.getId()));
    static { PASSIVE_ITEM_LIST.add(HOST_HAT); }

    public static final RegistryObject<Item> PYROMANIAC = ITEMS.register("pyromaniac",
            () -> new PassiveItem(new Item.Properties(),4, ItemId.PYROMANIAC.getId()));
    static { PASSIVE_ITEM_LIST.add(PYROMANIAC); }

    public static final RegistryObject<Item> PYRO = ITEMS.register("pyro",
            () -> new PassiveItem(new Item.Properties(),3, ItemId.PYRO.getId()));
    static { PASSIVE_ITEM_LIST.add(PYRO); }

    public static final RegistryObject<Item> PIGGY_BANK = ITEMS.register("piggy_bank",
            () -> new PassiveItem(new Item.Properties(),2, ItemId.PIGGY_BANK.getId()));
    static { PASSIVE_ITEM_LIST.add(PIGGY_BANK); }

    public static final RegistryObject<Item> TINY_PLANET = ITEMS.register("tiny_planet",
            () -> new PassiveItem(new Item.Properties(),0, ItemId.TINY_PLANET.getId()));
    static { PASSIVE_ITEM_LIST.add(TINY_PLANET); }

    public static final RegistryObject<Item> MAGIC_SCAB = ITEMS.register("magic_scab",
            () -> new PassiveItem(new Item.Properties(), 1, ItemId.MAGIC_SCAB.getId()));
    static { PASSIVE_ITEM_LIST.add(MAGIC_SCAB); }

    public static final RegistryObject<Item> SCREW = ITEMS.register("screw",
            () -> new PassiveItem(new Item.Properties(), 2, ItemId.SCREW.getId()));
    static { PASSIVE_ITEM_LIST.add(SCREW); }

    public static final RegistryObject<Item> BLACK_CANDLE = ITEMS.register("black_candle",
            () -> new PassiveItem(new Item.Properties(), 3, ItemId.BLACK_CANDLE.getId()));
    static { PASSIVE_ITEM_LIST.add(BLACK_CANDLE); }

    public static final RegistryObject<Item> TAROT_CLOTH = ITEMS.register("tarot_cloth",
            () -> new PassiveItem(new Item.Properties(), 2, ItemId.TAROT_CLOTH.getId()));
    static { PASSIVE_ITEM_LIST.add(TAROT_CLOTH); }

    public static final RegistryObject<Item> WHORE_OF_BABYLON = ITEMS.register("whore_of_babylon",
            () -> new PassiveItem(new Item.Properties(), 2, ItemId.WHORE_OF_BABYLON.getId(), true));
    static { PASSIVE_ITEM_LIST.add(WHORE_OF_BABYLON); }

    public static final RegistryObject<Item> CURSE_OF_THE_TOWER = ITEMS.register("curse_of_the_tower",
            () -> new PassiveItem(new Item.Properties(), 0, ItemId.CURSE_OF_THE_TOWER.getId()));
    static { PASSIVE_ITEM_LIST.add(CURSE_OF_THE_TOWER); }

    public static final RegistryObject<Item> THE_SOUL = ITEMS.register("the_soul",
            () -> new PassiveItem(new Item.Properties(), 2, ItemId.THE_SOUL.getId(), true));
    static { PASSIVE_ITEM_LIST.add(THE_SOUL); }



    // passive end
    // Active item
    public static final RegistryObject<Item> YUM_HEART = ITEMS.register("yum_heart",
            () -> new ActiveItem(new Item.Properties(),1, ItemId.YUM_HEART.getId(),
                    8, 8));
    static { ACTIVE_ITEM_LIST.add(YUM_HEART); }

    public static final RegistryObject<Item> THE_BOOK_OF_BELIAL = ITEMS.register("the_book_of_belial",
            () -> new ActiveItem(new Item.Properties(),2, ItemId.THE_BOOK_OF_BELIAL.getId(),
                    8, 8, true));
    static { ACTIVE_ITEM_LIST.add(THE_BOOK_OF_BELIAL); }

    public static final RegistryObject<Item> BOOK_OF_SHADOW = ITEMS.register("book_of_shadow",
            () -> new ActiveItem(new Item.Properties(),3, ItemId.BOOK_OF_SHADOW.getId(),
                    12, 12, true));
    static { ACTIVE_ITEM_LIST.add(BOOK_OF_SHADOW); }

    public static final RegistryObject<Item> THE_BIBLE = ITEMS.register("the_bible",
            () -> new ActiveItem(new Item.Properties(),2, ItemId.THE_BIBLE.getId(),
                    12, 12, true));
    static { ACTIVE_ITEM_LIST.add(THE_BIBLE); }

    public static final RegistryObject<Item> THE_NECRONMICON = ITEMS.register("the_necronmicon",
            () -> new ActiveItem(new Item.Properties(),2, ItemId.THE_NECRONMICON.getId(),
                    6, 6, true));
    static { ACTIVE_ITEM_LIST.add(THE_NECRONMICON); }

    public static final RegistryObject<Item> WOODEN_NICKEL = ITEMS.register("wooden_nickel",
            () -> new ActiveItem(new Item.Properties(),1, ItemId.WOODEN_NICKEL.getId(),
                    3, 3));
    static { ACTIVE_ITEM_LIST.add(WOODEN_NICKEL); }

    public static final RegistryObject<Item> TELEPORT = ITEMS.register("teleport",
            () -> new ActiveItem(new Item.Properties(),0, ItemId.TELEPORT.getId(),
                    4, 4));
    static { ACTIVE_ITEM_LIST.add(TELEPORT); }

    public static final RegistryObject<Item> LEMON_MISHAP = ITEMS.register("lemon_mishap",
            () -> new ActiveItem(new Item.Properties(),1, ItemId.LEMON_MISHAP.getId(),
                    4, 4));
    static { ACTIVE_ITEM_LIST.add(LEMON_MISHAP); }

    public static final RegistryObject<Item> FREE_LEMONADE = ITEMS.register("free_lemonade",
            () -> new ActiveItem(new Item.Properties(),2, ItemId.FREE_LEMONADE.getId(),
                    6, 6));
    static { ACTIVE_ITEM_LIST.add(FREE_LEMONADE); }

    public static final RegistryObject<Item> THE_GAMEKID = ITEMS.register("the_gamekid",
            () -> new ActiveItem(new Item.Properties(),1, ItemId.THE_GAMEKID.getId(),
                    12, 12, true));
    static { ACTIVE_ITEM_LIST.add(THE_GAMEKID); }

    public static final RegistryObject<Item> UNICORN_STUMP = ITEMS.register("unicorn_stump",
            () -> new ActiveItem(new Item.Properties(),1, ItemId.UNICORN_STUMP.getId(),
                    12, 12, true));
    static { ACTIVE_ITEM_LIST.add(UNICORN_STUMP); }

    public static final RegistryObject<Item> MY_LITTLE_UNICORN = ITEMS.register("my_little_unicorn",
            () -> new ActiveItem(new Item.Properties(),1, ItemId.MY_LITTLE_UNICORN.getId(),
                    12, 12, true));
    static { ACTIVE_ITEM_LIST.add(MY_LITTLE_UNICORN); }

    public static final RegistryObject<Item> PLACEBO = ITEMS.register("placebo",
            () -> new ActiveItem(new Item.Properties(),1, ItemId.PLACEBO.getId(),
                    24, 24));
    static { ACTIVE_ITEM_LIST.add(PLACEBO); }

    public static final RegistryObject<Item> DIPLOPIA = ITEMS.register("diplopia",
            () -> new ActiveItem(new Item.Properties(),4, ItemId.DIPLOPIA.getId(),
                    0, 0));
    static { ACTIVE_ITEM_LIST.add(DIPLOPIA); }

    public static final RegistryObject<Item> CROOKED_PENNY = ITEMS.register("crooked_penny",
            () -> new ActiveItem(new Item.Properties(),3, ItemId.CROOKED_PENNY.getId(),
                    36, 36));
    static { ACTIVE_ITEM_LIST.add(CROOKED_PENNY); }

    public static final RegistryObject<Item> DULL_RAZOR = ITEMS.register("dull_razor",
            () -> new ActiveItem(new Item.Properties(),2, ItemId.DULL_RAZOR.getId(),
                    2, 2));
    static { ACTIVE_ITEM_LIST.add(DULL_RAZOR); }

    public static final RegistryObject<Item> TELEPATHY_FOR_DUMMIES = ITEMS.register("telepathy_for_dummies",
            () -> new ActiveItem(new Item.Properties(),1, ItemId.TELEPATHY_FOR_DUMMIES.getId(),
                    8, 8, true));
    static { ACTIVE_ITEM_LIST.add(TELEPATHY_FOR_DUMMIES); }

    public static final RegistryObject<Item> ANARCHIST_COOKBOOK = ITEMS.register("anarchist_cookbook",
            () -> new ActiveItem(new Item.Properties(),1, ItemId.ANARCHIST_COOKBOOK.getId(),
                    6, 6, true));
    static { ACTIVE_ITEM_LIST.add(ANARCHIST_COOKBOOK); }

    public static final RegistryObject<Item> SMELTER = ITEMS.register("smelter",
            () -> new ActiveItem(new Item.Properties(),2, ItemId.SMELTER.getId(),
                    12, 12));
    static { ACTIVE_ITEM_LIST.add(SMELTER); }



    // active end
    // trinket

    public static final RegistryObject<Item> SWALLOWED_PENNY = ITEMS.register("swallowed_penny",
            () -> new Trinket(new Item.Properties(), TrinketId.SWALLOWED_PENNY.getId()));
    static { TRINKET_LIST.add(SWALLOWED_PENNY); }

    public static final RegistryObject<Item> AAA_BATTERY = ITEMS.register("aaa_battery",
            () -> new Trinket(new Item.Properties(), TrinketId.AAA_BATTERY.getId()));
    static { TRINKET_LIST.add(AAA_BATTERY); }

    public static final RegistryObject<Item> BROKEN_REMOTE = ITEMS.register("broken_remote",
            () -> new Trinket(new Item.Properties(), TrinketId.BROKEN_REMOTE.getId()));
    static { TRINKET_LIST.add(BROKEN_REMOTE); }

    public static final RegistryObject<Item> CARTRIDGE = ITEMS.register("cartridge",
            () -> new Trinket(new Item.Properties(), TrinketId.CARTRIDGE.getId()));
    static { TRINKET_LIST.add(CARTRIDGE); }

    public static final RegistryObject<Item> LUCKY_ROCK = ITEMS.register("lucky_rock",
            () -> new Trinket(new Item.Properties(), TrinketId.LUCKY_ROCK.getId()));
    static { TRINKET_LIST.add(LUCKY_ROCK); }

    public static final RegistryObject<Item> LUCKY_TOE = ITEMS.register("lucky_toe",
            () -> new Trinket(new Item.Properties(), TrinketId.LUCKY_TOE.getId()));
    static { TRINKET_LIST.add(LUCKY_TOE); }

    public static final RegistryObject<Item> CANCER = ITEMS.register("cancer",
            () -> new Trinket(new Item.Properties(), TrinketId.CANCER.getId()));
    static { TRINKET_LIST.add(CANCER); }

    public static final RegistryObject<Item> BLIND_RAGE = ITEMS.register("blind_rage",
            () -> new Trinket(new Item.Properties(), TrinketId.BLIND_RAGE.getId()));
    static { TRINKET_LIST.add(BLIND_RAGE); }

    public static final RegistryObject<Item> PERFECTION = ITEMS.register("perfection",
            () -> new Trinket(new Item.Properties(), TrinketId.PERFECTION.getId()));
    static { TRINKET_LIST.add(PERFECTION); }




    // trinket end
    // pickups
    public static final RegistryObject<Item> ISAAC_HEAD = ITEMS.register("isaac_head",
            () -> new IsaacHead(new Item.Properties(), PickupId.ISAAC_HEAD.getId()));
    static { PICKUP_LIST.add(ISAAC_HEAD); }

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

    public static final RegistryObject<Item> PENNY = ITEMS.register("penny",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(PENNY); }

    public static final RegistryObject<Item> NICKEL = ITEMS.register("nickel",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(NICKEL); }

    public static final RegistryObject<Item> DIME = ITEMS.register("dime",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(DIME); }


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

    public static final RegistryObject<Item> THE_HIEROPHANT_R = ITEMS.register("the_hierophant_r",
            () -> new Card(new Item.Properties(), PickupId.THE_HIEROPHANT_R.getId()));
    static { PICKUP_LIST.add(THE_HIEROPHANT_R); }







    public static final RegistryObject<Item> TWO_OF_DIAMONDS = ITEMS.register("2_of_diamonds",
            () -> new Card(new Item.Properties(), PickupId.TWO_OF_DIAMONDS.getId()));
    static { PICKUP_LIST.add(TWO_OF_DIAMONDS); }

    public static final RegistryObject<Item> TWO_OF_CLUBS = ITEMS.register("2_of_clubs",
            () -> new Card(new Item.Properties(), PickupId.TWO_OF_CLUBS.getId()));
    static { PICKUP_LIST.add(TWO_OF_CLUBS); }

    public static final RegistryObject<Item> TWO_OF_HEARTS = ITEMS.register("2_of_hearts",
            () -> new Card(new Item.Properties(), PickupId.TWO_OF_HEARTS.getId()));
    static { PICKUP_LIST.add(TWO_OF_HEARTS); }

    public static final RegistryObject<Item> ACE_OF_CLUBS = ITEMS.register("ace_of_clubs",
            () -> new Card(new Item.Properties(), PickupId.ACE_OF_CLUBS.getId()));
    static { PICKUP_LIST.add(ACE_OF_CLUBS); }

    public static final RegistryObject<Item> ACE_OF_DIAMONDS = ITEMS.register("ace_of_diamonds",
            () -> new Card(new Item.Properties(), PickupId.ACE_OF_DIAMONDS.getId()));
    static { PICKUP_LIST.add(ACE_OF_DIAMONDS); }

    public static final RegistryObject<Item> ACE_OF_HEARTS = ITEMS.register("ace_of_hearts",
            () -> new Card(new Item.Properties(), PickupId.ACE_OF_HEARTS.getId()));
    static { PICKUP_LIST.add(ACE_OF_HEARTS); }

    public static final RegistryObject<Item> QUEEN_OF_HEARTS = ITEMS.register("queen_of_hearts",
            () -> new Card(new Item.Properties(), PickupId.QUEEN_OF_HEARTS.getId()));
    static { PICKUP_LIST.add(QUEEN_OF_HEARTS); }

    public static final RegistryObject<Item> HOLY_CARD = ITEMS.register("holy_card",
            () -> new Card(new Item.Properties(), PickupId.HOLY_CARD.getId()));
    static { PICKUP_LIST.add(HOLY_CARD); }

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
        PICKUP_LIST.add(PILL1);
        PICKUP_LIST.add(PILL2);
        PICKUP_LIST.add(PILL3);
        PICKUP_LIST.add(PILL4);
        PICKUP_LIST.add(PILL5);
        PICKUP_LIST.add(PILL6);
        PICKUP_LIST.add(PILL7);
        PICKUP_LIST.add(PILL8);
        PICKUP_LIST.add(PILL9);
        PICKUP_LIST.add(PILL10);
        PICKUP_LIST.add(PILL11);
        PICKUP_LIST.add(PILL12);
        PICKUP_LIST.add(PILL13);
        PICKUP_LIST.add(PILL1_H);
        PICKUP_LIST.add(PILL2_H);
        PICKUP_LIST.add(PILL3_H);
        PICKUP_LIST.add(PILL4_H);
        PICKUP_LIST.add(PILL5_H);
        PICKUP_LIST.add(PILL6_H);
        PICKUP_LIST.add(PILL7_H);
        PICKUP_LIST.add(PILL8_H);
        PICKUP_LIST.add(PILL9_H);
        PICKUP_LIST.add(PILL10_H);
        PICKUP_LIST.add(PILL11_H);
        PICKUP_LIST.add(PILL12_H);
        PICKUP_LIST.add(PILL13_H);
        PICKUP_LIST.add(GOLDEN_PILL);
        PICKUP_LIST.add(GOLDEN_PILL_H);
    }

    public static final RegistryObject<Item> CHEST_ITEM = ITEMS.register("chest",
            () -> new BlockItem(ModBlocks.CHEST_BLOCK.get(), new Item.Properties()));
    static { MISC_LIST.add(CHEST_ITEM); }





    // pickup end
    // misc
    public static final RegistryObject<Item> PEDESTAL_ITEM = ITEMS.register("pedestal",
            () -> new BlockItem(ModBlocks.PEDESTAL_BLOCK.get(), new Item.Properties()));
    static { MISC_LIST.add(PEDESTAL_ITEM); }

    public static final RegistryObject<Item> LOCK = ITEMS.register("lock",
            () -> new Item(new Item.Properties()));
    static { MISC_LIST.add(LOCK); }

    public static final RegistryObject<Item> DEBUG_STICK = ITEMS.register("debug_stick",
            () -> new DebugStick(new Item.Properties()));
    static { MISC_LIST.add(DEBUG_STICK); }


    // misc end


}

package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.item.ActiveItem;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item.pickup.Bomb;
import net.luojiuoscar.isaac_disaster.item.pickup.Heart;
import net.luojiuoscar.isaac_disaster.item_ability.pickup.items.RedHeart;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.PickupId;
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





    // Active item
    public static final RegistryObject<Item> YUM_HEART = ITEMS.register("yum_heart",
            () -> new ActiveItem(new Item.Properties(),1, ItemId.YUM_HEART.getId(),
                    800, 800));
    static { ACTIVE_ITEM_LIST.add(YUM_HEART); }

    public static final RegistryObject<Item> THE_BOOK_OF_BELIAL = ITEMS.register("the_book_of_belial",
            () -> new ActiveItem(new Item.Properties(),2, ItemId.THE_BOOK_OF_BELIAL.getId(),
                    800, 800, true));
    static { ACTIVE_ITEM_LIST.add(THE_BOOK_OF_BELIAL); }

    public static final RegistryObject<Item> BOOK_OF_SHADOW = ITEMS.register("book_of_shadow",
            () -> new ActiveItem(new Item.Properties(),3, ItemId.BOOK_OF_SHADOW.getId(),
                    1200, 1200, true));
    static { ACTIVE_ITEM_LIST.add(BOOK_OF_SHADOW); }

    public static final RegistryObject<Item> THE_BIBLE = ITEMS.register("the_bible",
            () -> new ActiveItem(new Item.Properties(),2, ItemId.THE_BIBLE.getId(),
                    1200, 1200, true));
    static { ACTIVE_ITEM_LIST.add(THE_BIBLE); }

    public static final RegistryObject<Item> THE_NECRONMICON = ITEMS.register("the_necronmicon",
            () -> new ActiveItem(new Item.Properties(),2, ItemId.THE_NECRONMICON.getId(),
                    600, 600));
    static { ACTIVE_ITEM_LIST.add(THE_NECRONMICON); }

    public static final RegistryObject<Item> WOODEN_NICKEL = ITEMS.register("wooden_nickel",
            () -> new ActiveItem(new Item.Properties(),1, ItemId.WOODEN_NICKEL.getId(),
                    300, 300));
    static { ACTIVE_ITEM_LIST.add(WOODEN_NICKEL); }



    // pickups
    public static final RegistryObject<Item> BOMB = ITEMS.register("bomb",
            () -> new Bomb(new Item.Properties(), PickupId.BOMB.getId()));
    static { PICKUP_LIST.add(BOMB); }

    public static final RegistryObject<Item> GIGA_BOMB = ITEMS.register("giga_bomb",
            () -> new Bomb(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), PickupId.GIGA_BOMB.getId()));
    static { PICKUP_LIST.add(GIGA_BOMB); }

    public static final RegistryObject<Item> GOLDEN_BOMB = ITEMS.register("golden_bomb",
            () -> new Bomb(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), PickupId.GOLDEN_BOMB.getId()));
    static { PICKUP_LIST.add(GOLDEN_BOMB); }

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

    public static final RegistryObject<Item> PENNY = ITEMS.register("penny",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(PENNY); }

    public static final RegistryObject<Item> NICKEL = ITEMS.register("nickel",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(NICKEL); }

    public static final RegistryObject<Item> DIME = ITEMS.register("dime",
            () -> new Item(new Item.Properties()));
    static { PICKUP_LIST.add(DIME); }

}

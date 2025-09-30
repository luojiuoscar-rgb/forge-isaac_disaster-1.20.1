package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.custom.NormalActiveItem;
import net.luojiuoscar.isaac_disaster.item.custom.NormalPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.ItemId;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


import static net.luojiuoscar.isaac_disaster.manager.ItemListManager.ACTIVE_ITEM_LIST;
import static net.luojiuoscar.isaac_disaster.manager.ItemListManager.PASSIVE_ITEM_LIST;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }


    // Passive items
    public static final RegistryObject<Item> BREAKFAST = ITEMS.register("breakfast",
            () -> new NormalPassiveItem(new Item.Properties(),1, ItemId.BREAKFAST.getId()));
    static { PASSIVE_ITEM_LIST.add(BREAKFAST); }

    public static final RegistryObject<Item> DESSERT = ITEMS.register("dessert",
            () -> new NormalPassiveItem(new Item.Properties(),1, ItemId.DESSERT.getId()));
    static { PASSIVE_ITEM_LIST.add(DESSERT); }

    public static final RegistryObject<Item> DINNER = ITEMS.register("dinner",
            () -> new NormalPassiveItem(new Item.Properties(),1, ItemId.DINNER.getId()));
    static { PASSIVE_ITEM_LIST.add(DINNER); }

    public static final RegistryObject<Item> LUNCH = ITEMS.register("lunch",
            () -> new NormalPassiveItem(new Item.Properties(),1, ItemId.LUNCH.getId()));
    static { PASSIVE_ITEM_LIST.add(LUNCH); }

    public static final RegistryObject<Item> SUPPER = ITEMS.register("supper",
            () -> new NormalPassiveItem(new Item.Properties(),1, ItemId.SUPPER.getId()));
    static { PASSIVE_ITEM_LIST.add(SUPPER); }

    public static final RegistryObject<Item> MIDNIGHT_SNACK = ITEMS.register("midnight_snack",
            () -> new NormalPassiveItem(new Item.Properties(),1, ItemId.MIDNIGHT_SNACK.getId()));
    static { PASSIVE_ITEM_LIST.add(MIDNIGHT_SNACK); }

    public static final RegistryObject<Item> ROTTEN_MEAT = ITEMS.register("rotten_meat",
            () -> new NormalPassiveItem(new Item.Properties(),1, ItemId.ROTTEN_MEAT.getId()));
    static { PASSIVE_ITEM_LIST.add(ROTTEN_MEAT); }

    public static final RegistryObject<Item> A_SNACK = ITEMS.register("a_snack",
            () -> new NormalPassiveItem(new Item.Properties(),1, ItemId.A_SNACK.getId()));
    static { PASSIVE_ITEM_LIST.add(A_SNACK); }


    public static final RegistryObject<Item> WOODEN_SPOON = ITEMS.register("wooden_spoon",
            () -> new NormalPassiveItem(new Item.Properties(),1, ItemId.WOODEN_SPOON.getId()));
    static { PASSIVE_ITEM_LIST.add(WOODEN_SPOON); }

    public static final RegistryObject<Item> STEVEN = ITEMS.register("steven",
            () -> new NormalPassiveItem(new Item.Properties(),3, ItemId.STEVEN.getId()));
    static { PASSIVE_ITEM_LIST.add(STEVEN); }

    public static final RegistryObject<Item> CRICKETS_HEAD = ITEMS.register("crickets_head",
            () -> new NormalPassiveItem(new Item.Properties(),4, ItemId.CRICKETS_HEAD.getId()));
    static { PASSIVE_ITEM_LIST.add(CRICKETS_HEAD); }

    public static final RegistryObject<Item> THE_COMMON_COLD = ITEMS.register("the_common_cold",
            () -> new NormalPassiveItem(new Item.Properties(),1, ItemId.THE_COMMON_COLD.getId()));
    static { PASSIVE_ITEM_LIST.add(THE_COMMON_COLD); }

    public static final RegistryObject<Item> GLASS_EYE = ITEMS.register("glass_eye",
            () -> new NormalPassiveItem(new Item.Properties(),3, ItemId.GLASS_EYE.getId()));
    static { PASSIVE_ITEM_LIST.add(GLASS_EYE); }

    public static final RegistryObject<Item> CAR_BATTERY = ITEMS.register("car_battery",
            () -> new NormalPassiveItem(new Item.Properties(),3, ItemId.CAR_BATTERY.getId()));
    static { PASSIVE_ITEM_LIST.add(CAR_BATTERY); }

    public static final RegistryObject<Item> THE_BATTERY = ITEMS.register("the_battery",
            () -> new NormalPassiveItem(new Item.Properties(),3, ItemId.THE_BATTERY.getId()));
    static { PASSIVE_ITEM_LIST.add(THE_BATTERY); }
















    // Active item
    public static final RegistryObject<Item> YUM_HEART = ITEMS.register("yum_heart",
            () -> new NormalActiveItem(new Item.Properties(),1, ItemId.YUM_HEART.getId(),
                    800, 800));
    static { ACTIVE_ITEM_LIST.add(YUM_HEART); }



}

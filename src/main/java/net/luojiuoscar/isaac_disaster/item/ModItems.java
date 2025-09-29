package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.custom.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.ItemId;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


import static net.luojiuoscar.isaac_disaster.manager.ModItemManager.PASSIVE_ITEM_LIST;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }


    // Passive items
    public static final RegistryObject<Item> BREAKFAST = ITEMS.register("breakfast",
            () -> new ModPassiveItems(new Item.Properties(),1, ItemId.BREAKFAST.getId()));
    static { PASSIVE_ITEM_LIST.add(BREAKFAST); }

    public static final RegistryObject<Item> DESSERT = ITEMS.register("dessert",
            () -> new ModPassiveItems(new Item.Properties(),1, ItemId.DESSERT.getId()));
    static { PASSIVE_ITEM_LIST.add(DESSERT); }

    public static final RegistryObject<Item> WOODEN_SPOON = ITEMS.register("wooden_spoon",
            () -> new ModPassiveItems(new Item.Properties(),1, ItemId.WOODEN_SPOON.getId()));
    static { PASSIVE_ITEM_LIST.add(WOODEN_SPOON); }

    public static final RegistryObject<Item> STEVEN = ITEMS.register("steven",
            () -> new ModPassiveItems(new Item.Properties(),3, ItemId.STEVEN.getId()));
    static { PASSIVE_ITEM_LIST.add(STEVEN); }

    public static final RegistryObject<Item> CRICKETS_HEAD = ITEMS.register("crickets_head",
            () -> new ModPassiveItems(new Item.Properties(),4, ItemId.CRICKETS_HEAD.getId()));
    static { PASSIVE_ITEM_LIST.add(CRICKETS_HEAD); }

    public static final RegistryObject<Item> THE_COMMON_COLD = ITEMS.register("the_common_cold",
            () -> new ModPassiveItems(new Item.Properties(),1, ItemId.THE_COMMON_COLD.getId()));
    static { PASSIVE_ITEM_LIST.add(THE_COMMON_COLD); }

    public static final RegistryObject<Item> GLASS_EYE = ITEMS.register("glass_eye",
            () -> new ModPassiveItems(new Item.Properties(),3, ItemId.GLASS_EYE.getId()));
    static { PASSIVE_ITEM_LIST.add(GLASS_EYE); }





}

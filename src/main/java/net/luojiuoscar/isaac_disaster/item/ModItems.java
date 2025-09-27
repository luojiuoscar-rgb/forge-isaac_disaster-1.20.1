package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.ItemIdManager;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


import java.util.List;

import static net.luojiuoscar.isaac_disaster.item.ModPassiveItems.PASSIVE_ITEM_LIST;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }


    // Passive items
    public static final RegistryObject<Item> BREAKFAST = ITEMS.register("breakfast",
            () -> new ModPassiveItems(new Item.Properties(),1, ItemIdManager.BREAKFAST));
    static { PASSIVE_ITEM_LIST.add(BREAKFAST); }

    public static final RegistryObject<Item> DESSERT = ITEMS.register("dessert",
            () -> new ModPassiveItems(new Item.Properties(),1, ItemIdManager.DESSERT));
    static { PASSIVE_ITEM_LIST.add(DESSERT); }

    public static final RegistryObject<Item> WOODEN_SPOON = ITEMS.register("wooden_spoon",
            () -> new ModPassiveItems(new Item.Properties(),1, ItemIdManager.WOODEN_SPOON));
    static { PASSIVE_ITEM_LIST.add(WOODEN_SPOON); }

    public static final RegistryObject<Item> STEVEN = ITEMS.register("steven",
            () -> new ModPassiveItems(new Item.Properties(),3, ItemIdManager.STEVEN));
    static { PASSIVE_ITEM_LIST.add(STEVEN); }

    public static final RegistryObject<Item> CRICKETS_HEAD = ITEMS.register("crickets_head",
            () -> new ModPassiveItems(new Item.Properties(),4, ItemIdManager.CRICKETS_HEAD));
    static { PASSIVE_ITEM_LIST.add(CRICKETS_HEAD); }









}

package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.ItemIdManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


import java.util.List;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IsaacDisaster.MOD_ID);

    // 1. breakfast
    public static final RegistryObject<Item> BREAKFAST = ITEMS.register("breakfast",
            () -> new ModPassiveItems(new Item.Properties().stacksTo(1), ItemIdManager.BREAKFAST));

    // 2. dessert
    public static final RegistryObject<Item> DESSERT = ITEMS.register("dessert",
            () -> new ModPassiveItems(new Item.Properties().stacksTo(1), ItemIdManager.DESSERT));


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

}

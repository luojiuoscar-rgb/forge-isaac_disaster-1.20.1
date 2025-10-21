package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.manager.ItemListManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IsaacDisaster.MOD_ID);

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }


    public static final RegistryObject<CreativeModeTab> PASSIVE_ITEMS_TAB = CREATIVE_MODE_TABS.register("passive_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BREAKFAST.get()))
                    .title(Component.translatable("creativetab.isaac_disaster.passive_items"))
                    .displayItems((params, output) -> {
                        //将道具添加进创造模式面板
                        ItemListManager.PASSIVE_ITEM_LIST.forEach(itemRegistry -> {
                            output.accept(itemRegistry.get());}
                        );
                    }).build());


    public static final RegistryObject<CreativeModeTab> ACTIVE_ITEMS_TAB = CREATIVE_MODE_TABS.register("active_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.YUM_HEART.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "passive_items_tab"))
                    .title(Component.translatable("creativetab.isaac_disaster.active_items"))
                    .displayItems((params, output) -> {
                        //将道具添加进创造模式面板
                        ItemListManager.ACTIVE_ITEM_LIST.forEach(itemRegistry -> {
                            output.accept(itemRegistry.get());}
                        );
                    }).build());

    public static final RegistryObject<CreativeModeTab> TRINKETS_TAB = CREATIVE_MODE_TABS.register("trinkets_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SWALLOWED_PENNY.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "active_items_tab"))
                    .title(Component.translatable("creativetab.isaac_disaster.trinkets"))
                    .displayItems((params, output) -> {
                        //将道具添加进创造模式面板
                        ItemListManager.TRINKET_LIST.forEach(itemRegistry -> {
                            output.accept(itemRegistry.get());}
                        );
                    }).build());

    public static final RegistryObject<CreativeModeTab> PICKUPS_TAB = CREATIVE_MODE_TABS.register("pickups_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BOMB.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "trinkets_tab"))
                    .title(Component.translatable("creativetab.isaac_disaster.pickups"))
                    .displayItems((params, output) -> {
                        //将道具添加进创造模式面板
                        ItemListManager.PICKUP_LIST.forEach(itemRegistry -> {
                            output.accept(itemRegistry.get());}
                        );
                    }).build());
}

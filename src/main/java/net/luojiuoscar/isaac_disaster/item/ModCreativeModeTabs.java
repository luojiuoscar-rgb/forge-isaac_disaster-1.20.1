package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.tags.ITag;

import java.util.Objects;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IsaacDisaster.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ISAAC_ITEMS_TAB = CREATIVE_MODE_TABS.register("isaac_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BREAKFAST.get()))
                    .title(Component.translatable("creativetab.isaac_disaster.isaac_item"))
                    .displayItems((params, output) -> {
                        //将被动道具添加进创造模式面板
                        ModPassiveItems.PASSIVE_ITEM_LIST.forEach(itemRegistry -> {
                            output.accept(itemRegistry.get());}
                        );
                    }).build());


    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }

}

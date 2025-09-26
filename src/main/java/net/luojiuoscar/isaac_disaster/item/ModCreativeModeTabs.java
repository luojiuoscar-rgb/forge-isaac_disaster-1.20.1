package net.luojiuoscar.isaac_disaster.item;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IsaacDisaster.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ISAAC_ITEMS_TAB = CREATIVE_MODE_TABS.register("isaac_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BREAKFAST.get()))
                    .title(Component.translatable("creativetab.isaac_disaster.isaac_item"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.BREAKFAST.get());
                        output.accept(ModItems.DESSERT.get());
                        output.accept(ModItems.WOODEN_SPOON.get());

                    }).build());


    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }

}

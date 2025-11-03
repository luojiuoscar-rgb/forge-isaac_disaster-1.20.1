package net.luojiuoscar.isaac_disaster.datagen;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.datagen.loot.ForgeGlobalLootModifiersProvider;
import net.luojiuoscar.isaac_disaster.datagen.loot.ModGlobalLootModifiersProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event){
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        //模型生成器
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));
        //注册物品TAG生成器（无需方块标签，传递空的CompletableFuture）
        generator.addProvider(event.includeServer(), new ModItemTagGenerator(
                packOutput,
                lookupProvider,
                CompletableFuture.completedFuture(null),
                existingFileHelper
        ));

        // Loot tables
        generator.addProvider(event.includeServer(), new ModLootTableProvider(packOutput));
        generator.addProvider(event.includeServer(), new ModGlobalLootModifiersProvider(packOutput));
        generator.addProvider(event.includeServer(), new ForgeGlobalLootModifiersProvider(packOutput));

    }
}

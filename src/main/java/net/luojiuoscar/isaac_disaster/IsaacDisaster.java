package net.luojiuoscar.isaac_disaster;

import com.mojang.logging.LogUtils;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.block.ModBlocks;
import net.luojiuoscar.isaac_disaster.commands.gamerule.ModGameRules;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.item.*;
import net.luojiuoscar.isaac_disaster.loot.ModLootModifiers;
import net.luojiuoscar.isaac_disaster.loot.ModLootTypes;
import net.luojiuoscar.isaac_disaster.manager.attack.AttackManager;
import net.luojiuoscar.isaac_disaster.manager.item_managers.*;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.registries.ModRegistries;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(IsaacDisaster.MOD_ID)
public class IsaacDisaster
{
    public static final String MOD_ID = "isaac_disaster";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();



    public IsaacDisaster(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModRegistries.register(modEventBus);

        ModAttributes.register(modEventBus);

        ModItems.register(modEventBus);
        ModPassiveItems.register(modEventBus);
        ModActiveItems.register(modEventBus);
        ModTrinkets.register(modEventBus);
        ModPills.register(modEventBus);

        ModCreativeModeTabs.register(modEventBus);


        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);

        ModSounds.register(modEventBus);
        ModEffects.register(modEventBus);
        ModLootTypes.register(modEventBus);
        ModLootModifiers.register(modEventBus);

        ModGameRules.register();



        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModMessages.register();

        event.enqueueWork(() -> {
            PickupManager.getInstance().init();
            AttackManager.getInstance().init();
        });
    }


    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }

}

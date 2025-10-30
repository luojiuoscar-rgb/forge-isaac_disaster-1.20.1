package net.luojiuoscar.isaac_disaster.block;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.custom.PedestalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, IsaacDisaster.MOD_ID);
    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }


    public static final RegistryObject<Block> PEDESTAL_BLOCK = BLOCKS.register("pedestal",
            () -> new PedestalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(0.5f, 1200f)
                    .noOcclusion()));

}

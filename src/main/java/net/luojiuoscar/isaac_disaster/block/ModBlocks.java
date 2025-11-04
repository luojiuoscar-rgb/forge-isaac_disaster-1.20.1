package net.luojiuoscar.isaac_disaster.block;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.custom.*;
import net.luojiuoscar.isaac_disaster.block.custom.chest.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
            () -> new PedestalBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(0.5f, 1200f)
                    .noOcclusion()));

    public static final RegistryObject<Block> NORMAL_CHEST_BLOCK = BLOCKS.register("chest",
            () -> new NormalChestBlock(BlockBehaviour.Properties.copy(Blocks.CHEST)));

    public static final RegistryObject<Block> LOCKED_CHEST_BLOCK = BLOCKS.register("locked_chest",
            () -> new LockedChestBlock(BlockBehaviour.Properties.copy(Blocks.CHEST)));

    public static final RegistryObject<Block> OLD_CHEST_BLOCK = BLOCKS.register("old_chest",
            () -> new OldChestBlock(BlockBehaviour.Properties.copy(Blocks.CHEST)));

    public static final RegistryObject<Block> ETERNAL_CHEST_BLOCK = BLOCKS.register("eternal_chest",
            () -> new EternalChestBlock(BlockBehaviour.Properties.copy(Blocks.CHEST).lightLevel(state -> 15)));

    public static final RegistryObject<Block> RED_CHEST_BLOCK = BLOCKS.register("red_chest",
            () -> new RedChestBlock(BlockBehaviour.Properties.copy(Blocks.CHEST)));

}

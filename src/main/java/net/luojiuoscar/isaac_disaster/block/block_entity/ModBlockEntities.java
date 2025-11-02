package net.luojiuoscar.isaac_disaster.block.block_entity;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlocks;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.NormalChestBlockEntity;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.LockedChestChestBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, IsaacDisaster.MOD_ID);
    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }


    public static final RegistryObject<BlockEntityType<PedestalBlockEntity>> PEDESTAL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("pedestal_block_entity",
                    () -> BlockEntityType.Builder.of(PedestalBlockEntity::new,
                                    ModBlocks.PEDESTAL_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<NormalChestBlockEntity>> NORMAL_CHEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("chest_block_entity",
                    () -> BlockEntityType.Builder.of(NormalChestBlockEntity::new,
                                    ModBlocks.NORMAL_CHEST_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<LockedChestChestBlockEntity>> LOCKED_CHEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("locked_chest_block_entity",
                    () -> BlockEntityType.Builder.of(LockedChestChestBlockEntity::new,
                            ModBlocks.LOCKED_CHEST_BLOCK.get()).build(null));

}

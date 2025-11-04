package net.luojiuoscar.isaac_disaster.block;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalBlockEntity;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.*;
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

    public static final RegistryObject<BlockEntityType<LockedChestBlockEntity>> LOCKED_CHEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("locked_chest_block_entity",
                    () -> BlockEntityType.Builder.of(LockedChestBlockEntity::new,
                            ModBlocks.LOCKED_CHEST_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<OldChestBlockEntity>> OLD_CHEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("old_chest_block_entity",
                    () -> BlockEntityType.Builder.of(OldChestBlockEntity::new,
                            ModBlocks.OLD_CHEST_BLOCK.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<EternalChestBlockEntity>> ETERNAL_CHEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("eternal_chest_block_entity",
                    () -> BlockEntityType.Builder.of(EternalChestBlockEntity::new,
                            ModBlocks.ETERNAL_CHEST_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<RedChestBlockEntity>> RED_CHEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("red_chest_block_entity",
                    () -> BlockEntityType.Builder.of(RedChestBlockEntity::new,
                            ModBlocks.RED_CHEST_BLOCK.get()).build(null));

}

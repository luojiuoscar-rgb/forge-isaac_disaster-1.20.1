package net.luojiuoscar.isaac_disaster.block;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalBlockEntity;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.*;
import net.luojiuoscar.isaac_disaster.block.block_entity.identifier.*;
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

    public static final RegistryObject<BlockEntityType<WoodenChestBlockEntity>> WOODEN_CHEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("wooden_chest_block_entity",
                    () -> BlockEntityType.Builder.of(WoodenChestBlockEntity::new,
                            ModBlocks.WOODEN_CHEST_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<BombChestBlockEntity>> BOMB_CHEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("bomb_chest_block_entity",
                    () -> BlockEntityType.Builder.of(BombChestBlockEntity::new,
                            ModBlocks.BOMB_CHEST_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<ChestPlaceholderBlockEntity>> CHEST_PLACEHOLDER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("chest_placeholder_block_entity",
                    () -> BlockEntityType.Builder.of(ChestPlaceholderBlockEntity::new,
                            ModBlocks.CHEST_PLACEHOLDER_BLOCK.get()).build(null));

    // identifiers
    public static final RegistryObject<BlockEntityType<TreasureIdentifierBlockEntity>> TREASURE_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("treasure_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(TreasureIdentifierBlockEntity::new,
                            ModBlocks.TREASURE_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<NormalIdentifierBlockEntity>> NORMAL_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("normal_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(NormalIdentifierBlockEntity::new,
                            ModBlocks.NORMAL_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<ShopIdentifierBlockEntity>> SHOP_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("shop_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(ShopIdentifierBlockEntity::new,
                            ModBlocks.SHOP_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<SecretIdentifierBlockEntity>> SECRET_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("secret_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(SecretIdentifierBlockEntity::new,
                            ModBlocks.SECRET_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<SuperSecretIdentifierBlockEntity>> SUPER_SECRET_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("super_secret_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(SuperSecretIdentifierBlockEntity::new,
                            ModBlocks.SUPER_SECRET_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<UltraSecretIdentifierBlockEntity>> ULTRA_SECRET_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("ultra_secret_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(UltraSecretIdentifierBlockEntity::new,
                            ModBlocks.ULTRA_SECRET_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<CurseIdentifierBlockEntity>> CURSE_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("curse_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(CurseIdentifierBlockEntity::new,
                            ModBlocks.CURSE_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<LibraryIdentifierBlockEntity>> LIBRARY_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("library_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(LibraryIdentifierBlockEntity::new,
                            ModBlocks.LIBRARY_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<GambleIdentifierBlockEntity>> GAMBLE_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("gamble_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(GambleIdentifierBlockEntity::new,
                            ModBlocks.GAMBLE_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<BossIdentifierBlockEntity>> BOSS_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("boss_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(BossIdentifierBlockEntity::new,
                            ModBlocks.BOSS_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<EliteIdentifierBlockEntity>> ELITE_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("elite_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(EliteIdentifierBlockEntity::new,
                            ModBlocks.ELITE_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<AngelIdentifierBlockEntity>> ANGEL_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("angel_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(AngelIdentifierBlockEntity::new,
                            ModBlocks.ANGEL_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<DevilIdentifierBlockEntity>> DEVIL_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("devil_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(DevilIdentifierBlockEntity::new,
                            ModBlocks.DEVIL_IDENTIFIER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<PlanetariumIdentifierBlockEntity>> PLANETARIUM_IDENTIFIER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("planetarium_identifier_block_entity",
                    () -> BlockEntityType.Builder.of(PlanetariumIdentifierBlockEntity::new,
                            ModBlocks.PLANETARIUM_IDENTIFIER_BLOCK.get()).build(null));




}

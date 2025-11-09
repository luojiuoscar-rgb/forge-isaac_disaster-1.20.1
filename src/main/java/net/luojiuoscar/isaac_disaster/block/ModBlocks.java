package net.luojiuoscar.isaac_disaster.block;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.custom.*;
import net.luojiuoscar.isaac_disaster.block.custom.chest.*;
import net.luojiuoscar.isaac_disaster.block.custom.identifier.*;
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

    public static final RegistryObject<Block> WOODEN_CHEST_BLOCK = BLOCKS.register("wooden_chest",
            () -> new WoodenChest(BlockBehaviour.Properties.copy(Blocks.CHEST)));

    public static final RegistryObject<Block> BOMB_CHEST_BLOCK = BLOCKS.register("bomb_chest",
            () -> new BombChest(BlockBehaviour.Properties.copy(Blocks.STONE)));

    public static final RegistryObject<Block> CHEST_PLACEHOLDER_BLOCK = BLOCKS.register("chest_placeholder",
            () -> new ChestPlaceholderBlock(BlockBehaviour.Properties.copy(Blocks.GLASS).strength(0f)));

    // identifiers
    public static final RegistryObject<Block> NORMAL_IDENTIFIER_BLOCK = BLOCKS.register("normal_identifier",
            () -> new NormalIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));
    public static final RegistryObject<Block> TREASURE_IDENTIFIER_BLOCK = BLOCKS.register("treasure_identifier",
            () -> new TreasureIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));
    public static final RegistryObject<Block> SHOP_IDENTIFIER_BLOCK = BLOCKS.register("shop_identifier",
            () -> new ShopIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));
    public static final RegistryObject<Block> SECRET_IDENTIFIER_BLOCK = BLOCKS.register("secret_identifier",
            () -> new SecretIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));
    public static final RegistryObject<Block> SUPER_SECRET_IDENTIFIER_BLOCK = BLOCKS.register("super_secret_identifier",
            () -> new SuperSecretIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));
    public static final RegistryObject<Block> ULTRA_SECRET_IDENTIFIER_BLOCK = BLOCKS.register("ultra_secret_identifier",
            () -> new UltraSecretIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));
    public static final RegistryObject<Block> CURSE_IDENTIFIER_BLOCK = BLOCKS.register("curse_identifier",
            () -> new CurseIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));
    public static final RegistryObject<Block> LIBRARY_IDENTIFIER_BLOCK = BLOCKS.register("library_identifier",
            () -> new LibraryIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));
    public static final RegistryObject<Block> GAMBLE_IDENTIFIER_BLOCK = BLOCKS.register("gamble_identifier",
            () -> new GambleIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));
    public static final RegistryObject<Block> BOSS_IDENTIFIER_BLOCK = BLOCKS.register("boss_identifier",
            () -> new BossIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));
    public static final RegistryObject<Block> ELITE_IDENTIFIER_BLOCK = BLOCKS.register("elite_identifier",
            () -> new EliteIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));
    public static final RegistryObject<Block> ANGEL_IDENTIFIER_BLOCK = BLOCKS.register("angel_identifier",
            () -> new AngelIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));
    public static final RegistryObject<Block> DEVIL_IDENTIFIER_BLOCK = BLOCKS.register("devil_identifier",
            () -> new DevilIdentifierBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .noOcclusion().noCollission()));



}

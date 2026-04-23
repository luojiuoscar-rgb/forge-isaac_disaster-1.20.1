package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class WoodenChestBlockEntity extends ItemChestBlockEntity {

    public WoodenChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WOODEN_CHEST_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void init(){
        this.setLocked(false);
        this.setItemLootChance(0.1);
        this.setItemLootTable(ModLootTables.POOL_WOODEN_CHEST.toString());
    }

    @Override
    public ResourceLocation getPresetLootTable(){
        return ModLootTables.WOODEN_CHEST;
    }

    @Override
    public ResourceLocation getLidResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/wooden_chest_lid");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.isaac_disaster.wooden_chest");
    }
}

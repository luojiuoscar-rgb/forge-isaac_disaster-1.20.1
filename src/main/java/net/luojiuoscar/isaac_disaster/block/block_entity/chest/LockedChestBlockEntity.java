package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.manager.LootTableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class LockedChestBlockEntity extends ItemChestBlockEntity {

    public LockedChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOCKED_CHEST_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void init(){
        this.setLocked(true);
        this.setItemLootChance(0.2);
        this.setItemLootTable(LootTableManager.POOL_LOCKED_CHEST.toString());
    }

    @Override
    public ResourceLocation getPresetLootTable(){
        return LootTableManager.LOCKED_CHEST;
    }

    @Override
    public ResourceLocation getLidResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/locked_chest_lid");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.isaac_disaster.locked_chest");
    }
}

package net.luojiuoscar.isaac_disaster.block.block_entity;

import net.luojiuoscar.isaac_disaster.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IsaacChestBlockEntity extends ChestBlockEntity implements MenuProvider {

    public IsaacChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEST_BLOCK_ENTITY.get(), pos, state);
    }
}

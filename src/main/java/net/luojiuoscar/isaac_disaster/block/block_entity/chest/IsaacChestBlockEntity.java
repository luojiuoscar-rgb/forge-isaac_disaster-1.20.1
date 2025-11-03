package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class IsaacChestBlockEntity extends ChestBlockEntity implements MenuProvider {
    public IsaacChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    abstract public ResourceLocation getLidResourceLocation();

    @Override
    abstract public @NotNull Component getDisplayName();
}

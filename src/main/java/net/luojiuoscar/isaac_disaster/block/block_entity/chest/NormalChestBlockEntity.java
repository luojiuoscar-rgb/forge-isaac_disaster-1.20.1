package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class NormalChestBlockEntity extends IsaacChestBlockEntity {

    public NormalChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NORMAL_CHEST_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public ResourceLocation getLidResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/chest_lid");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.isaac_disaster.chest");
    }
}

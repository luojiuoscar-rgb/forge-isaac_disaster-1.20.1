package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class OldChestBlockEntity extends ItemChestBlockEntity {

    public OldChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OLD_CHEST_BLOCK_ENTITY.get(), pos, state);

        this.setLocked(true);
    }

    @Override
    public ResourceLocation getLidResourceLocation() {
        return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/old_chest_lid");
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.isaac_disaster.old_chest");
    }
}

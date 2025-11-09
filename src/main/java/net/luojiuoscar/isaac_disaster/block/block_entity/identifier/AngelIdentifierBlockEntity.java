package net.luojiuoscar.isaac_disaster.block.block_entity.identifier;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class AngelIdentifierBlockEntity extends IdentifierBlockEntity {
    public AngelIdentifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANGEL_IDENTIFIER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public ResourceLocation getResourceLocation(){
        return ModBlockEntities.ANGEL_IDENTIFIER_BLOCK_ENTITY.getId();
    }

    @Override
    public ResourceLocation getModelResource(){
        return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/angel_identifier");
    }
}

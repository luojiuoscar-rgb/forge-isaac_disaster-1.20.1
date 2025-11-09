package net.luojiuoscar.isaac_disaster.block.block_entity.identifier;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class TreasureIdentifierBlockEntity extends IdentifierBlockEntity {
    public TreasureIdentifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TREASURE_IDENTIFIER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public ResourceLocation getResourceLocation(){
        return ModBlockEntities.TREASURE_IDENTIFIER_BLOCK_ENTITY.getId();
    }

    @Override
    public ResourceLocation getModelResource(){
        return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/treasure_identifier");
    }
}

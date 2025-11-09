package net.luojiuoscar.isaac_disaster.block.block_entity.identifier;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class UltraSecretIdentifierBlockEntity extends IdentifierBlockEntity {
    public UltraSecretIdentifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ULTRA_SECRET_IDENTIFIER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public ResourceLocation getResourceLocation(){
        return ModBlockEntities.ULTRA_SECRET_IDENTIFIER_BLOCK_ENTITY.getId();
    }

    @Override
    public ResourceLocation getModelResource(){
        return ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/ultra_secret_identifier");
    }
}

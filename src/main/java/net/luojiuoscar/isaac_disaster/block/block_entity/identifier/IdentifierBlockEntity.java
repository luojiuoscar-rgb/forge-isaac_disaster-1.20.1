package net.luojiuoscar.isaac_disaster.block.block_entity.identifier;

import net.luojiuoscar.isaac_disaster.commands.gamerule.ModGameRules;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class IdentifierBlockEntity extends BlockEntity {
    public IdentifierBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract ResourceLocation getResourceLocation();

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide){
            BlockData.get((ServerLevel) level).addIdentifier(getResourceLocation(), this.worldPosition);
        }
    }

    public abstract ResourceLocation getModelResource();

    public static <T extends IdentifierBlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (level.isClientSide) return;

        double range = 3.0;
        Player player = LevelHelper.findNearestOfType(
                level,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                range,
                Player.class,
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator()
        );

        if (player != null && !player.isCreative() && !player.isSpectator() &&
                !level.getGameRules().getBoolean(ModGameRules.DISABLE_PLACEHOLDER)){
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        }
    }
}

package net.luojiuoscar.isaac_disaster.block.block_entity.chest;

import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.block.ModBlocks;
import net.luojiuoscar.isaac_disaster.commands.gamerule.ModGameRules;
import net.luojiuoscar.isaac_disaster.helper.LevelHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.id_managers.TrinketId;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ChestPlaceholderBlockEntity extends ItemChestBlockEntity {
    public ChestPlaceholderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEST_PLACEHOLDER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void init(){
    }

    @Override
    public ResourceLocation getLidResourceLocation() {
        return null;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.isaac_disaster.chest_placeholder");
    }

    public static <T extends ItemChestBlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (level.isClientSide || level.getGameTime() % 5 != 0 || level.getGameRules().getBoolean(ModGameRules.DISABLE_PLACEHOLDER)) return;

        double range = 6.0;

        Player player = LevelHelper.findNearestOfType(
                level,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                6.0,
                Player.class,
                e -> e instanceof Player p && !p.isCreative() && !p.isSpectator()
        );

        BlockState oldState = level.getBlockState(pos);
        BlockState newState = null;

        // 替换成随机方块
        if (player != null) {
            if (PlayerHelper.hasTrinket(TrinketId.THE_LEFT_HAND.getId(), (ServerPlayer) player)){
                newState = ModBlocks.RED_CHEST_BLOCK.get().defaultBlockState();

            }else if (PlayerHelper.hasTrinket(TrinketId.GILDED_KEY.getId(), (ServerPlayer) player)){
                newState = ModBlocks.LOCKED_CHEST_BLOCK.get().defaultBlockState();

            }else{
                newState = ModBlocks.LOCKED_CHEST_BLOCK.get().defaultBlockState();
//                int val = level.getRandom().nextInt(0, 10);
//                if (val < 7){
//                    newState = ModBlocks.NORMAL_CHEST_BLOCK.get().defaultBlockState();
//                }else if(val < 8){
//                    newState = ModBlocks.LOCKED_CHEST_BLOCK.get().defaultBlockState();
//                }else if(val < 9){
//                    newState = ModBlocks.BOMB_CHEST_BLOCK.get().defaultBlockState();
//                }else{
//                    newState = ModBlocks.RED_CHEST_BLOCK.get().defaultBlockState();
//                }
            }

            // facing
            newState = newState.setValue(HorizontalDirectionalBlock.FACING, oldState.getValue(HorizontalDirectionalBlock.FACING));

            level.setBlock(pos, newState, 7);
            // 继承数据
            if (level.getBlockEntity(pos) instanceof ItemChestBlockEntity newEntity) {
                newEntity.init();

                if (!blockEntity.getItemLootTable().isEmpty()){
                    newEntity.setItemLootTable(blockEntity.getItemLootTable());
                }
                if (blockEntity.getItemLootChance() != -1){
                    newEntity.setItemLootChance(blockEntity.getItemLootChance());
                }
                if (blockEntity.getLootTable() == null){
                    newEntity.setLootTable(blockEntity.getLootTable(), 0);
                }

            }
        }
    }






    @Override
    public ResourceLocation getPresetLootTable(){
        return null;
    }
}

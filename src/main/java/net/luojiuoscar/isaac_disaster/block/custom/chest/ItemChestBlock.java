package net.luojiuoscar.isaac_disaster.block.custom.chest;

import net.luojiuoscar.isaac_disaster.block.block_entity.chest.ItemChestBlockEntity;
import net.luojiuoscar.isaac_disaster.block.custom.ItemDisplayContainerBlock;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class ItemChestBlock extends IsaacChestBlock implements ItemDisplayContainerBlock {
    public ItemChestBlock(Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntitySupplier) {
        super(properties, blockEntitySupplier);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {

        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel) ||
                !(level.getBlockEntity(pos) instanceof ItemChestBlockEntity chest)) return InteractionResult.SUCCESS;

        ItemStack held = player.getItemInHand(hand).copy();
        ItemStack store = chest.getItem(0).copy();

        if (!chest.isOpened()){

            if (chest.isLocked()) {
                PlayerHelper.unlockBlock(player, hand, pos, 1, () -> chest.setLocked(false));
            }

            if (!chest.isLocked()){
                chest.unpackLootTable(player); // 尝试掉落物
                boolean spawnedItem = chest.tryLootItem(serverLevel, player, pos); // 尝试生成道具

                if (!spawnedItem) {
                    return super.use(state, level, pos, player, hand, hit); // 打开箱子
                }else{
                    level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                            SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }

        } else if (chest.isDisplayingItem()){ // 获取物品
            if (held.isEmpty()){
                acquireItem(player, hand, store);

                chest.clearItemDisplayList();
                chest.clearContent();
                chest.setDisplayingItem(false);
                BlockData.get(serverLevel).removeItemBlock(pos);

                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7f, 1.0f);
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }

        }else{
            return super.use(state, level, pos, player, hand, hit);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving){
        if (!level.isClientSide){
            BlockData.get((ServerLevel) level).removeItemBlock(pos);
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ItemChestBlockEntity chest && stack.hasTag()) {
            chest.load(stack.getTag().getCompound("BlockEntityTag"));
        }
    }
}

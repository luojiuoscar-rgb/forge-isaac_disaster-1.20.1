package net.luojiuoscar.isaac_disaster.block.custom.chest;

import net.luojiuoscar.isaac_disaster.block.block_entity.chest.ItemChestBlockEntity;
import net.luojiuoscar.isaac_disaster.block.block_entity.chest.LockedChestChestBlockEntity;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
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

public abstract class ItemChestBlock extends IsaacChestBlock{
    public ItemChestBlock(Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntitySupplier) {
        super(properties, blockEntitySupplier);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel) ||
                !(level.getBlockEntity(pos) instanceof LockedChestChestBlockEntity chest)) return InteractionResult.SUCCESS;

        ItemStack held = player.getItemInHand(hand).copy();
        ItemStack store = chest.getItem(0).copy();

        if (!chest.isOpened()){
            if (chest.isLocked()) {
                PlayerHelper.unlockBlock(player, hand, pos, () -> chest.setLocked(false));
            }

            if (!chest.isLocked()){
                chest.unpackLootTable(player); // 尝试掉落物
                chest.tryLootItem(serverLevel); // 尝试生成道具
            }

        } else if (chest.isDisplayingItem()){ // 获取物品
            if (held.isEmpty()){
                player.setItemInHand(hand, store);
                chest.setItem(0, ItemStack.EMPTY);
                chest.setDisplayingItem(false);

                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7f, 1.0f);
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }

        }else{
            super.use(state, level, pos, player, hand, hit);
        }



        return InteractionResult.SUCCESS;
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

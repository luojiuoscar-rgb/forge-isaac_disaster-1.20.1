package net.luojiuoscar.isaac_disaster.block.custom;

import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalBlockEntity;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.custom.DebugStick;
import net.luojiuoscar.isaac_disaster.manager.data.PedestalData;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class PedestalBlock extends BaseEntityBlock {

    public PedestalBlock(Properties properties) {
        super(properties);
    }

    // 显示方块模型
    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // 创建 BlockEntity
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.box(0.0625, 0.0, 0.0625, 0.9375, 0.3125, 0.9375);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getShape(state, world, pos, context);
    }

    // 玩家右键交互
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ServerLevel serverLevel = (ServerLevel) level;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PedestalBlockEntity pedestal)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand).copy();
        ItemStack stored = pedestal.getItem().copy();

        // debug stick
        if (held.getItem() instanceof DebugStick){

            if (!DebugStick.hasStoredPos(held)){
                DebugStick.saveBlockPos(held, pos);
                player.displayClientMessage(Component.translatable("message.isaac_disaster.debug_stick.pedestal.save"), true);
                PedestalBlockEntity.linkPedestals(pos, pos, serverLevel); // 连接自己(设定为非decoration)

            }else{
                PedestalBlockEntity.linkPedestals(DebugStick.loadBlockPos(held), pos, serverLevel);
                player.displayClientMessage(Component.translatable("message.isaac_disaster.debug_stick.pedestal.link"), true);
            }
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                    ModSounds.BATTERY_SMALL.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            player.setItemInHand(hand, held); // refresh
        }

        else if (pedestal.isLocked()){
            PlayerHelper.unlockBlock(player, hand, pos, 2, pedestal::unlockAll);
        }

        else if (pedestal.isDecoration() || player.isCreative()){
            // 如果是装饰性的，则交换物品
            if (held.isEmpty() && !stored.isEmpty()){
                player.setItemInHand(hand, stored);
                pedestal.clearContents();
                level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7f, 1.0f);

            }else if (stored.isEmpty()){
                held.setCount(1);
                pedestal.setItem(held);
                if (!player.isCreative()){
                    player.getItemInHand(hand).shrink(1);
                }
            }
        }
        else if (held.isEmpty() && !stored.isEmpty()){
            // 如果不是装饰性、且空手，则取下物品；并删除linked pedestal上的物品
            player.setItemInHand(hand, stored);
            pedestal.clearContents();
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.7f, 1.0f);

            Set<BlockPos> linkedPedestals = pedestal.getLinkedPedestals();
            for (BlockPos linkedPos : linkedPedestals){
                BlockEntity be1 = level.getBlockEntity(linkedPos);

                if (be1 instanceof PedestalBlockEntity linkedPedestal) {
                    linkedPedestal.setDecoration(true);
                    linkedPedestal.clearLinkedPedestals();

                    if (linkedPedestal == pedestal) continue; // 排除自己
                    if (linkedPedestal.getItem().isEmpty()) continue; // 空则无需更新
                    // 清空&更新linkedPedestal
                    linkedPedestal.clearContents();

                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            linkedPos.getX()+0.5, linkedPos.getY()+0.5, linkedPos.getZ()+0.5, 10,
                            0, 0.2, 0, 0.05);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    // 方块被破坏时掉落
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (!level.isClientSide) {
            PedestalData manager = PedestalData.get((ServerLevel) level);
            manager.removePedestal(pos);
        }

        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PedestalBlockEntity pedestal) {
                pedestal.drops();
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.PEDESTAL_BLOCK_ENTITY.get(),
                PedestalBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof PedestalBlockEntity pedestal && stack.hasTag()) {
                pedestal.load(stack.getTag().getCompound("BlockEntityTag"));
            }
    }


}

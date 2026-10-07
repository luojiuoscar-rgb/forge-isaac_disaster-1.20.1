package net.luojiuoscar.isaac_disaster.block.custom;

import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.block.block_entity.PedestalBlockEntity;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.custom.DebugStick;
import net.luojiuoscar.isaac_disaster.manager.data.BlockData;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PedestalBlock extends BaseEntityBlock implements ItemDisplayContainerBlock {

    public PedestalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.box(0.0625, 0.0, 0.0625, 0.9375, 0.3125, 0.9375);
    }

    // 玩家右键交互
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ServerLevel serverLevel = (ServerLevel) level;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PedestalBlockEntity pedestal)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof DebugStick) {
            return handleDebugStick(serverLevel, pos, player, held);
        }
        if (pedestal.isLocked()) {
            return handleLockedInteraction(player, hand, pos, pedestal);
        }

        return pedestal.interact(player, hand);
    }

    private InteractionResult handleDebugStick(ServerLevel serverLevel, BlockPos pos,
                                               Player player, ItemStack held) {
        if (!DebugStick.hasStoredPos(held)) {
            DebugStick.saveBlockPos(held, pos);
            player.displayClientMessage(Component.translatable(
                    "message.isaac_disaster.debug_stick.pedestal.save"), true);
            PedestalBlockEntity.linkPedestals(pos, pos, serverLevel);
        } else {
            PedestalBlockEntity.linkPedestals(DebugStick.loadBlockPos(held), pos, serverLevel);
            player.displayClientMessage(Component.translatable(
                    "message.isaac_disaster.debug_stick.pedestal.link"), true);
        }
        serverLevel.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                ModSounds.BATTERY_SMALL.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleLockedInteraction(Player player, InteractionHand hand,
                                                       BlockPos pos, PedestalBlockEntity pedestal) {
        PlayerHelper.unlockBlock(player, hand, pos, 2, pedestal::unlockAll);
        return InteractionResult.SUCCESS;
    }

    // 方块被破坏时掉落
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && !level.isClientSide) {
            BlockData manager = BlockData.get((ServerLevel) level);
            PedestalBlockEntity.removeFromAllLinks(pos, (ServerLevel) level);
            manager.removePedestal(pos);
            manager.removeItemBlock(pos);
            manager.takePendingPedestalClear(pos);
            manager.takePendingPedestalUpdate(pos);
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

    // ====== facing ======
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}

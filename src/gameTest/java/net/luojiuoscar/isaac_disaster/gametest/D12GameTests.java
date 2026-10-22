package net.luojiuoscar.isaac_disaster.gametest;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.D12;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("isaac_disaster_d12")
@PrefixGameTestTemplate(false)
public final class D12GameTests {
    @GameTest(template = "d12_empty", batch = "d12")
    public static void rerollPreservesAirFluidsWaterloggedBlocksAndExcludedTags(GameTestHelper helper) {
        var level = helper.getLevel();
        var source = EntityType.COW.create(level);
        helper.assertTrue(source != null, "Non-player source exists");
        BlockPos target = helper.absolutePos(new BlockPos(9, 3, 3));
        BlockPos[] positions = {
                target, target.offset(-2, 0, 0), target.offset(2, 0, 0),
                target.offset(0, 0, 2), target.offset(0, 0, -2)
        };
        BlockState[] states = {
                Blocks.AIR.defaultBlockState(), Blocks.WATER.defaultBlockState(), Blocks.LAVA.defaultBlockState(),
                Blocks.OAK_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true),
                Blocks.END_PORTAL.defaultBlockState()
        };
        for (BlockPos pos : BlockPos.betweenClosed(target.offset(-2, -2, -2), target.offset(2, 2, 2))) {
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        for (int i = 0; i < positions.length; i++) {
            level.setBlock(positions[i], states[i], Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        var context = new ExecutableEffectContext(source);
        context.set(ContextKeys.TARGET_POSITION, Vec3.atCenterOf(target));

        helper.assertTrue(new D12().applyEffect(context), "D12 rerolls the target cube");

        for (int i = 0; i < positions.length; i++) {
            helper.assertTrue(level.getBlockState(positions[i]).equals(states[i]),
                    "D12 preserves excluded state " + states[i]);
        }
        helper.succeed();
    }

    @GameTest(template = "d12_empty", batch = "d12")
    public static void nonPlayerRerollsAtContextPositionWithoutTouchingItsOwnPosition(GameTestHelper helper) {
        var level = helper.getLevel();
        var source = EntityType.COW.create(level);
        helper.assertTrue(source != null, "Non-player source exists");
        BlockPos sourcePos = helper.absolutePos(new BlockPos(2, 3, 3));
        BlockPos target = helper.absolutePos(new BlockPos(9, 3, 3));
        source.setPos(Vec3.atCenterOf(sourcePos));
        level.setBlock(sourcePos, Blocks.DIAMOND_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        for (BlockPos pos : BlockPos.betweenClosed(target.offset(-2, -2, -2), target.offset(2, 2, 2))) {
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        var context = new ExecutableEffectContext(source);
        context.set(ContextKeys.TARGET_POSITION, Vec3.atCenterOf(target));

        helper.assertTrue(new D12().applyEffect(context), "D12 accepts a non-player source");

        int changed = 0;
        for (BlockPos pos : BlockPos.betweenClosed(target.offset(-2, -2, -2), target.offset(2, 2, 2))) {
            if (!level.getBlockState(pos).is(Blocks.STONE)) changed++;
        }
        helper.assertTrue(changed > 0, "The cube at the context position is rerolled");
        helper.assertTrue(level.getBlockState(sourcePos).is(Blocks.DIAMOND_BLOCK),
                "The source position is not used as the reroll center");
        helper.succeed();
    }

    @GameTest(template = "d12_empty", batch = "d12")
    public static void missingContextPositionFailsWithoutRerollingAroundTheEntity(GameTestHelper helper) {
        var level = helper.getLevel();
        var source = EntityType.COW.create(level);
        helper.assertTrue(source != null, "Non-player source exists");
        BlockPos center = helper.absolutePos(new BlockPos(3, 3, 3));
        source.setPos(Vec3.atCenterOf(center));
        level.setBlock(center, Blocks.DIAMOND_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);

        helper.assertTrue(!new D12().applyEffect(new ExecutableEffectContext(source)),
                "D12 requires a context position");
        helper.assertTrue(level.getBlockState(center).is(Blocks.DIAMOND_BLOCK),
                "Missing context position does not fall back to the source position");
        helper.succeed();
    }
}

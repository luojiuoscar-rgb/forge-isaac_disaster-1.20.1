package net.luojiuoscar.isaac_disaster.gametest;

import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider.SpiderTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("isaac_disaster_blue_spider")
@PrefixGameTestTemplate(false)
public final class SpiderTerrainGameTests {
    @GameTest(template = "trajectory_empty", batch = "spider_navigation")
    public static void shapeTopsIncludeSlabsAndBothStairTreads(GameTestHelper helper) {
        Entity spider = body(helper);
        helper.setBlock(new BlockPos(0, 0, 1), Blocks.STONE);
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE_SLAB);
        helper.setBlock(new BlockPos(2, 0, 1), Blocks.STONE_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST));
        assertGround(helper, spider, new Vec3(0.5, 1.2, 1.5), 1);
        assertGround(helper, spider, new Vec3(1.5, 1, 1.5), 0.5);
        assertGround(helper, spider, new Vec3(2.25, 0.6, 1.5), 0.5);
        assertGround(helper, spider, new Vec3(2.75, 0.6, 1.5), 1);
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "spider_navigation")
    public static void groundPlacementRejectsBuriedSurfaceAndUnsupportedAir(GameTestHelper helper) {
        Entity spider = body(helper);
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.STONE);
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE);
        assertGround(helper, spider, new Vec3(1.5, 0.6, 1.5), 2);
        helper.assertTrue(SpiderTerrain.groundPosition(spider,
                helper.absoluteVec(new Vec3(1.5, 8, 1.5)), 1) == null,
                "Spawn placement cannot invent ground in unsupported air");
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "spider_navigation")
    public static void blockSweepsRejectDiagonalCornerWithClearEndpoints(GameTestHelper helper) {
        Entity spider = body(helper);
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.STONE);
        Vec3 from = helper.absoluteVec(new Vec3(0.8, 1, 1.5));
        Vec3 to = helper.absoluteVec(new Vec3(1.5, 1, 0.8));
        helper.assertTrue(SpiderTerrain.clearSweep(spider, from, from) && SpiderTerrain.clearSweep(spider, to, to),
                "Both sides of the corner are empty");
        helper.assertTrue(!SpiderTerrain.clearSweep(spider, from, to),
                "The body cannot cut diagonally through a block corner");
        helper.succeed();
    }

    private static void assertGround(GameTestHelper helper, Entity spider, Vec3 near, double height) {
        Vec3 absolute = helper.absoluteVec(near);
        Vec3 ground = SpiderTerrain.groundPosition(spider, absolute, 2);
        double expectedY = helper.absoluteVec(new Vec3(0, height, 0)).y;
        helper.assertTrue(ground != null && Math.abs(ground.y - expectedY) < 1e-6
                        && SpiderTerrain.clearSweep(spider, ground, ground),
                "Ground search returns the nearest clear actual collision top at height " + height);
    }

    private static Entity body(GameTestHelper helper) {
        Entity spider = ModEntities.BLUE_SPIDER.get().create(helper.getLevel());
        helper.assertTrue(spider != null, "Spider entity type creates a body");
        return spider;
    }
}

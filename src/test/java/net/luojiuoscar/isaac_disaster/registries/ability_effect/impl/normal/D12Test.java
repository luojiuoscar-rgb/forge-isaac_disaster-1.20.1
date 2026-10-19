package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class D12Test {
    @Test
    void cubePositionsAreCenteredOnTheFeetBlockAndContain125Positions() {
        BlockPos center = new BlockPos(10, 20, 30);

        var positions = D12.cubePositions(center);

        assertEquals(125, positions.size());
        assertTrue(positions.contains(center));
        assertEquals(125, new HashSet<>(positions).size());
        assertTrue(positions.contains(new BlockPos(8, 18, 28)));
        assertTrue(positions.contains(new BlockPos(12, 22, 32)));
    }

    @Test
    void airAndFluidStatesAreExcludedButOrdinaryBlocksAreCandidates() {
        assertTrue(D12.isExcluded(Blocks.AIR.defaultBlockState()));
        assertTrue(D12.isExcluded(Blocks.WATER.defaultBlockState()));
        assertTrue(D12.isExcluded(Blocks.LAVA.defaultBlockState()));
        assertFalse(D12.isExcluded(Blocks.STONE.defaultBlockState()));
    }
}

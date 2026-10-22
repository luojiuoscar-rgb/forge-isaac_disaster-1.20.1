package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.minecraft.core.BlockPos;
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
}

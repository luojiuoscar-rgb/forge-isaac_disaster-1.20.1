package net.luojiuoscar.isaac_disaster.bullet;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VoxelDdaTest {
    @Test
    void visitsEveryVoxelAlongLongSegment() {
        List<String> visited = new ArrayList<>();
        VoxelDda.traverse(new Vec3(0.1, 0.1, 0.1), new Vec3(3.9, 0.1, 0.1), (x, y, z, t) -> { visited.add(x + ":" + y + ":" + z); return false; });
        assertTrue(visited.contains("0:0:0"));
        assertTrue(visited.contains("1:0:0"));
        assertTrue(visited.contains("2:0:0"));
        assertTrue(visited.contains("3:0:0"));
    }
}

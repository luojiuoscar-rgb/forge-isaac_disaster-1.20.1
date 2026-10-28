package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.core;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GroundEffectPlacementTest {
    private final Vec3 start = new Vec3(0, 1.002, 0.5);
    private final Vec3 end = new Vec3(0.1, 1.002, 0.5);

    @Test
    void adjacentTopFacesCoverBothMovementDirections() {
        var faces = List.of(new AABB(-1, 0, 0, 0.05, 1, 1), new AABB(0.05, 0, 0, 1, 1, 1));
        assertTrue(GroundEffectPlacement.coversHorizontalPath(start, end, faces));
        assertTrue(GroundEffectPlacement.coversHorizontalPath(end, start, faces));
    }

    @Test
    void narrowGapIsRejectedEvenWhenBothEndpointsHaveSupport() {
        var faces = List.of(new AABB(-1, 0, 0, 0.049, 1, 1), new AABB(0.051, 0, 0, 1, 1, 1));
        assertFalse(GroundEffectPlacement.coversHorizontalPath(start, end, faces));
    }

    @Test
    void lowerFloorsHigherFacesAndVerticalMovementDoNotProvideHorizontalSupport() {
        assertFalse(GroundEffectPlacement.coversHorizontalPath(start, end, List.of(new AABB(-1, -1, 0, 1, 0, 1))));
        assertFalse(GroundEffectPlacement.coversHorizontalPath(start, end, List.of(new AABB(-1, 0, 0, 1, 2, 1))));
        assertFalse(GroundEffectPlacement.coversHorizontalPath(start, end.add(0, -1, 0), List.of(new AABB(-1, 0, 0, 1, 1, 1))));
    }
}

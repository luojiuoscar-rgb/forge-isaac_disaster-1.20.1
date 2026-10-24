package net.luojiuoscar.isaac_disaster.groundeffect;

import net.luojiuoscar.isaac_disaster.groundeffect.server.GroundEffectManager;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectContactGeometry;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static net.luojiuoscar.isaac_disaster.groundeffect.GroundEffectTestFixtures.*;

import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;

class GroundEffectManagerTest {
    @Test
    void denseQueryUsesTheGridAndRetainsItsInclusiveRadiusBoundary() {
        var manager = new GroundEffectManager();
        var atCenter = water(manager, null, Vec3.ZERO, 0.1D, 100, 1.0F);
        var atBoundary = water(manager, null, new Vec3(0.5D, 0, 0), 0.1D, 100, 1.0F);
        water(manager, null, new Vec3(0.75D, 0, 0), 0.1D, 100, 1.0F);
        for (int i = 0; i < 10; i++) water(manager, null, new Vec3(10 + i * 2, 0, 0), 0.1D, 100, 1.0F);
        var visible = manager.queryVisible(Vec3.ZERO, 0.5D);
        assertEquals(2, visible.size());
        assertTrue(visible.contains(atCenter));
        assertTrue(visible.contains(atBoundary));
    }

    @Test
    void contactRejectsOtherFloorsAndAirborneFeetDespiteHorizontalOverlap() {
        var effect = water(new GroundEffectManager(), null, new Vec3(0, 64, 0));
        assertFalse(GroundEffectContactGeometry.touches(effect, feetAt(63)));
        assertFalse(GroundEffectContactGeometry.touches(effect, feetAt(65)));
        assertFalse(GroundEffectContactGeometry.touches(effect, feetAt(100)));
        assertTrue(GroundEffectContactGeometry.touches(effect, feetAt(64)));
    }

    @Test
    void contactIncludesBothPointTwoHeightBoundariesButRejectsBeyondThem() {
        var effect = water(new GroundEffectManager(), null, new Vec3(0, 64, 0));
        assertTrue(GroundEffectContactGeometry.touches(effect, feetAt(63.8D)));
        assertTrue(GroundEffectContactGeometry.touches(effect, feetAt(64.2D)));
        assertFalse(GroundEffectContactGeometry.touches(effect, feetAt(63.799D)));
        assertFalse(GroundEffectContactGeometry.touches(effect, feetAt(64.201D)));
    }

    @Test
    void partialOverlapKeepsBothCompleteContactAreas() {
        GroundEffectManager manager = new GroundEffectManager();
        var effect = water(manager, new UUID(1, 1), new Vec3(0, 64, 0), 0.5D, 100, 1.0F);
        water(manager, new UUID(2, 2), new Vec3(0.4D, 64, 0), 0.5D, 100, 1.0F);
        assertTrue(GroundEffectContactGeometry.touches(effect, feetAt(64).move(0.2D, 0, 0)));
        assertTrue(GroundEffectContactGeometry.touches(effect, feetAt(64).move(-0.3D, 0, 0)));
        assertFalse(GroundEffectContactGeometry.touches(effect, feetAt(64).move(-0.6D, 0, 0)));
    }

    @Test
    void visibleQueryUsesAnInclusiveThreeDimensionalCenterDistance() {
        GroundEffectManager manager = new GroundEffectManager();
        var boundary = water(manager, null, new Vec3(48, 0, 0));
        var verticalBoundary = water(manager, null, new Vec3(0, 48, 0));
        var outside = water(manager, null, new Vec3(48.01D, 1, 0));
        var diagonalOutside = water(manager, null, new Vec3(40, 40, 0));
        var visible = manager.queryVisible(Vec3.ZERO, 48);
        assertEquals(2, visible.size());
        assertTrue(visible.contains(boundary));
        assertTrue(visible.contains(verticalBoundary));
        assertFalse(visible.contains(outside));
        assertFalse(visible.contains(diagonalOutside));
    }

    @Test
    void visibleQueryFindsLargePuddlesOnceAndForgetsRemovedAndClearedEntries() {
        GroundEffectManager manager = new GroundEffectManager();
        var large = water(manager, null, new Vec3(47, 0, 0), 12, 100, 1.0F);
        var small = water(manager, null, new Vec3(-47, 0, 0));
        assertEquals(2, manager.queryVisible(Vec3.ZERO, 48).size());
        manager.remove(large);
        assertEquals(List.of(small), manager.queryVisible(Vec3.ZERO, 48));
        manager.clear();
        assertTrue(manager.queryVisible(Vec3.ZERO, 48).isEmpty());
    }

    private static AABB feetAt(double y) {
        return new AABB(-0.3D, y, -0.3D, 0.3D, y + 1.8D, 0.3D);
    }

    @Test
    void managerRejectsTheEffectAfterItsPerLevelCapacityIsReached() {
        GroundEffectManager manager = new GroundEffectManager();
        UUID owner = new UUID(1, 2);
        for (int i = 0; i < GroundEffectManager.MAX_ACTIVE_EFFECTS; i++) {
            assertNotNull(water(manager, owner, new Vec3(i * 2.0D, 0, 0), 0.5D, 100, 1.0F));
        }
        assertEquals(GroundEffectManager.MAX_ACTIVE_EFFECTS, manager.activeCount());
        assertNull(water(manager, owner, new Vec3(40_002, 0, 0), 0.5D, 100, 1.0F));
        var first = manager.activeEffects().get(0);
        assertNull(water(manager, new UUID(7, 8), new Vec3(0.1D, 0, 0), 0.5D, 100, 1.0F));
        assertTrue(first.contains(new Vec3(0.2D, 0, 0)), "Rejected paint cannot erase old coverage");
        assertSame(first, water(manager, owner, Vec3.ZERO, 0.5D, 100, 1.0F), "Refresh is allowed at capacity");
        assertNotNull(water(manager, new UUID(7, 8), Vec3.ZERO, 1.0D, 100, 1.0F),
                "Replacement may reclaim the completely covered slot at capacity");
        assertEquals(GroundEffectManager.MAX_ACTIVE_EFFECTS, manager.activeCount());
    }

    @Test
    void removingAnEffectReleasesItsSlotForAReplacementGeneration() {
        GroundEffectManager manager = new GroundEffectManager();
        var first = water(manager, null, Vec3.ZERO);
        assertNotNull(first);
        manager.remove(first);
        var replacement = water(manager, null, new Vec3(1, 1, 1));
        assertNotNull(replacement);
        assertEquals(first.slot(), replacement.slot());
        assertEquals(first.generation() + 1, replacement.generation());
    }

    @Test
    void completelyCoveredWaterIsRemovedRatherThanKeptUntilExpiry() {
        GroundEffectManager manager = new GroundEffectManager();
        var old = water(manager, new UUID(1, 1), Vec3.ZERO, 0.5D, 100, 1.0F);
        var newer = water(manager, new UUID(2, 2), Vec3.ZERO, 1.0D, 20, 1.0F);

        assertNotNull(newer);
        assertEquals(1, manager.activeCount());
        assertSame(newer, manager.activeEffects().get(0));
        assertTrue(manager.drainDespawns().contains(old.identity()));
    }

    @Test
    void partialOverlapDoesNotEraseAnyPartOfEitherPuddle() {
        GroundEffectManager manager = new GroundEffectManager();
        var old = water(manager, new UUID(1, 1), Vec3.ZERO, 0.5D, 100, 1.0F);
        var newer = water(manager, new UUID(2, 2), new Vec3(0.4D, 0, 0), 0.5D, 20, 1.0F);
        assertTrue(old.contains(new Vec3(-0.3D, 0, 0)));
        assertTrue(old.contains(new Vec3(0.2D, 0, 0)));
        manager.remove(newer);

        assertEquals(1, manager.activeCount());
        assertTrue(old.contains(new Vec3(-0.3D, 0, 0)));
        assertTrue(old.contains(new Vec3(0.2D, 0, 0)));
    }

    @Test
    void sameOwnerRepaintingTheSameCircleRefreshesWithoutMovingOrAllocating() {
        GroundEffectManager manager = new GroundEffectManager();
        UUID owner = new UUID(1, 1);
        var old = water(manager, owner, Vec3.ZERO, 0.5D, 100, 1.0F);
        for (int i = 0; i < 50; i++) old.tick();
        var repainted = water(manager, owner, Vec3.ZERO, 0.5D, 100, 1.0F);

        assertSame(old, repainted);
        assertEquals(1, manager.activeCount());
        assertEquals(0, repainted.age());
        assertEquals(Vec3.ZERO, repainted.position());
    }

    @Test
    void separateFloorsDoNotEraseOneAnother() {
        GroundEffectManager manager = new GroundEffectManager();
        var old = water(manager, new UUID(1, 1), Vec3.ZERO, 0.5D, 100, 1.0F);
        water(manager, new UUID(2, 2), new Vec3(0, 1, 0), 2.0D, 20, 1.0F);

        assertEquals(2, manager.activeCount());
        assertTrue(old.contains(Vec3.ZERO));
    }

    @Test
    void partiallyOverlappingOwnersRetainTheirOwnContactAreas() {
        GroundEffectManager manager = new GroundEffectManager();
        var old = water(manager, new UUID(1, 1), Vec3.ZERO, 0.5D, 100, 1.0F);
        var newer = water(manager, new UUID(2, 2), new Vec3(0.1D, 0, 0), 0.5D, 100, 1.0F);
        assertTrue(old.contains(new Vec3(0.1D, 0, 0)));
        assertTrue(newer.contains(new Vec3(0.1D, 0, 0)));
        assertEquals(new UUID(2, 2), newer.ownerUuid());
    }

    @Test
    void movingSlowlyLeavesOldUncoveredPartsBehindInsteadOfMovingOneCircle() {
        GroundEffectManager manager = new GroundEffectManager();
        UUID owner = new UUID(1, 1);
        var first = water(manager, owner, Vec3.ZERO, 0.5D, 100, 1.0F);
        for (int i = 1; i <= 15; i++) water(manager, owner, new Vec3(i * 0.05D, 0, 0), 0.5D, 100, 1.0F);
        assertTrue(manager.activeCount() > 1);
        assertTrue(first.contains(new Vec3(-0.4D, 0, 0)));
        assertTrue(manager.activeEffects().stream().anyMatch(effect -> effect.contains(new Vec3(0.9D, 0, 0))));
        assertEquals(Vec3.ZERO, first.position());
    }

    @Test
    void repaintCanEraseSmallerInterveningPaintWithoutChangingItsOutline() {
        GroundEffectManager manager = new GroundEffectManager();
        UUID owner = new UUID(1, 1);
        var original = water(manager, owner, Vec3.ZERO, 1.0D, 100, 1.0F);
        var covering = water(manager, new UUID(2, 2), Vec3.ZERO, 0.25D, 100, 1.0F);
        assertTrue(original.contains(Vec3.ZERO));
        long originalOrder = original.creationOrder();
        assertSame(original, water(manager, owner, Vec3.ZERO, 1.0D, 100, 1.0F));
        assertTrue(original.contains(Vec3.ZERO));
        assertTrue(original.creationOrder() > originalOrder);
        assertEquals(1, manager.activeCount());
        assertTrue(manager.drainDespawns().contains(covering.identity()));
    }

    @Test
    void zeroLifetimePaintCannotEraseAnExistingEffect() {
        GroundEffectManager manager = new GroundEffectManager();
        var original = water(manager, null, Vec3.ZERO);
        assertNull(water(manager, new UUID(1, 1), Vec3.ZERO, 1.0D, 0, 1.0F));
        assertTrue(original.contains(Vec3.ZERO));
        assertEquals(1, manager.activeCount());
    }

    @Test
    void excessiveGridCoverageIsRejectedBeforeEnumeratingCells() {
        var manager = new GroundEffectManager();
        assertNull(water(manager, null, Vec3.ZERO, 399996, 100, 1));
        assertNull(water(manager, null, new Vec3(Double.MAX_VALUE, 0, 0), 0.5, 100, 1));
        assertEquals(0, manager.activeCount());
        assertNotNull(water(manager, null, Vec3.ZERO));
        assertEquals(1, manager.queryVisible(Vec3.ZERO, Double.MAX_VALUE).size());
    }

    @Test
    void gridBudgetAcceptsItsBoundaryAndRefusesTheNextCellBand() {
        var manager = new GroundEffectManager();
        var boundary = water(manager, null, Vec3.ZERO, 63.5, 100, 1);
        assertNotNull(boundary);
        assertNull(water(manager, null, Vec3.ZERO, 64, 100, 1));
        assertSame(boundary, manager.activeEffects().get(0));
    }
}

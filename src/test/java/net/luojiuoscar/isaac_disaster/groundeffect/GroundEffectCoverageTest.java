package net.luojiuoscar.isaac_disaster.groundeffect;

import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectCoverage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroundEffectCoverageTest {
    @Test
    void admissionByteBudgetMatchesActualUncutAndClippedEncoding() {
        var original = GroundEffectCoverage.puddle(1, 2);
        var clipped = original.subtract(GroundEffectCoverage.puddle(0.25, 3), 0, 0);
        for (var coverage : new GroundEffectCoverage[]{original, clipped}) {
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                coverage.write(buffer);
                assertEquals(buffer.readableBytes(), coverage.serializedBytes());
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void malformedCountsAreRejectedBeforeAllocatingFootprintCollections() {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeBoolean(false);
            buffer.writeVarInt(Integer.MAX_VALUE);
            assertThrows(IllegalArgumentException.class, () -> GroundEffectCoverage.read(buffer, 1, 2));
            buffer.clear();
            buffer.writeBoolean(false);
            buffer.writeVarInt(1);
            buffer.writeVarInt(Integer.MAX_VALUE);
            buffer.writeZero(48);
            assertThrows(IllegalArgumentException.class, () -> GroundEffectCoverage.read(buffer, 1, 2));
        } finally {
            buffer.release();
        }
    }

    @Test
    void aSmallBrushCutsAHoleWithoutErasingTheOuterRing() {
        var original = GroundEffectCoverage.puddle(1.0D, 10);
        var remainder = original.subtract(GroundEffectCoverage.puddle(0.25D, 11), 0, 0);

        assertFalse(remainder.contains(0, 0));
        assertTrue(remainder.contains(0.7D, 0));
        assertTrue(remainder.contains(-0.7D, 0));
        assertTrue(original.contains(0, 0), "Snapshots remain immutable after another state is clipped");
    }

    @Test
    void severalPartialBrushesCanCompletelyEraseAnOldCircle() {
        var original = GroundEffectCoverage.puddle(0.5D, 1);
        var left = original.subtract(GroundEffectCoverage.puddle(1.0D, 2), -0.65D, 0);
        assertFalse(left.isEmpty());
        var right = left.subtract(GroundEffectCoverage.puddle(1.0D, 3), 0.65D, 0);
        assertTrue(right.isEmpty());
    }

    @Test
    void nearbyDisjointBrushDoesNotFragmentOrChangeAnExistingFootprint() {
        var original = GroundEffectCoverage.puddle(0.5D, 1);
        assertSame(original, original.subtract(GroundEffectCoverage.puddle(0.5D, 2), 1.01D, 0));
    }

    @Test
    void clippingMatchesSetDifferenceAcrossAnAreaWithMultipleBrushes() {
        var original = GroundEffectCoverage.puddle(1.0D, 1);
        var first = GroundEffectCoverage.puddle(0.6D, 2);
        var second = GroundEffectCoverage.puddle(0.4D, 3);
        var remainder = original.subtract(first, 0.4D, 0.1D).subtract(second, -0.3D, -0.2D);
        // Offset sample positions avoid shared polygon boundaries; this oracle uses original footprints.
        for (double x = -1.023D; x < 1.0D; x += 0.037D) {
            for (double z = -1.017D; z < 1.0D; z += 0.041D) {
                boolean expected = original.contains(x, z)
                        && !first.contains(x - 0.4D, z - 0.1D)
                        && !second.contains(x + 0.3D, z + 0.2D);
                assertEquals(expected, remainder.contains(x, z), "Coverage at " + x + ", " + z);
            }
        }
    }

    @Test
    void everyGeneratedOutlineIsConvexAndClosesAtTheSameSeededVertex() {
        for (int seed = 0; seed < 256; seed++) {
            var points = GroundEffectCoverage.puddle(0.5D, seed).pieces().get(0).vertices();
            for (int i = 0; i < points.size(); i++) {
                var a = points.get(i);
                var b = points.get((i + 1) % points.size());
                var c = points.get((i + 2) % points.size());
                double turn = (b.x() - a.x()) * (c.z() - b.z())
                        - (b.z() - a.z()) * (c.x() - b.x());
                assertTrue(turn > 0, "Convex clipping requires a counterclockwise convex brush");
            }
        }
    }
}

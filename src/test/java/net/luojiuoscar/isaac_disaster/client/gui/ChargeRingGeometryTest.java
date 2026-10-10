package net.luojiuoscar.isaac_disaster.client.gui;

import java.util.List;

import net.luojiuoscar.isaac_disaster.client.gui.charge_bar.ChargeRingGeometry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChargeRingGeometryTest {
    @Test
    void leavesTheCenterAndCornersTransparentAtEveryChargeLevel() {
        for (float progress : new float[] { 0f, 0.25f, 0.5f, 0.75f, 1f }) {
            var pixels = ChargeRingGeometry.rasterize(progress);
            assertFalse(pixels.isEmpty());
            assertTrue(pixels.stream().noneMatch(pixel -> pixel.x() >= 4 && pixel.x() <= 7
                    && pixel.y() >= 4 && pixel.y() <= 7));
            assertTrue(pixels.stream().noneMatch(pixel -> pixel.x() == 0 && pixel.y() == 0));
            assertTrue(pixels.stream().anyMatch(pixel -> pixel.part() == ChargeRingGeometry.Part.OUTLINE));
        }
    }

    @Test
    void chargesClockwiseByAngleInsteadOfFillingAHalfRectangle() {
        var quarter = ChargeRingGeometry.rasterize(0.25f);
        assertEquals(ChargeRingGeometry.Part.FILLED, at(quarter, 9, 3));
        assertEquals(ChargeRingGeometry.Part.TRACK, at(quarter, 9, 8));
        assertEquals(ChargeRingGeometry.Part.TRACK, at(quarter, 2, 3));
        var half = ChargeRingGeometry.rasterize(0.5f);
        assertEquals(ChargeRingGeometry.Part.FILLED, at(half, 9, 8));
        assertEquals(ChargeRingGeometry.Part.TRACK, at(half, 2, 8));
        var threeQuarters = ChargeRingGeometry.rasterize(0.75f);
        assertEquals(ChargeRingGeometry.Part.FILLED, at(threeQuarters, 2, 8));
        assertEquals(ChargeRingGeometry.Part.TRACK, at(threeQuarters, 2, 3));
    }

    @Test
    void hasAnEmptyTrackAtZeroAndACompleteRingAtFullCharge() {
        var empty = ChargeRingGeometry.rasterize(0);
        var full = ChargeRingGeometry.rasterize(1);
        assertTrue(empty.stream().noneMatch(pixel -> pixel.part() == ChargeRingGeometry.Part.FILLED));
        assertTrue(full.stream().noneMatch(pixel -> pixel.part() == ChargeRingGeometry.Part.TRACK));
        assertEquals(empty.size(), full.size());
        long previous = -1;
        for (int step = 0; step <= 20; step++) {
            long filled = ChargeRingGeometry.rasterize(step / 20f).stream()
                    .filter(pixel -> pixel.part() == ChargeRingGeometry.Part.FILLED).count();
            assertTrue(filled >= previous);
            previous = filled;
        }
        assertEquals(empty, ChargeRingGeometry.rasterize(-1));
        assertEquals(empty, ChargeRingGeometry.rasterize(Float.NaN));
        assertEquals(full, ChargeRingGeometry.rasterize(2));
    }

    private ChargeRingGeometry.Part at(List<ChargeRingGeometry.Pixel> pixels, int x, int y) {
        return pixels.stream().filter(pixel -> pixel.x() == x && pixel.y() == y)
                .findFirst().orElseThrow().part();
    }

    @Test
    void approachingPixelRingHidesAtTheBoundariesAndUsesFineRadiusSteps() {
        assertTrue(ChargeRingGeometry.rasterizeApproach(0f).isEmpty());
        assertTrue(ChargeRingGeometry.rasterizeApproach(1f).isEmpty());
        assertTrue(ChargeRingGeometry.rasterizeApproach(Float.NaN).isEmpty());
        var shapes = new java.util.HashSet<List<ChargeRingGeometry.Span>>();
        for (int step = 1; step < 64; step++) {
            var spans = ChargeRingGeometry.rasterizeApproach(step / 64f);
            assertFalse(spans.isEmpty());
            assertTrue(spans.stream().allMatch(span -> span.width() > 0));
            shapes.add(spans);
        }
        assertTrue(shapes.size() > 10, "Subpixel radius steps must avoid only four large jumps");
        double early = averageRadius(ChargeRingGeometry.rasterizeApproach(0.01f));
        double late = averageRadius(ChargeRingGeometry.rasterizeApproach(0.99f));
        assertTrue(early > late + 3);
    }

    private double averageRadius(List<ChargeRingGeometry.Span> spans) {
        double total = 0;
        int count = 0;
        for (var span : spans) {
            for (int x = span.x(); x < span.x() + span.width(); x++) {
                total += Math.hypot(x + 0.5, span.y() + 0.5);
                count++;
            }
        }
        return total / count;
    }
}

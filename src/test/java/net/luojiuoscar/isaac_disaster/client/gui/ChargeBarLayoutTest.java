package net.luojiuoscar.isaac_disaster.client.gui;

import java.util.List;

import net.luojiuoscar.isaac_disaster.client.gui.charge_bar.ChargeBarLayout;
import net.luojiuoscar.isaac_disaster.client.gui.charge_bar.ChargeRingGeometry;
import net.luojiuoscar.isaac_disaster.registries.charge_bar.ChargeBarType;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChargeBarLayoutTest {
    @Test
    void invalidSlotIsSkippedWithoutBreakingLaterPositions() {
        assertNull(assertDoesNotThrow(() -> ChargeBarLayout.position(-1)));
        assertNotNull(ChargeBarLayout.position(0));
    }

    @Test
    void missingBaseTextureIsSkippedWhileValidEntriesKeepTheirOrder() {
        var invalid = assertDoesNotThrow(() -> new ChargeBarType(Integer.MAX_VALUE, null, 0xFF7DE000));
        var bad = new ChargeBarLayout.Entry(ResourceLocation.fromNamespaceAndPath("test", "bad"), invalid, 1f);
        var good = entry("good", 10);
        assertEquals(List.of(good), assertDoesNotThrow(() -> ChargeBarLayout.sorted(List.of(bad, good))));
    }

    @Test
    void smallRingsStayCloseToTheCrosshair() {
        var first = ChargeBarLayout.position(0);
        double radius = Math.hypot(first.x(), first.y());
        assertTrue(radius >= 16 && radius <= 20);
        assertTrue(first.x() <= 14 && -first.y() <= 14);
    }

    @Test
    void startsAtUpperRightAndMovesClockwiseOnACircle() {
        var first = ChargeBarLayout.position(0);
        assertTrue(first.x() > 0);
        assertTrue(first.y() < 0);
        assertEquals(first.x(), -first.y(), 1.0E-8);
        var second = ChargeBarLayout.position(1);
        assertTrue(second.x() > first.x());
        assertEquals(0, second.y(), 1.0E-8);
        double radius = Math.hypot(first.x(), first.y());
        for (int slot = 0; slot < 8; slot++) {
            var point = ChargeBarLayout.position(slot);
            assertEquals(radius, Math.hypot(point.x(), point.y()), 1.0E-8);
        }
    }

    @Test
    void secondAndThirdRingsRestartAtUpperRightWithMoreSlots() {
        var inner = ChargeBarLayout.position(0);
        var outer = ChargeBarLayout.position(8);
        var third = ChargeBarLayout.position(24);
        assertEquals(inner.x() * 2, outer.x(), 1.0E-8);
        assertEquals(inner.y() * 2, outer.y(), 1.0E-8);
        assertEquals(inner.x() * 3, third.x(), 1.0E-8);
        assertEquals(outer.x(), -outer.y(), 1.0E-8);
        var right = ChargeBarLayout.position(10);
        assertEquals(0, right.y(), 1.0E-8);
    }

    @Test
    void neighboringIndicatorsDoNotOverlapEvenOnOuterRings() {
        double minimumDistance = ChargeRingGeometry.SIZE + ChargeBarLayout.GAP;
        int start = 0;
        for (int ring = 1; ring <= 12; ring++) {
            int capacity = ring * 8;
            for (int slot = 0; slot < capacity; slot++) {
                var a = ChargeBarLayout.position(start + slot);
                var b = ChargeBarLayout.position(start + (slot + 1) % capacity);
                assertTrue(Math.hypot(a.x() - b.x(), a.y() - b.y()) >= minimumDistance - 1.0E-8);
            }
            start += capacity;
        }
    }

    @Test
    void highestPriorityComesFirstAndEqualPrioritiesUseIds() {
        var high = entry("attack_charge", Integer.MAX_VALUE);
        var a = entry("a", 10);
        var b = entry("b", 10);
        assertEquals(List.of(high, a, b), ChargeBarLayout.sorted(List.of(b, a, high)));
        assertEquals(List.of(high, b), ChargeBarLayout.sorted(List.of(b, high)));
    }

    private ChargeBarLayout.Entry entry(String id, int priority) {
        var type = new ChargeBarType(priority,
                ResourceLocation.fromNamespaceAndPath("test", "textures/ring.png"), 0xFF7DE000);
        return new ChargeBarLayout.Entry(ResourceLocation.fromNamespaceAndPath("test", id), type, 0.5f);
    }
}

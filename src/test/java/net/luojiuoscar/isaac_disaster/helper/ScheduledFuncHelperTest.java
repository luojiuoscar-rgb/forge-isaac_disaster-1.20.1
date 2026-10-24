package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledFuncHelperTest {
    private final UUID owner = UUID.randomUUID();
    private final ResourceLocation type = ResourceLocation.fromNamespaceAndPath("isaac_disaster", "test_barrage");

    @BeforeEach
    void reset() {
        ScheduledFuncHelper.onServerStopping(null);
    }

    @AfterEach
    void cleanUp() {
        ScheduledFuncHelper.onServerStopping(null);
    }

    @Test
    void cancellingAnActiveTaskPreventsItsNextCallback() {
        AtomicInteger emitted = new AtomicInteger();
        ScheduledFuncHelper.scheduleForPlayer(owner, type, 1, 0, 10, false, emitted::incrementAndGet);
        ScheduledFuncHelper.tick(null);
        assertEquals(1, emitted.get());
        ScheduledFuncHelper.clearByType(type, owner);
        ScheduledFuncHelper.tick(null);
        assertEquals(1, emitted.get());
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
    }

    @Test
    void cancellingBeforeActivationAlsoRemovesThePlayerIndex() {
        AtomicInteger emitted = new AtomicInteger();
        ScheduledFuncHelper.scheduleForPlayer(owner, type, 1, 0, 10, false, emitted::incrementAndGet);
        ScheduledFuncHelper.clearByType(type, owner);
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
        ScheduledFuncHelper.tick(null);
        assertEquals(0, emitted.get());
    }

    @Test
    void overlappingSchedulesKeepIndependentCountsAndIntervals() {
        AtomicInteger first = new AtomicInteger();
        AtomicInteger second = new AtomicInteger();
        ScheduledFuncHelper.scheduleForPlayer(owner, type, 1, -1, 3, false, first::incrementAndGet);
        ScheduledFuncHelper.tick(null);
        assertEquals(0, first.get());
        ScheduledFuncHelper.tick(null);
        assertEquals(1, first.get());
        ScheduledFuncHelper.scheduleForPlayer(owner, type, 1, 0, 2, false, second::incrementAndGet);
        ScheduledFuncHelper.tick(null);
        assertEquals(2, first.get());
        assertEquals(1, second.get());
        ScheduledFuncHelper.tick(null);
        assertEquals(3, first.get());
        assertEquals(2, second.get());
        ScheduledFuncHelper.tick(null);
        assertEquals(3, first.get());
        assertEquals(2, second.get());
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
    }
}

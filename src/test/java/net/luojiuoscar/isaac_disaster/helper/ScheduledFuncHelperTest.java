package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledFuncHelperTest {
    private final ResourceLocation type = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "append_test");
    private final UUID owner = UUID.randomUUID();
    private final AtomicInteger now = new AtomicInteger(100);
    private final List<Integer> emissions = new ArrayList<>();

    @BeforeEach
    @AfterEach
    void reset() {
        ScheduledFuncHelper.onServerStopping(null);
    }

    private int append(UUID player, ResourceLocation taskType, int count, Runnable action) {
        return ScheduledFuncHelper.scheduleOrAppendForPlayer(player, taskType, 3, count, action);
    }

    private int append(int count) {
        return append(owner, type, count, () -> emissions.add(now.get()));
    }

    private void tick(int tick) {
        now.set(tick);
        ScheduledFuncHelper.tick(null);
    }

    @Test
    void threePendingUsesShareOneTaskAndStartAtTheNextSchedulerPass() throws Exception {
        int id = append(7);
        assertEquals(id, append(7));
        assertEquals(id, append(7));
        assertEquals(1, ScheduledFuncHelper.getPlayerTasks(owner).size());
        tick(100);
        assertEquals(List.of(100), emissions, "The next scheduler pass can run within the use tick");
        for (int tick = 101; tick <= 161; tick++) tick(tick);
        assertEquals(IntStream.range(0, 21).map(i -> 100 + 3 * i).boxed().toList(), emissions);
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
    }

    @Test
    void appendingToAnActiveTaskKeepsItsDeadlineAndOriginalCallback() throws Exception {
        int id = append(7);
        tick(101);
        tick(102);
        assertEquals(id, append(owner, type, 7, () -> fail("Appending must retain the original callback")));
        for (int tick = 103; tick <= 141; tick++) tick(tick);
        assertEquals(IntStream.range(0, 14).map(i -> 101 + 3 * i).boxed().toList(), emissions);
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
    }

    @Test
    void appendingFromTheFinalCallbackStillRunsEveryAddedExecution() throws Exception {
        append(owner, type, 1, () -> {
            emissions.add(now.get());
            if (emissions.size() == 1) append(7);
        });
        for (int tick = 101; tick <= 123; tick++) tick(tick);
        assertEquals(IntStream.range(0, 8).map(i -> 101 + 3 * i).boxed().toList(), emissions);
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
    }

    @Test
    void pendingCancellationCleansTheIndexAndDoesNotRemoveOtherTypes() throws Exception {
        int canceled = append(7);
        ScheduledFuncHelper.clearByType(type, owner);
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
        ResourceLocation otherType = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "other_append_test");
        List<Integer> otherEmissions = new ArrayList<>();
        append(owner, otherType, 1, () -> otherEmissions.add(now.get()));
        assertNotEquals(canceled, append(2));
        assertEquals(2, ScheduledFuncHelper.getPlayerTasks(owner).size());
        for (int tick = 100; tick <= 105; tick++) tick(tick);
        assertEquals(List.of(100, 103), emissions);
        assertEquals(List.of(100), otherEmissions);
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
    }

    @Test
    void cancelingAnActiveTaskBeforeItsDueTickCannotRunItsOldCallback() throws Exception {
        append(7);
        tick(100);
        tick(101);
        tick(102);
        now.set(103);
        ScheduledFuncHelper.clearByType(type, owner);
        List<Integer> replacement = new ArrayList<>();
        append(owner, type, 2, () -> replacement.add(now.get()));
        for (int tick = 103; tick <= 107; tick++) tick(tick);
        assertEquals(List.of(100), emissions, "A canceled due task cannot execute before deferred removal");
        assertEquals(List.of(103, 106), replacement);
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
    }

    @Test
    void ordinaryAndBatteryCountsStayIndependentAcrossPlayers() throws Exception {
        UUID other = UUID.randomUUID();
        List<Integer> otherEmissions = new ArrayList<>();
        append(7);
        append(other, type, 14, () -> otherEmissions.add(now.get()));
        for (int tick = 101; tick <= 141; tick++) tick(tick);
        assertEquals(7, emissions.size());
        assertEquals(14, otherEmissions.size());
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
        assertTrue(ScheduledFuncHelper.getPlayerTasks(other).isEmpty());
    }

    @Test
    void zeroAndNegativeCountsDoNotCreateTasksAndCompletedTasksCanRestart() throws Exception {
        assertEquals(0, append(0));
        assertEquals(0, append(-7));
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
        int first = append(1);
        tick(101);
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
        assertNotEquals(first, append(1));
        assertEquals(List.of(101), emissions);
        tick(102);
        assertEquals(List.of(101, 102), emissions);
    }

    @Test
    void legacyFiniteAndUnboundedSchedulesKeepTheirExistingCadence() throws Exception {
        ScheduledFuncHelper.scheduleForPlayer(owner, type, 3, 0, 2, false, () -> emissions.add(now.get()));
        ResourceLocation infinite = ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "legacy_infinite_test");
        List<Integer> unbounded = new ArrayList<>();
        int unboundedId = ScheduledFuncHelper.scheduleForPlayer(owner, infinite, 2, 0, -1, false, () -> unbounded.add(now.get()));
        assertEquals(unboundedId, append(owner, infinite, 7, () -> fail("An unbounded task keeps its callback")));
        for (int tick = 0; tick <= 7; tick++) tick(tick);
        assertEquals(List.of(2, 5), emissions);
        assertEquals(List.of(1, 3, 5, 7), unbounded);
        ScheduledFuncHelper.clearPlayerTasks(owner);
        tick(8);
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
    }

    @Test
    void nonpositiveIntervalsNormalizeToOneTick() {
        ScheduledFuncHelper.scheduleOrAppendForPlayer(owner, type, -3, 2, () -> emissions.add(now.get()));
        tick(100);
        assertEquals(List.of(100), emissions);
        tick(101);
        assertEquals(List.of(100, 101), emissions);
        assertTrue(ScheduledFuncHelper.getPlayerTasks(owner).isEmpty());
    }
}

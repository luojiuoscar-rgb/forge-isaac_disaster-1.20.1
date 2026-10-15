package net.luojiuoscar.isaac_disaster.registries.trigger_module;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TriggerModuleSnapshotCacheTest {
    @Test
    void unchangedInventoryReusesSnapshotAndMutationsPreserveOldSnapshots() {
        var queue = new TriggerModuleQueue();
        var id = ResourceLocation.parse("test:module");
        queue.replaceAll(List.of(new TriggerModuleInstance(id, 2)));
        var original = queue.snapshot();
        assertSame(original, queue.snapshot());
        queue.replaceAll(List.of(new TriggerModuleInstance(id, 4)));
        assertNotSame(original, queue.snapshot());
        assertEquals(2, original.getStacks(id));
        assertEquals(4, queue.snapshot().getStacks(id));
        var beforeNoop = queue.snapshot();
        queue.remove(ResourceLocation.parse("test:missing"));
        assertSame(beforeNoop, queue.snapshot());
        queue.remove(id);
        assertTrue(queue.snapshot().isEmpty());
        assertEquals(2, original.getStacks(id));
    }

    @Test
    void copyAndClearDoNotInvalidateAnotherQueuesSnapshot() {
        var source = new TriggerModuleQueue();
        source.replaceAll(List.of(new TriggerModuleInstance(ResourceLocation.parse("test:module"), 2)));
        var original = source.snapshot();
        var target = new TriggerModuleQueue();
        target.copyFrom(source);
        assertEquals(original.modules(), target.snapshot().modules());
        target.clear();
        assertTrue(target.snapshot().isEmpty());
        assertSame(original, source.snapshot());
    }
}

package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FamiliarAdmissionTest {
    private static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("test", "familiar");

    @Test
    void admissionBudgetBelongsToThePlayerAndRenewsOnTheNextTick() {
        PlayerFamiliarData first = data();
        PlayerFamiliarData second = data();
        assertTrue(first.beginAdmission(100));
        assertFalse(first.beginAdmission(100));
        assertTrue(second.beginAdmission(100));
        assertTrue(first.beginAdmission(101));
        assertFalse(first.beginAdmission(101));
    }

    @Test
    void recallsAreDeduplicatedOrderedAndCanceledWhenAnEntityIsUnlinked() {
        PlayerFamiliarData data = data();
        data.setCount(TYPE, 2);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        data.registerEntity(TYPE, first);
        data.registerEntity(TYPE, second);
        data.queueRecall(first);
        data.queueRecall(first);
        data.queueRecall(second);
        assertEquals(first, data.pollRecall());
        assertEquals(second, data.pollRecall());
        assertNull(data.pollRecall());
        data.queueRecall(first);
        data.queueRecall(second);
        data.removeEntity(TYPE, first);
        assertEquals(second, data.pollRecall());
        assertNull(data.pollRecall());
    }

    @Test
    void saveAndPlayerCopyRebuildQuotaAndResetRuntimeAdmissionState() {
        PlayerFamiliarData source = data();
        UUID old = UUID.randomUUID();
        source.setCount(TYPE, 2);
        source.registerEntity(TYPE, old);
        source.queueRecall(old);
        source.beginAdmission(100);
        PlayerFamiliarData restored = data();
        CompoundTag saved = new CompoundTag();
        source.saveNBTData(saved);
        restored.loadNBTData(saved);
        PlayerFamiliarData copied = data();
        copied.queueRecall(UUID.randomUUID());
        copied.beginAdmission(100);
        copied.copyFrom(source);
        for (PlayerFamiliarData data : new PlayerFamiliarData[]{restored, copied}) {
            assertEquals(2, data.getCount(TYPE));
            assertEquals(2, data.getEffectiveCount(TYPE));
            assertTrue(data.getEntry(TYPE).orElseThrow().hasNoEntities());
            assertEquals(TYPE, data.getNextIncompleteEntry().orElseThrow().getType());
            assertNull(data.pollRecall());
            assertTrue(data.beginAdmission(100));
        }
    }

    private static PlayerFamiliarData data() {
        FamiliarEntityType descriptor = new FamiliarEntityType(() -> null, 64);
        return new PlayerFamiliarData(type -> descriptor, List.of());
    }
}

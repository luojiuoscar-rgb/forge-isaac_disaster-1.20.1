package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarLimitGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FamiliarSourcesTest {
    private static final ResourceLocation PARENT = ResourceLocation.parse("test:parent");
    private static final ResourceLocation OTHER_BABY = ResourceLocation.parse("test:other_baby");
    private static final ResourceLocation FLY = ResourceLocation.parse("test:fly");
    private static final ResourceLocation A = ResourceLocation.parse("test:a");
    private static final ResourceLocation B = ResourceLocation.parse("test:b");

    private PlayerFamiliarData data() {
        var babies = new FamiliarLimitGroup(2);
        var flies = new FamiliarLimitGroup(3);
        var baby = new FamiliarEntityType(() -> null, 2, () -> babies, 0, true);
        var fly = new FamiliarEntityType(() -> null, 3, () -> flies, 0, false);
        Map<ResourceLocation, FamiliarEntityType> types = Map.of(PARENT, baby, OTHER_BABY, baby, FLY, fly);
        return new PlayerFamiliarData(types::get, List.of(babies, flies));
    }

    @Test
    void fullTypeRejectsIncomingDemandWithoutStealingAnOlderSource() {
        var data = data();
        data.setCount(FLY, A, 1);
        data.setCount(FLY, B, 2);
        data.addCount(FLY, A, 2);
        assertEquals(1, data.getCount(FLY, A));
        assertEquals(2, data.getCount(FLY, B));
    }

    @Test
    void removingParentsNeverRemovesTheirIndependentFlyContribution() {
        var data = data();
        data.setCount(PARENT, A, 100);
        data.setCount(FLY, B, 1);
        data.setCount(FLY, PARENT, 2);
        UUID fly = UUID.randomUUID();
        data.registerEntity(FLY, PARENT, fly);
        data.setCount(OTHER_BABY, A, 100);
        data.setCount(PARENT, A, 1);
        assertEquals(2, data.getCount(FLY, PARENT));
        data.setCount(PARENT, A, 0);
        assertEquals(2, data.getCount(FLY, PARENT));
        assertEquals(1, data.getCount(FLY, B));
        assertTrue(data.containsEntity(FLY, fly));
    }

    @Test
    void parentQuotaChangesNeverCreateOrRemoveFlyContributions() {
        var data = data();
        data.setCount(OTHER_BABY, A, 2);
        data.setCount(PARENT, A, 20);
        data.setCount(FLY, PARENT, 2);
        assertEquals(2, data.getCount(FLY));
        data.setCount(OTHER_BABY, A, 0);
        assertEquals(2, data.getEffectiveCount(PARENT));
        assertEquals(2, data.getCount(FLY));
    }

    @Test
    void consumingAnEntityReducesItsActualSourceBeforeAnyReplacement() {
        var data = data();
        data.setCount(FLY, A, 1);
        data.setCount(FLY, B, 2);
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        data.registerEntity(FLY, A, a);
        data.registerEntity(FLY, B, b);
        data.consumeEntity(FLY, b);
        assertEquals(1, data.getCount(FLY, A));
        assertEquals(1, data.getCount(FLY, B));
        assertEquals(1, data.getEntityCount(FLY, A));
        assertEquals(0, data.getEntityCount(FLY, B));
        assertFalse(data.containsEntity(FLY, b));
        data.consumeEntity(FLY, b);
        assertEquals(2, data.getCount(FLY));
    }

    @Test
    void totalPreservingSourceReplacementStillExposesMissingSource() {
        var data = data();
        data.setCount(FLY, A, 1);
        UUID old = UUID.randomUUID();
        data.registerEntity(FLY, A, old);
        data.setCount(FLY, A, 0);
        data.setCount(FLY, B, 1);
        var entry = data.getEntry(FLY).orElseThrow();
        assertEquals(A, entry.getEntitySource(old));
        assertEquals(0, entry.getCount(A));
        assertEquals(B, entry.getIncompleteSource().orElseThrow());
        data.removeEntity(FLY, old);
        assertEquals(FLY, data.getNextIncompleteEntry().orElseThrow().getType());
    }

    @Test
    void reservationRechecksTypeCapacityAndDoesNotLeavePartialContribution() {
        var data = data();
        data.setCount(PARENT, A, 1);
        UUID fly = UUID.randomUUID();
        try (var reservation = data.reserveSpawn(FLY, PARENT, fly)) {
            assertNotNull(reservation);
            data.setCount(FLY, B, 3);
            assertFalse(reservation.commit());
        }
        assertEquals(3, data.getCount(FLY, B));
        assertEquals(0, data.getCount(FLY, PARENT));
        assertFalse(data.containsEntity(FLY, fly));
    }

    @Test
    void reservationCommitsAndReleasesOnlyItsOwnSource() {
        var data = data();
        data.setCount(PARENT, A, 1);
        data.setCount(FLY, B, 1);
        UUID fly = UUID.randomUUID();
        try (var reservation = data.reserveSpawn(FLY, PARENT, fly)) {
            assertNotNull(reservation);
            assertTrue(reservation.commit());
        }
        assertEquals(1, data.getEntityCount(FLY, PARENT));
        try (var reservation = data.reserveSpawn(FLY, PARENT, UUID.randomUUID())) {
            assertNotNull(reservation);
        }
        data.consumeEntity(FLY, fly);
        assertEquals(1, data.getCount(FLY, B));
        assertEquals(0, data.getCount(FLY, PARENT));
        try (var reservation = data.reserveSpawn(FLY, PARENT, UUID.randomUUID())) {
            assertNotNull(reservation);
        }
    }

    @Test
    void saveAndDeathCopyRestoreDemandsWithoutRuntimeSources() {
        var data = data();
        data.setCount(PARENT, A, 2);
        data.setCount(FLY, PARENT, 2);
        data.setCount(FLY, B, 1);
        UUID fly = UUID.randomUUID();
        data.registerEntity(FLY, PARENT, fly);
        var copied = data();
        copied.copyFrom(data);
        CompoundTag saved = new CompoundTag();
        data.saveNBTData(saved);
        var loaded = data();
        loaded.loadNBTData(saved);
        for (var restored : List.of(copied, loaded)) {
            assertEquals(2, restored.getCount(FLY, PARENT));
            assertEquals(1, restored.getCount(FLY, B));
            assertEquals(0, restored.getEntityCount(FLY, PARENT));
            assertEquals(2, restored.getEffectiveCount(PARENT));
            assertFalse(restored.containsEntity(FLY, fly));
        }
    }
}

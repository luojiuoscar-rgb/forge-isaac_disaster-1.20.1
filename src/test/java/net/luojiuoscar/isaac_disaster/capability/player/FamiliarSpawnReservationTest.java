package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarLimitGroup;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FamiliarSpawnReservationTest {
    private static final ResourceLocation BLUE = ResourceLocation.parse("test:blue");
    private static final ResourceLocation HIGH = ResourceLocation.parse("test:high");

    @Test
    void abandoningAReplacementNeverChangesGrantedCountsOrRuntimeOrder() {
        PlayerFamiliarData data = data(false);
        data.setCount(BLUE, 2);
        UUID first = UUID.randomUUID(), tail = UUID.randomUUID(), incoming = UUID.randomUUID();
        data.registerEntity(BLUE, first);
        data.registerEntity(BLUE, tail);
        data.queueRecall(tail);
        try (var reservation = data.reserveSpawn(HIGH, incoming)) {
            assertNotNull(reservation);
            assertTrue(data.containsEntity(HIGH, incoming));
            assertEquals(2, data.getCount(BLUE));
            assertEquals(0, data.getCount(HIGH));
            assertEquals(List.of(first, tail), List.copyOf(data.getEntityIdsInAdmissionOrder()));
        }
        assertFalse(data.containsEntity(HIGH, incoming));
        assertEquals(2, data.getEffectiveCount(BLUE));
        assertEquals(tail, data.pollRecall());
    }

    @Test
    void commitAddsExactlyOneRequirementAndAppendsItsUuid() {
        PlayerFamiliarData data = data(false);
        data.setCount(BLUE, 2);
        UUID incoming = UUID.randomUUID();
        try (var reservation = data.reserveSpawn(HIGH, incoming)) {
            assertNotNull(reservation);
            assertTrue(reservation.commit());
            assertFalse(reservation.commit());
        }
        assertEquals(1, data.getCount(BLUE));
        assertEquals(1, data.getCount(HIGH));
        assertEquals(List.of(incoming), List.copyOf(data.getEntityIdsInAdmissionOrder()));
        assertTrue(data.containsEntity(HIGH, incoming));
    }

    @Test
    void storedOverflowDoesNotTurnAnImmediateRequestIntoAMissingEntityRepair() {
        PlayerFamiliarData data = data(true);
        data.setCount(BLUE, 5);
        assertEquals(2, data.getEffectiveCount(BLUE));
        assertNull(data.reserveSpawn(BLUE, UUID.randomUUID()));
        assertEquals(5, data.getCount(BLUE));
        try (var reservation = data.reserveSpawn(HIGH, UUID.randomUUID())) {
            assertNotNull(reservation);
            assertTrue(reservation.commit());
        }
        assertEquals(5, data.getCount(BLUE));
        assertEquals(1, data.getEffectiveCount(BLUE));
    }

    @Test
    void aChangedAllocationIsRecheckedWithoutUndoingTheListener() {
        PlayerFamiliarData data = data(false);
        try (var reservation = data.reserveSpawn(BLUE, UUID.randomUUID())) {
            assertNotNull(reservation);
            data.setCount(HIGH, 2);
            assertFalse(reservation.commit());
        }
        assertEquals(2, data.getCount(HIGH));
        assertEquals(0, data.getCount(BLUE));
    }

    @Test
    void explicitRemovalAndClearingInvalidatePendingSpawns() {
        PlayerFamiliarData data = data(false);
        try (var reservation = data.reserveSpawn(HIGH, UUID.randomUUID())) {
            assertNotNull(reservation);
            data.clearRequirements();
            assertFalse(reservation.commit());
        }
        UUID incoming = UUID.randomUUID();
        try (var reservation = data.reserveSpawn(HIGH, incoming)) {
            assertNotNull(reservation);
            data.removeEntity(HIGH, incoming);
            assertFalse(reservation.commit());
        }
        try (var reservation = data.reserveSpawn(HIGH, UUID.randomUUID())) {
            assertNotNull(reservation);
            data.setCount(HIGH, 1);
            assertFalse(reservation.commit());
        }
        assertEquals(1, data.getCount(HIGH));
    }

    @Test
    void nestedSpawnAndUnknownTypeCannotReserveAnotherSlot() {
        PlayerFamiliarData data = data(false);
        assertNull(data.reserveSpawn(ResourceLocation.parse("test:unknown"), UUID.randomUUID()));
        try (var reservation = data.reserveSpawn(BLUE, UUID.randomUUID())) {
            assertNotNull(reservation);
            assertNull(data.reserveSpawn(HIGH, UUID.randomUUID()));
        }
        try (var reservation = data.reserveSpawn(HIGH, UUID.randomUUID())) {
            assertNotNull(reservation);
        }
    }

    @Test
    void staleOverQuotaRuntimeEntitiesCannotMakeCommitThrowAfterGrantingOwnership() {
        PlayerFamiliarData data = data(false);
        data.setCount(BLUE, 2);
        data.registerEntity(BLUE, UUID.randomUUID());
        data.registerEntity(BLUE, UUID.randomUUID());
        data.setCount(BLUE, 0);
        assertNull(data.reserveSpawn(BLUE, UUID.randomUUID()));
        assertEquals(0, data.getCount(BLUE));
    }

    private static PlayerFamiliarData data(boolean retained) {
        FamiliarLimitGroup group = new FamiliarLimitGroup(2);
        Map<ResourceLocation, FamiliarEntityType> types = Map.of(
                BLUE, new FamiliarEntityType(() -> null, 2, () -> group, 0, retained),
                HIGH, new FamiliarEntityType(() -> null, 2, () -> group, 1, false));
        return new PlayerFamiliarData(types::get, List.of(group));
    }
}

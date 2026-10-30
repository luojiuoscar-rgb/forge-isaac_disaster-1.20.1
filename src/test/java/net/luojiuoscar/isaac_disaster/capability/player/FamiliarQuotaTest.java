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

class FamiliarQuotaTest {
    private static final ResourceLocation FAMILIAR_SOURCE = ResourceLocation.parse("test:source");
    private static final ResourceLocation A = id("a"), B = id("b"), C = id("c");

    @Test
    void firstRequirementImmediatelyClaimsQuotaWithoutWaitingForAnEntity() {
        FamiliarLimitGroup group = new FamiliarLimitGroup(3);
        PlayerFamiliarData data = data(Map.of(A, type(10, group, 0, false), B, type(10, group, 0, false)), group);
        data.setCount(A, FAMILIAR_SOURCE, 10);
        assertEquals(3, data.getCount(A));
        assertEquals(3, data.getEffectiveCount(A));
        assertTrue(data.getEntry(A).orElseThrow().hasNoEntities());
        data.setCount(B, FAMILIAR_SOURCE, 10);
        assertEquals(0, data.getCount(B));
        assertEquals(0, data.getEffectiveCount(B));
    }

    @Test
    void higherPriorityImmediatelyReplacesLatestLowestPriorityTypeAndHonorsRetention() {
        FamiliarLimitGroup group = new FamiliarLimitGroup(2);
        PlayerFamiliarData data = data(Map.of(A, type(10, group, 0, false),
                B, type(10, group, 0, true), C, type(10, group, 5, true)), group);
        data.setCount(A, FAMILIAR_SOURCE, 1);
        data.setCount(B, FAMILIAR_SOURCE, 4);
        data.setCount(C, FAMILIAR_SOURCE, 1);
        assertEquals(1, data.getEffectiveCount(A));
        assertEquals(0, data.getEffectiveCount(B));
        assertEquals(4, data.getCount(B));
        assertEquals(1, data.getEffectiveCount(C));
        data.setCount(C, FAMILIAR_SOURCE, 2);
        assertEquals(0, data.getCount(A));
        assertEquals(2, data.getEffectiveCount(C));
        data.setCount(C, FAMILIAR_SOURCE, 0);
        assertEquals(2, data.getEffectiveCount(B));
        assertEquals(0, data.getCount(A));
    }

    @Test
    void retainedOwnOverflowIsReducedBeforeEffectiveQuota() {
        PlayerFamiliarData data = data(Map.of(A, type(13, null, 0, true)));
        data.setCount(A, FAMILIAR_SOURCE, 20);
        assertEquals(20, data.getCount(A));
        assertEquals(13, data.getEffectiveCount(A));
        data.addCount(A, FAMILIAR_SOURCE, -5);
        assertEquals(15, data.getCount(A));
        assertEquals(13, data.getEffectiveCount(A));
        data.addCount(A, FAMILIAR_SOURCE, -3);
        assertEquals(12, data.getCount(A));
        assertEquals(12, data.getEffectiveCount(A));
        data.addCount(A, FAMILIAR_SOURCE, Integer.MIN_VALUE);
        data.addCount(A, FAMILIAR_SOURCE, Integer.MAX_VALUE);
        data.addCount(A, FAMILIAR_SOURCE, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, data.getCount(A));
        assertEquals(13, data.getEffectiveCount(A));
    }

    @Test
    void groupOverflowWaitsAndConsumesRecordedOverflowBeforeReleasingQuota() {
        FamiliarLimitGroup group = new FamiliarLimitGroup(3);
        PlayerFamiliarData data = data(Map.of(A, type(2, group, 0, true), B, type(4, group, 0, true)), group);
        data.setCount(A, FAMILIAR_SOURCE, 5);
        data.setCount(B, FAMILIAR_SOURCE, 4);
        assertEquals(2, data.getEffectiveCount(A));
        assertEquals(1, data.getEffectiveCount(B));
        data.addCount(A, FAMILIAR_SOURCE, -3);
        assertEquals(2, data.getEffectiveCount(A));
        data.addCount(A, FAMILIAR_SOURCE, -1);
        assertEquals(1, data.getEffectiveCount(A));
        assertEquals(2, data.getEffectiveCount(B));
    }

    @Test
    void lowerPriorityIsReplacedBeforeNewerHigherPriority() {
        FamiliarLimitGroup group = new FamiliarLimitGroup(3);
        PlayerFamiliarData data = data(Map.of(A, type(3, group, -5, false),
                B, type(3, group, 0, false), C, type(3, group, 5, false)), group);
        data.setCount(A, FAMILIAR_SOURCE, 2);
        data.setCount(B, FAMILIAR_SOURCE, 1);
        data.setCount(C, FAMILIAR_SOURCE, 2);
        assertEquals(0, data.getCount(A));
        assertEquals(1, data.getEffectiveCount(B));
        assertEquals(2, data.getEffectiveCount(C));
    }

    @Test
    void lostRuntimeEntitiesDoNotReleaseQuotaOrLeaveRecallEntries() {
        FamiliarLimitGroup group = new FamiliarLimitGroup(1);
        PlayerFamiliarData data = data(Map.of(A, type(2, group, 0, true), B, type(2, group, 0, false)), group);
        data.setCount(A, FAMILIAR_SOURCE, 2);
        UUID runtime = UUID.randomUUID();
        data.registerEntity(A, FAMILIAR_SOURCE, runtime);
        data.queueRecall(runtime);
        data.removeEntity(A, runtime);
        assertNull(data.pollRecall());
        assertEquals(1, data.getEffectiveCount(A));
        data.setCount(B, FAMILIAR_SOURCE, 1);
        assertEquals(0, data.getCount(B));
        data.registerEntity(A, FAMILIAR_SOURCE, UUID.randomUUID());
        assertEquals(1, data.getEffectiveCount(A));
    }

    @Test
    void groupsAndPlayersHaveIndependentCapacityEvenWhenTheirLimitsMatch() {
        FamiliarLimitGroup first = new FamiliarLimitGroup(1), second = new FamiliarLimitGroup(1);
        Map<ResourceLocation, FamiliarEntityType> types = Map.of(A, type(2, first, 0, false), B, type(2, second, 0, false));
        PlayerFamiliarData data = data(types, first, second);
        data.setCount(A, FAMILIAR_SOURCE, 2);
        data.setCount(B, FAMILIAR_SOURCE, 2);
        PlayerFamiliarData other = data(types, first, second);
        other.setCount(A, FAMILIAR_SOURCE, 2);
        assertEquals(1, data.getEffectiveCount(A));
        assertEquals(1, data.getEffectiveCount(B));
        assertEquals(1, other.getEffectiveCount(A));
    }

    @Test
    void saveAndCopyRebuildQuotaFromCountsAndRegistrationOrderWithoutRuntimeState() {
        FamiliarLimitGroup group = new FamiliarLimitGroup(2);
        Map<ResourceLocation, FamiliarEntityType> types = Map.of(A, type(5, group, 0, true), B, type(5, group, 0, true),
                C, type(5, group, 1, true));
        PlayerFamiliarData source = data(types, group);
        source.setCount(B, FAMILIAR_SOURCE, 3);
        source.setCount(A, FAMILIAR_SOURCE, 3);
        source.setCount(C, FAMILIAR_SOURCE, 1);
        source.registerEntity(C, FAMILIAR_SOURCE, UUID.randomUUID());
        CompoundTag saved = new CompoundTag();
        source.saveNBTData(saved);
        PlayerFamiliarData loaded = data(types, group), copied = data(types, group);
        loaded.loadNBTData(saved);
        copied.copyFrom(source);
        for (PlayerFamiliarData restored : List.of(loaded, copied)) {
            assertEquals(List.of(B, A, C), restored.getEntries().stream().map(FamiliarEntry::getType).toList());
            assertEquals(1, restored.getEffectiveCount(B));
            assertEquals(0, restored.getEffectiveCount(A));
            assertEquals(1, restored.getEffectiveCount(C));
            assertTrue(restored.getEntries().stream().allMatch(FamiliarEntry::hasNoEntities));
            assertEquals(3, restored.getCount(A));
        }
        source.clearRequirements();
        assertEquals(0, source.getEffectiveCount(C));
    }

    @Test
    void fullyRemovedThenReaddedTypeBecomesTheLatestReplacementVictim() {
        FamiliarLimitGroup group = new FamiliarLimitGroup(2);
        PlayerFamiliarData data = data(Map.of(A, type(2, group, 0, true), B, type(2, group, 0, true),
                C, type(2, group, 2, false)), group);
        data.setCount(A, FAMILIAR_SOURCE, 1);
        data.setCount(B, FAMILIAR_SOURCE, 1);
        data.setCount(A, FAMILIAR_SOURCE, 0);
        data.setCount(A, FAMILIAR_SOURCE, 1);
        data.setCount(C, FAMILIAR_SOURCE, 1);
        assertEquals(0, data.getEffectiveCount(A));
        assertEquals(1, data.getEffectiveCount(B));
    }

    @Test
    void zeroAndMaximumCapacityStayWithinIntegerBounds() {
        FamiliarLimitGroup group = new FamiliarLimitGroup(Integer.MAX_VALUE), zero = new FamiliarLimitGroup(0);
        PlayerFamiliarData data = data(Map.of(A, type(Integer.MAX_VALUE, group, 0, true),
                B, type(Integer.MAX_VALUE, group, 2, true), C, type(2, zero, 0, false)), group, zero);
        data.setCount(A, FAMILIAR_SOURCE, Integer.MAX_VALUE);
        data.setCount(B, FAMILIAR_SOURCE, Integer.MAX_VALUE);
        assertEquals(0, data.getEffectiveCount(A));
        assertEquals(Integer.MAX_VALUE, data.getCount(A));
        assertEquals(Integer.MAX_VALUE, data.getEffectiveCount(B));
        data.setCount(C, FAMILIAR_SOURCE, 2);
        assertEquals(0, data.getCount(C));
    }

    private static FamiliarEntityType type(int limit, FamiliarLimitGroup group, int priority, boolean retain) {
        return new FamiliarEntityType(() -> null, limit, group == null ? null : () -> group, priority, retain);
    }

    private static PlayerFamiliarData data(Map<ResourceLocation, FamiliarEntityType> types, FamiliarLimitGroup... groups) {
        return new PlayerFamiliarData(types::get, List.of(groups));
    }

    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("test", path); }
}

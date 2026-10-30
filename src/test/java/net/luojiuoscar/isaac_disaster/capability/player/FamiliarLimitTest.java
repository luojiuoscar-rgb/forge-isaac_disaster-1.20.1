package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarLimitGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FamiliarLimitTest {
    private static final ResourceLocation FAMILIAR_SOURCE = ResourceLocation.parse("test:source");
    private static final ResourceLocation LIMITED = ResourceLocation.fromNamespaceAndPath("test", "limited");
    private static final ResourceLocation OTHER = ResourceLocation.fromNamespaceAndPath("test", "other");

    private static PlayerFamiliarData data() {
        FamiliarEntityType limited = new FamiliarEntityType(() -> null, 64);
        FamiliarEntityType other = new FamiliarEntityType(() -> null, 128);
        return new PlayerFamiliarData(type -> LIMITED.equals(type) ? limited : other, List.of());
    }

    @Test
    void defaultDescriptorAdmitsOnlyOneFamiliarAndDropsOverflow() {
        FamiliarEntityType descriptor = new FamiliarEntityType(() -> null);
        PlayerFamiliarData data = new PlayerFamiliarData(type -> descriptor, List.of());
        data.addCount(OTHER, FAMILIAR_SOURCE, 100);
        assertEquals(1, data.getCount(OTHER));
        assertEquals(1, data.getEffectiveCount(OTHER));
        spawn(data, OTHER);
        assertTrue(data.getNextIncompleteEntry().isEmpty());
        data.addCount(OTHER, FAMILIAR_SOURCE, -1);
        assertEquals(0, data.getCount(OTHER));
    }

    @Test
    void nonpositiveDescriptorLimitsSilentlyAllowOneFamiliar() {
        for (int limit : new int[]{Integer.MIN_VALUE, -1, 0}) {
            FamiliarEntityType descriptor = new FamiliarEntityType(() -> null, limit);
            PlayerFamiliarData data = new PlayerFamiliarData(type -> descriptor, List.of());
            data.setCount(OTHER, FAMILIAR_SOURCE, 100);
            assertEquals(1, data.getCount(OTHER), "Invalid limit " + limit + " is corrected to one");
            assertEquals(1, data.getEffectiveCount(OTHER));
            spawn(data, OTHER);
            assertTrue(data.getNextIncompleteEntry().isEmpty());
        }
    }

    @Test
    void excessAdditionsAreDroppedInsteadOfBankedForLaterRemoval() {
        PlayerFamiliarData data = data();
        data.setCount(LIMITED, FAMILIAR_SOURCE, 63);
        data.addCount(LIMITED, FAMILIAR_SOURCE, 5);
        assertEquals(64, data.getCount(LIMITED));
        data.addCount(LIMITED, FAMILIAR_SOURCE, 100);
        data.addCount(LIMITED, FAMILIAR_SOURCE, -1);
        assertEquals(63, data.getCount(LIMITED));
    }

    @Test
    void countMutationAppliesTheOwnLimitAndUpdatesTheEffectiveCount() {
        PlayerFamiliarData data = data();
        data.setCount(LIMITED, FAMILIAR_SOURCE, Integer.MAX_VALUE);
        assertEquals(64, data.getCount(LIMITED));
        assertEquals(64, data.getEffectiveCount(LIMITED));
        data.setCount(LIMITED, FAMILIAR_SOURCE, Integer.MIN_VALUE);
        assertEquals(0, data.getCount(LIMITED));
        assertEquals(0, data.getEffectiveCount(LIMITED));
    }

    @Test
    void overflowingArithmeticStillHonorsExplicitLimits() {
        PlayerFamiliarData data = data();
        data.setCount(OTHER, FAMILIAR_SOURCE, 127);
        data.addCount(OTHER, FAMILIAR_SOURCE, Integer.MAX_VALUE);
        assertEquals(128, data.getCount(OTHER));
        data.addCount(OTHER, FAMILIAR_SOURCE, Integer.MIN_VALUE);
        assertEquals(0, data.getCount(OTHER));
        data.setCount(LIMITED, FAMILIAR_SOURCE, 63);
        data.addCount(LIMITED, FAMILIAR_SOURCE, Integer.MAX_VALUE);
        assertEquals(64, data.getCount(LIMITED));
        data.addCount(LIMITED, FAMILIAR_SOURCE, Integer.MIN_VALUE);
        assertEquals(0, data.getCount(LIMITED));
    }

    @Test
    void nbtAndCopiesApplyTheCurrentDescriptorOwnLimit() {
        CompoundTag overLimitEntry = new CompoundTag();
        overLimitEntry.putString("Type", LIMITED.toString());
        CompoundTag sourceRequirement = new CompoundTag();
        sourceRequirement.putString("Source", FAMILIAR_SOURCE.toString());
        sourceRequirement.putInt("Count", 128);
        ListTag requirements = new ListTag();
        requirements.add(sourceRequirement);
        overLimitEntry.put("Requirements", requirements);
        ListTag entries = new ListTag();
        entries.add(overLimitEntry);
        CompoundTag savedData = new CompoundTag();
        savedData.put("Familiars", entries);
        PlayerFamiliarData loaded = data();
        loaded.loadNBTData(savedData);
        assertEquals(64, loaded.getCount(LIMITED));

        PlayerFamiliarData source = new PlayerFamiliarData(type -> new FamiliarEntityType(() -> null, 128), List.of());
        source.setCount(LIMITED, FAMILIAR_SOURCE, 128);
        PlayerFamiliarData copy = data();
        copy.copyFrom(source);
        assertEquals(64, copy.getCount(LIMITED));
        CompoundTag saved = new CompoundTag();
        copy.saveNBTData(saved);
        CompoundTag savedEntry = saved.getList("Familiars", Tag.TAG_COMPOUND).getCompound(0);
        assertEquals(64, savedEntry.getList("Requirements", Tag.TAG_COMPOUND).getCompound(0).getInt("Count"));
        assertFalse(savedEntry.contains("Limit"));
    }

    @Test
    void ownLimitAndSharedGroupLimitApplyTogether() {
        FamiliarLimitGroup group = new FamiliarLimitGroup(3);
        FamiliarEntityType limited = new FamiliarEntityType(() -> null, 1, () -> group, 0, false);
        FamiliarEntityType other = new FamiliarEntityType(() -> null, 4, () -> group, 0, false);
        PlayerFamiliarData data = new PlayerFamiliarData(type -> LIMITED.equals(type) ? limited : other, List.of(group));
        data.setCount(LIMITED, FAMILIAR_SOURCE, 10);
        data.setCount(OTHER, FAMILIAR_SOURCE, 10);
        assertEquals(1, data.getCount(LIMITED), "The own limit caps requirements");
        assertEquals(1, data.getEffectiveCount(LIMITED), "Changing the requirement grants shared capacity immediately");
        assertEquals(2, data.getEffectiveCount(OTHER));
        spawn(data, LIMITED);
        spawn(data, OTHER);
        spawn(data, OTHER);
        assertTrue(data.getNextIncompleteEntry().isEmpty());
        assertEquals(1, data.getEffectiveCount(LIMITED));
        assertEquals(2, data.getEffectiveCount(OTHER));
        assertEquals(2, data.getCount(OTHER), "Shared overflow is dropped when group capacity is exhausted");
    }

    @Test
    void schedulerStopsAtLimitedCountAndConsumedCountDoesNotRequestReplacement() {
        PlayerFamiliarData data = data();
        data.setCount(LIMITED, FAMILIAR_SOURCE, 128);
        for (int i = 0; i < 64; i++) {
            spawn(data, LIMITED);
        }
        assertTrue(data.getNextIncompleteEntry().isEmpty());
        FamiliarEntry entry = data.getEntry(LIMITED).orElseThrow();
        UUID consumed = entry.getEntityId(0);
        data.addCount(LIMITED, FAMILIAR_SOURCE, -1);
        data.removeEntity(LIMITED, consumed);
        assertEquals(63, entry.getCount());
        assertTrue(data.getNextIncompleteEntry().isEmpty());
    }

    @Test
    void rosterMembershipStaysConsistentThroughDuplicatesRemovalAndClear() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        FamiliarEntry entry = new FamiliarEntry(OTHER);
        entry.requirements().setCount(FAMILIAR_SOURCE, 3);
        for (UUID id : List.of(first, first, second)) entry.addEntity(id, FAMILIAR_SOURCE);
        var order = entry.getEntityIds();
        assertEquals(List.of(first, second), order);
        assertTrue(entry.containsEntity(first));
        assertTrue(entry.removeEntity(first));
        assertFalse(entry.containsEntity(first));
        assertFalse(entry.removeEntity(first));
        assertEquals(List.of(second), order);
        entry.addEntity(first, FAMILIAR_SOURCE);
        assertEquals(List.of(second, first), order);
        entry.clearEntityIds();
        assertTrue(order.isEmpty());
        assertFalse(entry.containsEntity(first));
        assertFalse(entry.containsEntity(second));
        entry.addEntity(second, FAMILIAR_SOURCE);
        assertTrue(entry.containsEntity(second));
        assertEquals(List.of(second), order);
    }

    private static void spawn(PlayerFamiliarData data, ResourceLocation expectedType) {
        assertEquals(expectedType, data.getNextIncompleteEntry().orElseThrow().getType());
        data.registerEntity(expectedType, FAMILIAR_SOURCE, UUID.randomUUID());
    }
}

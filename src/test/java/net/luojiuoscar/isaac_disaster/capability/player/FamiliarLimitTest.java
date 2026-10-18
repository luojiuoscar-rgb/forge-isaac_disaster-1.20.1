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
    private static final ResourceLocation LIMITED = ResourceLocation.fromNamespaceAndPath("test", "limited");
    private static final ResourceLocation OTHER = ResourceLocation.fromNamespaceAndPath("test", "other");

    private static PlayerFamiliarData data() {
        FamiliarEntityType limited = new FamiliarEntityType(() -> null, 64);
        FamiliarEntityType unlimited = new FamiliarEntityType(() -> null);
        return new PlayerFamiliarData(type -> LIMITED.equals(type) ? limited : unlimited, List.of());
    }

    @Test
    void excessAdditionsAreDroppedInsteadOfBankedForLaterRemoval() {
        PlayerFamiliarData data = data();
        data.setCount(LIMITED, 63);
        data.addCount(LIMITED, 5);
        assertEquals(64, data.getCount(LIMITED));
        data.addCount(LIMITED, 100);
        data.addCount(LIMITED, -1);
        assertEquals(63, data.getCount(LIMITED));
    }

    @Test
    void countMutationAppliesTheOwnLimitAndUpdatesTheEffectiveCount() {
        PlayerFamiliarData data = data();
        data.setCount(LIMITED, Integer.MAX_VALUE);
        assertEquals(64, data.getCount(LIMITED));
        assertEquals(64, data.getEffectiveCount(LIMITED));
        data.setCount(LIMITED, Integer.MIN_VALUE);
        assertEquals(0, data.getCount(LIMITED));
        assertEquals(0, data.getEffectiveCount(LIMITED));
    }

    @Test
    void overflowingArithmeticSaturatesAndUnlimitedTypesRetainTheirCounts() {
        PlayerFamiliarData data = data();
        data.setCount(OTHER, Integer.MAX_VALUE - 1);
        data.addCount(OTHER, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, data.getCount(OTHER));
        data.addCount(OTHER, Integer.MIN_VALUE);
        assertEquals(0, data.getCount(OTHER));
        data.setCount(LIMITED, 63);
        data.addCount(LIMITED, Integer.MAX_VALUE);
        assertEquals(64, data.getCount(LIMITED));
        data.addCount(LIMITED, Integer.MIN_VALUE);
        assertEquals(0, data.getCount(LIMITED));
    }

    @Test
    void nbtAndCopiesApplyTheCurrentDescriptorOwnLimit() {
        CompoundTag oldEntry = new CompoundTag();
        oldEntry.putString("Type", LIMITED.toString());
        oldEntry.putInt("Count", 128);
        ListTag entries = new ListTag();
        entries.add(oldEntry);
        CompoundTag oldData = new CompoundTag();
        oldData.put("Familiars", entries);
        PlayerFamiliarData loaded = data();
        loaded.loadNBTData(oldData);
        assertEquals(64, loaded.getCount(LIMITED));

        PlayerFamiliarData unlimitedSource = new PlayerFamiliarData(type -> new FamiliarEntityType(() -> null), List.of());
        unlimitedSource.setCount(LIMITED, 128);
        PlayerFamiliarData copy = data();
        copy.copyFrom(unlimitedSource);
        assertEquals(64, copy.getCount(LIMITED));
        CompoundTag saved = new CompoundTag();
        copy.saveNBTData(saved);
        CompoundTag savedEntry = saved.getList("Familiars", Tag.TAG_COMPOUND).getCompound(0);
        assertEquals(64, savedEntry.getInt("Count"));
        assertFalse(savedEntry.contains("Limit"));
    }

    @Test
    void ownLimitAndSharedGroupLimitApplyTogether() {
        FamiliarLimitGroup group = new FamiliarLimitGroup(3);
        FamiliarEntityType limited = new FamiliarEntityType(() -> null, 1, () -> group, 0, false);
        FamiliarEntityType other = new FamiliarEntityType(() -> null, 4, () -> group, 0, false);
        PlayerFamiliarData data = new PlayerFamiliarData(type -> LIMITED.equals(type) ? limited : other, List.of(group));
        data.setCount(LIMITED, 10);
        data.setCount(OTHER, 10);
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
        data.setCount(LIMITED, 128);
        for (int i = 0; i < 64; i++) {
            spawn(data, LIMITED);
        }
        assertTrue(data.getNextIncompleteEntry().isEmpty());
        FamiliarEntry entry = data.getEntry(LIMITED).orElseThrow();
        UUID consumed = entry.getEntityId(0);
        data.addCount(LIMITED, -1);
        data.removeEntity(LIMITED, consumed);
        assertEquals(63, entry.getCount());
        assertTrue(data.getNextIncompleteEntry().isEmpty());
    }

    private static void spawn(PlayerFamiliarData data, ResourceLocation expectedType) {
        assertEquals(expectedType, data.getNextIncompleteEntry().orElseThrow().getType());
        data.registerEntity(expectedType, UUID.randomUUID());
    }
}

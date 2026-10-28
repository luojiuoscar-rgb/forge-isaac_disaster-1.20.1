package net.luojiuoscar.isaac_disaster.system.familiar;

import net.luojiuoscar.isaac_disaster.entity.familiar.base.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.FamiliarModifierEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static net.luojiuoscar.isaac_disaster.registries.familiar_modifier.FamiliarModifierEntry.Parameter.*;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarModifiersTest {
    private final ResourceLocation first = id("first");
    private final ResourceLocation second = id("second");
    private final ResourceLocation size = id("size");
    private final ResourceLocation swarmSize = id("swarm_size");
    private final Map<ResourceLocation, FamiliarModifierEntry> definitions = new HashMap<>();
    private FamiliarModifiers modifiers;

    @BeforeEach
    void setUp() {
        definitions.put(first, new FamiliarModifierEntry(ENHANCEMENT, 1, familiar -> true));
        definitions.put(second, new FamiliarModifierEntry(ENHANCEMENT, 0.5, familiar -> true));
        definitions.put(size, new FamiliarModifierEntry(VISUAL_SCALE, 0.25, familiar -> true));
        definitions.put(swarmSize, new FamiliarModifierEntry(VISUAL_SCALE, 0.5, familiar -> true));
        modifiers = new FamiliarModifiers(definitions::get);
    }

    @Test
    void emptyCollectionsProvideZeroEnhancementAndNormalVisualScale() {
        assertEquals(0, modifiers.enhancement(null, false));
        assertEquals(1, modifiers.visualScale(null, false));
    }

    @Test
    void registeredPredicatesRemainDynamicAndSupportComposition() {
        AtomicBoolean eligible = new AtomicBoolean();
        Predicate<AbstractIsaacFamiliarEntity> filter = familiar -> eligible.get();
        definitions.put(first, new FamiliarModifierEntry(ENHANCEMENT, 1, filter));
        definitions.put(second, new FamiliarModifierEntry(ENHANCEMENT, 0.5, filter.and(familiar -> true)));
        definitions.put(size, new FamiliarModifierEntry(VISUAL_SCALE, 0.25, filter.negate()));
        modifiers.add(first, 1);
        modifiers.add(second, 1);
        modifiers.add(size, 1);
        assertEquals(0, modifiers.enhancement(null, false));
        assertEquals(1.25F, modifiers.visualScale(null, false));
        eligible.set(true);
        assertEquals(1.5, modifiers.enhancement(null, false));
        assertEquals(1, modifiers.visualScale(null, false));
    }

    @Test
    void differentIdsAddEvenWhenDuplicateStackingIsDisabled() {
        modifiers.add(first, 3);
        modifiers.add(second, 2);
        assertEquals(1.5, modifiers.enhancement(null, false));
        assertEquals(4, modifiers.enhancement(null, true));
        assertEquals(3, modifiers.getCount(first));
        assertEquals(2, modifiers.getCount(second));
    }

    @Test
    void visualIncrementsAddInsteadOfMultiplyingAcrossSourcesAndCopies() {
        modifiers.add(size, 2);
        modifiers.add(swarmSize, 2);
        assertEquals(1.75F, modifiers.visualScale(null, false));
        assertEquals(2.5F, modifiers.visualScale(null, true));
    }

    @Test
    void removingOneCopyRetainsItsEffectUntilTheLastCopyIsRemoved() {
        modifiers.add(first, 2);
        modifiers.remove(first, 1);
        assertEquals(1, modifiers.enhancement(null, false));
        modifiers.remove(first, 1);
        assertEquals(0, modifiers.enhancement(null, false));
        assertTrue(modifiers.snapshot(ENHANCEMENT).isEmpty());
    }

    @Test
    void nbtRoundTripStoresOnlyIdsAndCountsAndRetainsBothChannels() {
        modifiers.add(first, 3);
        modifiers.add(size, 2);
        CompoundTag saved = modifiers.save();
        assertEquals(2, saved.getAllKeys().size());
        var entry = saved.getList("enhancement", 10).getCompound(0);
        assertEquals(Set.of("id", "count"), entry.getAllKeys());
        var restored = new FamiliarModifiers(definitions::get);
        restored.load(saved);
        assertEquals(modifiers.snapshot(ENHANCEMENT), restored.snapshot(ENHANCEMENT));
        assertEquals(modifiers.snapshot(VISUAL_SCALE), restored.snapshot(VISUAL_SCALE));
        assertEquals(3, restored.enhancement(null, true));
        assertEquals(1.5F, restored.visualScale(null, true));
    }

    @Test
    void copiedCollectionsAndSnapshotsDoNotShareMutableCounts() {
        modifiers.add(first, 2);
        var copied = new FamiliarModifiers(definitions::get);
        copied.copyFrom(modifiers);
        copied.remove(first, 1);
        assertEquals(2, modifiers.getCount(first));
        assertEquals(1, copied.getCount(first));
        assertThrows(UnsupportedOperationException.class, () -> modifiers.snapshot(ENHANCEMENT).put(first, 9));
        copied.clear();
        assertEquals(2, modifiers.getCount(first));
    }

    @Test
    void addingAnotherRegisteredDefinitionDoesNotRequireAContainerChange() {
        var additional = id("additional");
        AtomicBoolean eligible = new AtomicBoolean();
        definitions.put(additional, new FamiliarModifierEntry(ENHANCEMENT, 0.75,
                familiar -> eligible.get()));
        modifiers.add(additional, 2);
        assertEquals(0, modifiers.enhancement(null, true));
        eligible.set(true);
        assertEquals(0.75, modifiers.enhancement(null, false));
        assertEquals(1.5, modifiers.enhancement(null, true));
    }

    @Test
    void unknownIdsAndMisplacedSavedEntriesAreIgnored() {
        CompoundTag saved = new CompoundTag();
        ListTag list = new ListTag();
        for (String value : new String[]{"INVALID ID", id("missing").toString(), size.toString()}) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", value);
            entry.putInt("count", 3);
            list.add(entry);
        }
        saved.put("enhancement", list);
        assertDoesNotThrow(() -> modifiers.load(saved));
        assertTrue(modifiers.snapshot(ENHANCEMENT).isEmpty());
        assertTrue(modifiers.snapshot(VISUAL_SCALE).isEmpty());
    }

    @Test
    void countsClampWithoutWrappingAndNegativeTotalsCannotInvertVisuals() {
        modifiers.setCount(first, -5);
        assertEquals(0, modifiers.getCount(first));
        modifiers.add(first, Integer.MAX_VALUE);
        modifiers.add(first, 1);
        assertEquals(Integer.MAX_VALUE, modifiers.getCount(first));
        modifiers.remove(first, Integer.MAX_VALUE);
        assertEquals(0, modifiers.getCount(first));
        definitions.put(size, new FamiliarModifierEntry(VISUAL_SCALE, -2, familiar -> true));
        modifiers.add(size, 1);
        assertEquals(0, modifiers.visualScale(null, false));
        assertEquals(0, new FamiliarModifierEntry(ENHANCEMENT, Double.NaN, familiar -> true).amount());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("familiar_test", path);
    }

}

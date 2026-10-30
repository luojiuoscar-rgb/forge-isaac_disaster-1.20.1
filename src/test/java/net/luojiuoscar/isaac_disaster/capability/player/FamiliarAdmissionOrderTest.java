package net.luojiuoscar.isaac_disaster.capability.player;

import net.luojiuoscar.isaac_disaster.registries.familiar.FamiliarEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FamiliarAdmissionOrderTest {
    private static final ResourceLocation FAMILIAR_SOURCE = ResourceLocation.parse("test:source");
    private static final ResourceLocation BLUE = ResourceLocation.parse("test:blue");
    private static final ResourceLocation GREEN = ResourceLocation.parse("test:green");

    @Test
    void orderSpansTypesAndSurvivesDuplicateRegistrationAndRecall() {
        PlayerFamiliarData data = data();
        UUID first = UUID.randomUUID(), second = UUID.randomUUID(), third = UUID.randomUUID();
        data.setCount(BLUE, FAMILIAR_SOURCE, 2);
        data.setCount(GREEN, FAMILIAR_SOURCE, 1);
        data.registerEntity(BLUE, FAMILIAR_SOURCE, first);
        data.registerEntity(GREEN, FAMILIAR_SOURCE, second);
        data.registerEntity(BLUE, FAMILIAR_SOURCE, third);
        data.registerEntity(BLUE, FAMILIAR_SOURCE, first);
        data.queueRecall(first);
        assertEquals(first, data.pollRecall());
        assertEquals(List.of(first, second, third), List.copyOf(data.getEntityIdsInAdmissionOrder()));
    }

    @Test
    void anUnlinkedEntityMustJoinAfterSurvivorsIfRegisteredAgain() {
        PlayerFamiliarData data = data();
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        data.setCount(BLUE, FAMILIAR_SOURCE, 2);
        data.registerEntity(BLUE, FAMILIAR_SOURCE, first);
        data.registerEntity(BLUE, FAMILIAR_SOURCE, second);
        data.removeEntity(BLUE, first);
        assertEquals(List.of(second), List.copyOf(data.getEntityIdsInAdmissionOrder()));
        data.registerEntity(BLUE, FAMILIAR_SOURCE, first);
        assertEquals(List.of(second, first), List.copyOf(data.getEntityIdsInAdmissionOrder()));
    }

    @Test
    void disposalSaveAndPlayerCopyPreserveRequirementsWithoutOldRuntimeOrder() {
        PlayerFamiliarData source = data();
        UUID old = UUID.randomUUID();
        source.setCount(BLUE, FAMILIAR_SOURCE, 2);
        source.registerEntity(BLUE, FAMILIAR_SOURCE, old);
        CompoundTag saved = new CompoundTag();
        source.saveNBTData(saved);
        PlayerFamiliarData loaded = data();
        loaded.loadNBTData(saved);
        PlayerFamiliarData copied = data();
        copied.copyFrom(source);
        source.queueRecall(old);
        source.clearRuntimeEntities();
        for (PlayerFamiliarData data : new PlayerFamiliarData[]{source, loaded, copied}) {
            assertEquals(2, data.getCount(BLUE));
            assertEquals(2, data.getEffectiveCount(BLUE));
            assertTrue(data.getEntry(BLUE).orElseThrow().hasNoEntities());
            assertTrue(data.getEntityIdsInAdmissionOrder().isEmpty());
            assertNull(data.pollRecall());
            UUID fresh = UUID.randomUUID();
            data.registerEntity(BLUE, FAMILIAR_SOURCE, fresh);
            assertEquals(List.of(fresh), List.copyOf(data.getEntityIdsInAdmissionOrder()));
        }
    }

    private static PlayerFamiliarData data() {
        FamiliarEntityType descriptor = new FamiliarEntityType(() -> null, 64);
        return new PlayerFamiliarData(type -> descriptor, List.of());
    }
}

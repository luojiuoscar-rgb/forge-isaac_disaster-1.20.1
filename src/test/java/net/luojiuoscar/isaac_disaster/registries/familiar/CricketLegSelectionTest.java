package net.luojiuoscar.isaac_disaster.registries.familiar;

import net.luojiuoscar.isaac_disaster.entity.familiar.AbstractIsaacFamiliarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.Locust;
import net.luojiuoscar.isaac_disaster.entity.familiar.DisposableFamiliar;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.BlueFlyEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfConquestEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfDeathEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfFamineEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfPestilenceEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly.LocustOfWarEntity;
import net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider.BlueSpiderEntity;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.CricketLeg;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CricketLegSelectionTest {
    @Test
    void probabilityAccumulatesOnlyTheFirstTwoContributions() {
        assertEquals(0, CricketLeg.getChance(0));
        assertEquals(1.0 / 6, CricketLeg.getChance(1));
        assertEquals(1.0 / 3, CricketLeg.getChance(2));
        assertEquals(1.0 / 3, CricketLeg.getChance(3));
        assertEquals(1.0 / 3, CricketLeg.getChance(6));
        assertEquals(1.0 / 3, CricketLeg.getChance(Integer.MAX_VALUE));
    }

    @Test
    void invalidStrengthDoesNotProc() {
        for (double value : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0, CricketLeg.getChance(value));
        }
    }

    @Test
    void allBuiltInLocustsInheritDisposableMarkerAndBelongToPool() {
        for (var entityClass : List.of(LocustOfWarEntity.class, LocustOfDeathEntity.class,
                LocustOfFamineEntity.class, LocustOfPestilenceEntity.class, LocustOfConquestEntity.class)) {
            FamiliarEntityType type = descriptor(entityClass);
            assertTrue(type.hasEntityTag(Locust.class));
            assertTrue(type.hasEntityTag(DisposableFamiliar.class));
            assertTrue(CricketLeg.isEligibleType(type));
        }
    }

    @Test
    void ordinaryDisposableFamiliarsAreExcludedWithoutLocustMarker() {
        for (var entityClass : List.of(BlueFlyEntity.class, BlueSpiderEntity.class)) {
            var type = descriptor(entityClass);
            assertTrue(type.hasEntityTag(DisposableFamiliar.class));
            assertFalse(CricketLeg.isEligibleType(type));
        }
    }

    @Test
    void additionalTypesAreSelectedByMarkersRatherThanBuiltInIds() {
        assertTrue(CricketLeg.isEligibleType(descriptor(AdditionalLocust.class)));
        assertFalse(CricketLeg.isEligibleType(descriptor(PersistentLocust.class)));
    }

    @Test
    void legacyDescriptorRemainsUsableButDoesNotEnterRandomPool() {
        var type = new FamiliarEntityType(() -> { throw new AssertionError("Factory must not resolve during filtering"); });
        assertEquals(1, type.getLimit());
        assertNull(type.getEntityClass());
        assertFalse(CricketLeg.isEligibleType(type));
        assertFalse(CricketLeg.isEligibleType(null));
    }

    private static FamiliarEntityType descriptor(Class<? extends AbstractIsaacFamiliarEntity> entityClass) {
        return new FamiliarEntityType(() -> { throw new AssertionError("Filtering must not resolve factories"); },
                entityClass, 64, null, 0, false);
    }

    private abstract static class AdditionalLocust extends BlueFlyEntity implements Locust {
        protected AdditionalLocust(EntityType<? extends BlueFlyEntity> type, Level level) {
            super(type, level);
        }
    }

    private abstract static class PersistentLocust extends AbstractIsaacFamiliarEntity implements Locust {
        protected PersistentLocust(EntityType<?> type, Level level) {
            super(type, level);
        }
    }
}

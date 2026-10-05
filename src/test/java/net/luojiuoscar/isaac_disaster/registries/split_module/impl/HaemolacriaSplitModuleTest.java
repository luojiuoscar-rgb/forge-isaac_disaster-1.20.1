package net.luojiuoscar.isaac_disaster.registries.split_module.impl;

import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HaemolacriaSplitModuleTest {
    @Test
    void contactEligibilityRespectsPiercingAndSpectralTears() {
        assertTrue(HaemolacriaSplitModule.canBurstOn(SplitTriggerType.ENTITY, false, false));
        assertFalse(HaemolacriaSplitModule.canBurstOn(SplitTriggerType.ENTITY, true, false));
        assertTrue(HaemolacriaSplitModule.canBurstOn(SplitTriggerType.BLOCK, false, false));
        assertFalse(HaemolacriaSplitModule.canBurstOn(SplitTriggerType.BLOCK, false, true));
        assertTrue(HaemolacriaSplitModule.canBurstOn(SplitTriggerType.END_OF_LIFE, true, true));
    }

    @Test
    void burstCountsAndIndependentDamageMultipliersStayWithinBounds() {
        RandomSource random = RandomSource.create(531);
        Set<Integer> counts = new HashSet<>();
        Set<Double> multipliers = new HashSet<>();

        for (int i = 0; i < 10_000; i++) {
            int count = HaemolacriaSplitModule.rollBulletCount();
            double multiplier = HaemolacriaSplitModule.damageMultiplier(random);
            assertTrue(count >= 6 && count <= 11);
            assertTrue(multiplier >= 0.5 && multiplier < 0.5 + 1.0 / 3.0);
            counts.add(count);
            multipliers.add(multiplier);
        }

        assertEquals(Set.of(6, 7, 8, 9, 10, 11), counts);
        assertTrue(multipliers.size() > 9_000);
    }
}

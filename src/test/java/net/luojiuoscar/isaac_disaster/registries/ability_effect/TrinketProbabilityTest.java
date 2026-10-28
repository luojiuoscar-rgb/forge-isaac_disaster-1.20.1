package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general.ChestLootTrinket;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.general.PennyTrinket;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BibleTract;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BlackLipstick;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.Cartridge;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.CricketLeg;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.DaemonsTail;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.LuckyRock;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.SwallowedPenny;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrinketProbabilityTest {
    @Test
    void otherTrinketProbabilityCapsDoNotLoseTheirLimits() {
        assertEquals(0.05, LuckyRock.getChance(1));
        assertEquals(0.1, LuckyRock.getChance(2));
        assertEquals(0.1, LuckyRock.getChance(100));
        assertEquals(1.0 / 6, CricketLeg.getChance(1));
        assertEquals(1.0 / 3, CricketLeg.getChance(2));
        assertEquals(1.0 / 3, CricketLeg.getChance(100));
    }

    @Test
    void lootConversionChancesScaleUntilTheyReachCertainty() {
        assertEquals(0.1, BlackLipstick.getChance(1));
        assertEquals(0.2, BlackLipstick.getChance(2));
        assertEquals(1, BlackLipstick.getChance(10));
        assertEquals(1, BlackLipstick.getChance(100));
        assertEquals(1.0 / 30, BibleTract.getChance(1));
        assertEquals(2.0 / 30, BibleTract.getChance(2));
        assertEquals(1, BibleTract.getChance(30));
        assertEquals(1, BibleTract.getChance(100));
    }

    @Test
    void cartridgePreservesItsLuckDependentStackingFormula() {
        assertEquals(0.05, Cartridge.getChance(1, 0));
        assertEquals(0.1, Cartridge.getChance(2, 0));
        assertEquals(0.2, Cartridge.getChance(4, 0));
        assertEquals(0.1, Cartridge.getChance(1, 20));
        assertEquals(1, Cartridge.getChance(1, 100));
    }

    @Test
    void pennyProbabilitiesAreFixedAndSwallowedPennyKeepsItsGoldUpgrade() {
        assertEquals(0.25, PennyTrinket.getResourceChance());
        assertEquals(0.125, PennyTrinket.getHeartOrBatteryChance());
        assertEquals(1.0 / 3, PennyTrinket.getCounterfeitChance());
        assertEquals(0.35, SwallowedPenny.getChance(false));
        assertEquals(0.7, SwallowedPenny.getChance(true));
    }

    @Test
    void ordinaryAndGoldLootValuesHaveTheApprovedMeaning() {
        assertEquals(0.33, ChestLootTrinket.getChance(false));
        assertEquals(0.66, ChestLootTrinket.getChance(true));
        assertEquals(0.7, DaemonsTail.getRemovalChance(false));
        assertEquals(0.35, DaemonsTail.getRemovalChance(true));
    }

    @Test
    void invalidProbabilityStrengthIsSafelyZero() {
        for (double invalid : new double[]{-1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            assertEquals(0, LuckyRock.getChance(invalid));
            assertEquals(0, BlackLipstick.getChance(invalid));
            assertEquals(0, BibleTract.getChance(invalid));
            assertEquals(0, Cartridge.getChance(invalid, 0));
        }
    }
}

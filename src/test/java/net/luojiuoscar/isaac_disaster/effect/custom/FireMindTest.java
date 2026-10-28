package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FireMindTest {
    @Test
    void collectibleAppendsItsOwnIdAndUsesQualityTwo() {
        ItemId item = ItemId.valueOf("FIRE_MIND");
        assertEquals(ItemId.HOLY_LIGHT.ordinal() + 1, item.getId());
        assertEquals(item.ordinal(), item.getId());
        assertEquals(2, item.getLevel());
    }

    @Test
    void burningUsesTheSameHalfPointSnapshotsAsDeadlyPoison() {
        for (double value : new double[]{-100, 0, 1.24, 1.25, 2, 8.2, 8.3, 1e100,
                Double.POSITIVE_INFINITY, Double.NaN}) {
            int amplifier = BurningEffect.amplifierForDamage(value);
            assertEquals(DeadlyPoisonEffect.amplifierForDamage(value), amplifier);
            assertEquals(DeadlyPoisonEffect.damageForAmplifier(amplifier), BurningEffect.damageForAmplifier(amplifier));
        }
    }

    @Test
    void damageClockFiresAtFortyAndEightyInsteadOfOnApplication() {
        int elapsed = 0;
        int pulses = 0;
        for (int tick = 1; tick <= 80; tick++) {
            elapsed = BurningEffect.advanceDamageClock(elapsed);
            if (elapsed == 0) {
                assertEquals(++pulses * 40, tick);
            }
        }
        assertEquals(2, pulses);
    }

    @Test
    void invalidClockInputIsSafelyNormalized() {
        assertEquals(1, BurningEffect.advanceDamageClock(-1));
        assertEquals(0, BurningEffect.advanceDamageClock(Integer.MAX_VALUE));
    }
}

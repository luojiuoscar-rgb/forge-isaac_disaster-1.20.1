package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttackChargeTimingTest {
    @Test
    void brimstoneKeepsTheThreefoldIntervalBeforeRoundingToTicks() {
        assertEquals(21, brimstone(7.0).getTotalCharge(null));
        assertEquals(16, brimstone(5.1).getTotalCharge(null));
        assertEquals(1, brimstone(0.0).getTotalCharge(null));
    }

    @Test
    void cSectionConvertsTheWikiChargeIntervalToMinecraftTicks() {
        // A 0.35 s ordinary interval corresponds to 9.5 Isaac tear delay:
        // the wiki charge interval is 30/30 s, or 20 Minecraft ticks.
        assertEquals(20, cSection(7.0).getTotalCharge(null));
        assertEquals(16, cSection(5.5).getTotalCharge(null));
        assertEquals(20, cSection(6.7).getTotalCharge(null));
        assertEquals(11, cSection(4.0).getTotalCharge(null));
    }

    @Test
    void cSectionRespectsTheWikiFifteenFetusesPerSecondLimit() {
        assertEquals(2, cSection(1.0).getTotalCharge(null));
        assertEquals(2, cSection(0.0).getTotalCharge(null));
    }

    private static BrimstoneAttack brimstone(double delay) {
        return new BrimstoneAttack(0.0) {
            @Override
            protected double getShotDelay(Player player) {
                return delay;
            }
        };
    }

    private static CSectionAttack cSection(double delay) {
        return new CSectionAttack(0.0) {
            @Override
            protected double getShotDelay(Player player) {
                return delay;
            }
        };
    }
}

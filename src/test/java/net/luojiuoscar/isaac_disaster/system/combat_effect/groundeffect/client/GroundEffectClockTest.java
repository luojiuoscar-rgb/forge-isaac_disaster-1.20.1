package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.client;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

class GroundEffectClockTest {
    @Test
    void sparseSamplesLearnASlowerServerTickRate() {
        AtomicLong nanos = new AtomicLong();
        var clock = new GroundEffectClock(nanos::get);
        clock.observe(100);
        nanos.set(2_000_000_000L);
        clock.observe(120);
        nanos.set(3_000_000_000L);
        assertEquals(130.0D, clock.now(), 0.001D);
        clock.observe(110);
        assertEquals(130.0D, clock.now(), 0.001D, "Older snapshots must not rewind the shared clock");
    }

    @Test
    void signedTickWrapDoesNotResetLifetimeTimebase() {
        AtomicLong nanos = new AtomicLong();
        var clock = new GroundEffectClock(nanos::get);
        clock.observe(Integer.MAX_VALUE - 9);
        nanos.set(1_000_000_000L);
        clock.observe(Integer.MIN_VALUE + 10);
        assertEquals(2147483658.0D, clock.now(), 0.001D);
    }
}

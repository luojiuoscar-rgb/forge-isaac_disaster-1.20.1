package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.client;

import java.util.function.LongSupplier;

/** One calibrated server tick estimate shared by every effect in the current session. */
public final class GroundEffectClock {
    private final LongSupplier nanos;
    private final long localOriginNanos;
    private double localTick;
    private boolean initialized;
    private long sampleTick;
    private long sampleNanos;
    private long calibrationTick;
    private long calibrationNanos;
    private double ticksPerNano = 20.0D / 1_000_000_000.0D;

    public GroundEffectClock(LongSupplier nanos) {
        this.nanos = nanos == null ? System::nanoTime : nanos;
        localOriginNanos = this.nanos.getAsLong();
    }

    /** Fixed 20 TPS animation time; server samples and session resets cannot rewind it. */
    public double localTicks() {
        localTick = Math.max(localTick, Math.max(0L, nanos.getAsLong() - localOriginNanos)
                * (20.0D / 1_000_000_000.0D));
        return localTick;
    }

    public long unwrap(int tick) {
        return initialized ? sampleTick + (int) (tick - (int) sampleTick) : Integer.toUnsignedLong(tick);
    }

    public void observe(int tick) {
        long value = unwrap(tick);
        if (initialized && value <= sampleTick) return;
        long now = nanos.getAsLong();
        if (!initialized) {
            calibrationTick = value;
            calibrationNanos = now;
        } else if (now - calibrationNanos >= 250_000_000L) {
            ticksPerNano = Math.min(20.0D / 1_000_000_000.0D,
                    (double) (value - calibrationTick) / (now - calibrationNanos));
            calibrationTick = value;
            calibrationNanos = now;
        }
        initialized = true;
        sampleTick = value;
        sampleNanos = now;
    }

    public double now() {
        return initialized ? sampleTick + Math.max(0L, nanos.getAsLong() - sampleNanos) * ticksPerNano : 0;
    }

    public void clear() {
        initialized = false;
        sampleTick = 0;
        sampleNanos = 0;
        ticksPerNano = 20.0D / 1_000_000_000.0D;
    }
}

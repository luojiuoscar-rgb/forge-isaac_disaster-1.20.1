package net.luojiuoscar.isaac_disaster.client.gui.charge_bar;

import net.luojiuoscar.isaac_disaster.networking.packet.ChargeBarUpdateS2CPacket.Action;

/** Display-only extrapolation. Tick time includes the current frame's partial tick. */
public final class ChargeBarPrediction {
    private static final double CORRECTION_TICKS = 3;
    private float progress;
    private float rate;
    private double updatedAt;
    private float correction;

    public void update(Action action, float progress, float rate, double now) {
        float displayed = sample(now);
        this.progress = Math.max(0f, Math.min(1f, progress));
        this.rate = action == Action.END || this.progress >= 1f ? 0f : Math.max(0f, rate);
        this.updatedAt = now;
        // Small periodic corrections blend; resets, stops and confirmed full charge apply immediately.
        float error = displayed - this.progress;
        this.correction = action == Action.CORRECT && this.progress < 1f
                && Math.abs(error) <= 0.15f ? error : 0f;
    }

    public float sample(double now) {
        double elapsed = Math.max(0, now - updatedAt);
        double blend = Math.max(0, 1 - elapsed / CORRECTION_TICKS);
        double predicted = progress + rate * elapsed + correction * blend;
        // Only an authoritative full update may flash or hide the approaching ring.
        float maximum = progress >= 1f ? 1f : Math.nextDown(1f);
        return (float) Math.max(0, Math.min(maximum, predicted));
    }
}

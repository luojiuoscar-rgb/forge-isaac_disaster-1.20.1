package net.luojiuoscar.isaac_disaster.client.gui.charge_bar;

import net.luojiuoscar.isaac_disaster.networking.packet.ChargeBarUpdateS2CPacket.Action;

/** Display-only extrapolation. Tick time includes the current frame's partial tick. */
public final class ChargeBarPrediction {
    private static final double CORRECTION_TICKS = 3;
    private float progress;
    private float rate;
    private double updatedAt;
    private float correction;
    private double fullChargeAt = Double.NaN;

    public void update(Action action, float progress, float rate, double now) {
        float displayed = sample(now);
        float normalized = Math.max(0f, Math.min(1f, progress));
        if (normalized < 1f) fullChargeAt = Double.NaN;
        else if (this.progress < 1f) fullChargeAt = now;
        this.progress = normalized;
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
        // Only an authoritative full update may flash or start the completion animation.
        float maximum = progress >= 1f ? 1f : Math.nextDown(1f);
        return (float) Math.max(0, Math.min(maximum, predicted));
    }

    public ChargeRingAnimation.Visual sampleVisual(double now) {
        float displayed = sample(now);
        if (progress >= 1f) {
            boolean visible = Double.isFinite(fullChargeAt)
                    && now < fullChargeAt + ChargeRingAnimation.COMPLETION_TICKS;
            return new ChargeRingAnimation.Visual(displayed,
                    ChargeRingAnimation.completionDistance(now - fullChargeAt), visible);
        }
        return new ChargeRingAnimation.Visual(displayed,
                ChargeRingAnimation.approachDistance(displayed), displayed > 0f);
    }
}

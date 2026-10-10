package net.luojiuoscar.isaac_disaster.client.gui.charge_bar;

/** Shared progress visuals and full-charge flash; time is a monotonic clock in milliseconds. */
public final class ChargeRingAnimation {
    public static final float APPROACH_RING_WIDTH = 1f;
    private static final double APPROACH_DISTANCE = 4.0D;
    private static final long FLASH_PERIOD_MS = 400;
    private static final long WHITE_DURATION_MS = 200;

    /** At full charge the thin ring's inner edge meets the icon's circular outer edge. */
    public static double approachRadius(float progress) {
        double normalized = Float.isFinite(progress) ? Math.max(0f, Math.min(1f, progress)) : 0;
        double iconRadius = ChargeRingGeometry.SIZE / 2.0D - 0.25D;
        return iconRadius + APPROACH_RING_WIDTH / 2.0D + APPROACH_DISTANCE * (1 - normalized);
    }

    public static int fillColor(float progress, int registeredColor, long timeMillis) {
        if (Float.isFinite(progress) && progress >= 1f
                && Math.floorMod(timeMillis, FLASH_PERIOD_MS) < WHITE_DURATION_MS) {
            return (registeredColor & 0xFF000000) | 0x00FFFFFF;
        }
        return registeredColor;
    }

    private ChargeRingAnimation() {
    }
}

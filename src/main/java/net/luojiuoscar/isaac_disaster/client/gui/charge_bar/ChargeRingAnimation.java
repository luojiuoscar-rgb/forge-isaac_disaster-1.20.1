package net.luojiuoscar.isaac_disaster.client.gui.charge_bar;

/** Distance-driven ring visuals; completion uses client ticks, and the fill flash uses milliseconds. */
public final class ChargeRingAnimation {
    public static final float APPROACH_RING_WIDTH = 1f;
    public static final double APPROACH_DISTANCE = 4.0D;
    /** 180 milliseconds at twenty client ticks per second. */
    public static final double COMPLETION_TICKS = 3.6D;
    private static final long FLASH_PERIOD_MS = 400;
    private static final long WHITE_DURATION_MS = 200;

    public record Visual(float progress, double ringDistance, boolean ringVisible) {
    }

    public static double approachDistance(float progress) {
        double normalized = Float.isFinite(progress) ? Math.max(0f, Math.min(1f, progress)) : 0;
        return APPROACH_DISTANCE * (1 - normalized);
    }

    /** The same edge-to-edge distance controls opacity during approach and completion. */
    public static int alphaAtDistance(double distance) {
        return (int) Math.round(255 * (0.8D - 0.7D * normalizedDistance(distance) / APPROACH_DISTANCE));
    }

    public static double completionDistance(double elapsedTicks) {
        double u = Math.max(0, Math.min(1, elapsedTicks / COMPLETION_TICKS));
        return APPROACH_DISTANCE * (1 - (1 - u) * (1 - u));
    }

    public static double radiusAtDistance(double distance) {
        double iconRadius = ChargeRingGeometry.SIZE / 2.0D - 0.25D;
        return iconRadius + APPROACH_RING_WIDTH / 2.0D + normalizedDistance(distance);
    }

    private static double normalizedDistance(double distance) {
        return Double.isFinite(distance) ? Math.max(0, Math.min(APPROACH_DISTANCE, distance)) : APPROACH_DISTANCE;
    }

    /** At full charge the thin ring's inner edge meets the icon's circular outer edge. */
    public static double approachRadius(float progress) {
        return radiusAtDistance(approachDistance(progress));
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

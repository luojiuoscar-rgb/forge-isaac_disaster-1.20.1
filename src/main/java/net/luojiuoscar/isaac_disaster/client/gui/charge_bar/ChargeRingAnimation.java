package net.luojiuoscar.isaac_disaster.client.gui.charge_bar;

/** Time-based full-charge flash; callers supply a monotonic clock in milliseconds. */
public final class ChargeRingAnimation {
    private static final long FLASH_PERIOD_MS = 400;
    private static final long WHITE_DURATION_MS = 200;

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

package net.luojiuoscar.isaac_disaster.client.gui.attribute_indicator;

import net.luojiuoscar.isaac_disaster.system.attribute_indicator.AttributeSnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Locale;

/** Presentation state driven by received snapshots, with caller-supplied monotonic time. */
public final class AttributeIndicatorState {
    private static final long FEEDBACK_MILLIS = 2000;
    private AttributeSnapshot snapshot;
    private final double[] displayedValues = new double[AttributeSnapshot.SIZE];
    private final String[] formattedValues = new String[AttributeSnapshot.SIZE];
    private final int[] directions = new int[AttributeSnapshot.SIZE];
    private final long[] expiresAt = new long[AttributeSnapshot.SIZE];

    public boolean hasSnapshot() {
        return snapshot != null;
    }

    public void accept(AttributeSnapshot next, boolean baseline, long nowMillis) {
        if (next == null || !next.isFinite()) return;
        if (snapshot == null || baseline) {
            Arrays.fill(directions, 0);
            Arrays.fill(expiresAt, 0);
        }
        for (int row = 0; row < AttributeSnapshot.SIZE; row++) {
            // Display movement speed in Isaac units; keep the snapshot in raw attribute units.
            double displayed = rounded(row == 0 ? next.movementSpeed() * 10.0 : next.value(row));
            if (snapshot != null && !baseline) {
                int change = Double.compare(displayed, displayedValues[row]);
                if (change != 0) {
                    directions[row] = change;
                    expiresAt[row] = nowMillis + FEEDBACK_MILLIS;
                }
            }
            displayedValues[row] = displayed;
            formattedValues[row] = String.format(Locale.ROOT, "%.2f", displayed);
        }
        snapshot = next;
    }

    public int direction(int row, long nowMillis) {
        return nowMillis < expiresAt[row] ? directions[row] : 0;
    }

    public String formattedValue(int row) {
        return formattedValues[row];
    }

    public static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", rounded(value));
    }

    private static double rounded(double value) {
        double rounded = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return rounded == 0 ? 0 : rounded;
    }

    public void clear() {
        snapshot = null;
        Arrays.fill(displayedValues, 0);
        Arrays.fill(formattedValues, null);
        Arrays.fill(directions, 0);
        Arrays.fill(expiresAt, 0);
    }
}

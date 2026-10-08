package net.luojiuoscar.isaac_disaster.system.attribute_indicator;

/** Character-panel values in HUD and wire order; no attack-type-specific conversion. */
public record AttributeSnapshot(double movementSpeed, double fireRate, double damage,
                                double range, double shotSpeed, double luck) {
    public static final int SIZE = 6;

    public double value(int row) {
        return switch (row) {
            case 0 -> movementSpeed;
            case 1 -> fireRate;
            case 2 -> damage;
            case 3 -> range;
            case 4 -> shotSpeed;
            case 5 -> luck;
            default -> throw new IndexOutOfBoundsException("Attribute row: " + row);
        };
    }

    public boolean isFinite() {
        for (int row = 0; row < SIZE; row++) {
            if (!Double.isFinite(value(row))) return false;
        }
        return true;
    }
}

package net.luojiuoscar.isaac_disaster.system.rockbottom;

/**
 * Resolves one Rock Bottom attribute value against its persisted maximum.
 */
public final class RockBottomValueHistory {
    private RockBottomValueHistory() {
    }

    public static double resolve(double current, Double historical) {
        return historical == null || current > historical ? current : historical;
    }
}

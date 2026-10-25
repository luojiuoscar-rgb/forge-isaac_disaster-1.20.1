package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core;

/** Shared half-open time intervals, measured from the authoritative server birth tick. */
public final class HolyLightTimeline {
    public static final int WARNING_TICKS = 35;
    public static final int FIRING_TICKS = 15;
    public static final int SCORCH_TICKS = 100;
    public static final int MAX_WARNING_TICKS = 700;
    public static final int MAX_DURATION = MAX_WARNING_TICKS + FIRING_TICKS + SCORCH_TICKS;

    private HolyLightTimeline() {
    }

    public static HolyLightPhase phase(double age, boolean skipWarning) {
        return phase(age, skipWarning ? 0 : WARNING_TICKS, true);
    }

    public static HolyLightPhase phase(double age, int warningTicks, boolean grounded) {
        if (!Double.isFinite(age)) return HolyLightPhase.EXPIRED;
        double elapsed = Math.max(0, age);
        int warning = Math.max(0, Math.min(MAX_WARNING_TICKS, warningTicks));
        if (elapsed < warning) return HolyLightPhase.WARNING;
        if (elapsed < warning + FIRING_TICKS) return HolyLightPhase.FIRING;
        if (elapsed < duration(warning, grounded)) return HolyLightPhase.SCORCH;
        return HolyLightPhase.EXPIRED;
    }

    public static double phaseAge(double age, boolean skipWarning) {
        return phaseAge(age, skipWarning ? 0 : WARNING_TICKS, true);
    }

    public static double phaseAge(double age, int warningTicks, boolean grounded) {
        double elapsed = Math.max(0, age);
        int warning = Math.max(0, Math.min(MAX_WARNING_TICKS, warningTicks));
        return switch (phase(age, warning, grounded)) {
            case WARNING -> elapsed;
            case FIRING -> elapsed - warning;
            case SCORCH -> elapsed - warning - FIRING_TICKS;
            case EXPIRED -> 0;
        };
    }

    public static int duration(boolean skipWarning) {
        return duration(skipWarning ? 0 : WARNING_TICKS, true);
    }

    public static int duration(int warningTicks, boolean grounded) {
        return Math.max(0, Math.min(MAX_WARNING_TICKS, warningTicks))
                + FIRING_TICKS + (grounded ? SCORCH_TICKS : 0);
    }

    /** Nonpositive or nonfinite multipliers use normal speed; all positive speeds stay bounded. */
    public static int warningTicks(double speed, boolean skipWarning) {
        if (skipWarning) return 0;
        double normalized = Double.isFinite(speed) && speed > 0 ? speed : 1;
        return (int) Math.max(1, Math.min(MAX_WARNING_TICKS, Math.ceil(WARNING_TICKS / normalized)));
    }
}

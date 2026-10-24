package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.client;

/** Frame-time brightness animation, separate from each effect's base RGB color. */
final class GroundEffectAppearance {
    private static final double PULSE_PERIOD_SECONDS = 0.5D;

    private GroundEffectAppearance() {
    }

    static float brightness(boolean hostile, double seconds) {
        if (!hostile) return 1.0F;
        // Smoothly raise and lower the base color's brightness between 75% and 150%.
        return 1.125F + 0.375F * (float) Math.cos(seconds * Math.PI * 2 / PULSE_PERIOD_SECONDS);
    }

    static float brightenChannel(int channel, float brightness) {
        // POSITION_COLOR stores unsigned bytes: values above 1 must saturate, never wrap around.
        return Math.min(1.0F, channel / 255.0F * brightness);
    }
}

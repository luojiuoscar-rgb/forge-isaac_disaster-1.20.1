package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight;

import net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.server.HolyLightRuntime;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Entry point for a fixed-position strike attributed to a living caster in the same world. */
public final class HolyLight {
    public static final double DEFAULT_RADIUS = 0.8;

    private HolyLight() {
    }

    public static boolean strike(ServerLevel level, LivingEntity caster, Vec3 position,
                                 float damage, boolean skipWarning) {
        return strike(level, caster, position, damage, DEFAULT_RADIUS, skipWarning);
    }

    /**
     * Returns false for invalid input, unloaded chunks or an obstructed starting position.
     * Negative finite damage becomes zero; positive finite radii above 16 are capped at 16.
     * Skipping the warning settles damage before this call returns.
     */
    public static boolean strike(ServerLevel level, LivingEntity caster, Vec3 position,
                                 float damage, double radius, boolean skipWarning) {
        return strike(level, caster, position, damage, radius, skipWarning, 1);
    }

    public static boolean strike(ServerLevel level, LivingEntity caster, Vec3 position,
                                 float damage, boolean skipWarning, double warningSpeed) {
        return strike(level, caster, position, damage, DEFAULT_RADIUS, skipWarning, warningSpeed);
    }

    /**
     * Multiplier 1 keeps the 35-tick warning, 2 doubles its speed and 0.5 halves it.
     * Invalid multipliers use 1; resulting warnings are bounded to 1..700 ticks.
     * Airborne positions stay airborne unless collision support is within one block below.
     */
    public static boolean strike(ServerLevel level, LivingEntity caster, Vec3 position,
                                 float damage, double radius, boolean skipWarning, double warningSpeed) {
        return level != null && HolyLightRuntime.INSTANCE.manager(level)
                .spawn(level, caster, position, damage, radius, skipWarning, warningSpeed);
    }
}

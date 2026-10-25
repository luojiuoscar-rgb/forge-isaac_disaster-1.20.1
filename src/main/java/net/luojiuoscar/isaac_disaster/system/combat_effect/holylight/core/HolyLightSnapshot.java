package net.luojiuoscar.isaac_disaster.system.combat_effect.holylight.core;

import net.minecraft.world.phys.Vec3;

/** Immutable visual description. Damage and caster references stay exclusively on the server. */
public record HolyLightSnapshot(long id, Vec3 position, double radius, double beamHeight,
                                long startTick, int warningTicks, boolean grounded, long seed) {
    public boolean skipWarning() {
        return warningTicks == 0;
    }

    public HolyLightPhase phase(double age) {
        return HolyLightTimeline.phase(age, warningTicks, grounded);
    }

    public double phaseAge(double age) {
        return HolyLightTimeline.phaseAge(age, warningTicks, grounded);
    }

    public int duration() {
        return HolyLightTimeline.duration(warningTicks, grounded);
    }

    public boolean valid() {
        return id > 0 && position != null
                && Double.isFinite(position.x) && Double.isFinite(position.y) && Double.isFinite(position.z)
                && Double.isFinite(radius) && radius > 0 && radius <= 16
                && Double.isFinite(beamHeight) && beamHeight > 0 && beamHeight <= 16
                && startTick >= 0 && warningTicks >= 0 && warningTicks <= HolyLightTimeline.MAX_WARNING_TICKS;
    }
}

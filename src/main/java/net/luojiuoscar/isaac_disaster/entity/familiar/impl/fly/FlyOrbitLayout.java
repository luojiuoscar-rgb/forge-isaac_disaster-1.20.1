package net.luojiuoscar.isaac_disaster.entity.familiar.impl.fly;

import net.minecraft.world.phys.Vec3;

/** Shared ring geometry, independent of entity update order and player view rotation. */
final class FlyOrbitLayout {
    static final double MAX_SPEED = 0.6;
    static final double ATTACK_SPEED = 0.5;
    static final double IDLE_SPEED = 0.3;

    private FlyOrbitLayout() { }

    static double radius(double ownerWidth, int count) {
        int capacity = Math.max(64, count);
        // Wings extend radially when facing along the orbit: reserve 0.25 radial blocks,
        // plus the 0.125 tangential footprint and 0.05 clear gap at the inside wing tips.
        double capacityRadius = 0.25 + 0.175 / (2 * Math.tan(Math.PI / capacity));
        return Math.max(Math.max(2.1 * ownerWidth / 0.6, capacityRadius), ownerWidth / 2 + 0.5);
    }

    static double advance(double phase, double radius) {
        return (phase + 2 * Math.asin(IDLE_SPEED / (2 * radius))) % (Math.PI * 2);
    }

    static Vec3 position(Vec3 center, double radius, double flyHeight, int slot, int count, double phase) {
        double angle = phase + Math.PI * 2 * slot / Math.max(1, count);
        return center.add(radius * Math.cos(angle), -flyHeight * 0.5, radius * Math.sin(angle));
    }
}

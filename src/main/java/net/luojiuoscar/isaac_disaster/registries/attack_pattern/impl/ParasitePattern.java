package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Generates the two tears perpendicular to the parent's firing direction. */
public final class ParasitePattern implements AttackPattern {
    private static final Vec3 WORLD_UP = new Vec3(0.0, 1.0, 0.0);
    private static final Vec3 FALLBACK_AXIS = new Vec3(1.0, 0.0, 0.0);
    private static final double EPSILON = 1.0E-8;

    @Override
    public List<AttackContext> generate(AttackPatternContext context) {
        AttackContext reference = context.getReferenceContext();
        Vec3 forward = Vec3.directionFromRotation(reference.getXRot(), reference.getYRot()).normalize();
        Vec3 side = forward.cross(WORLD_UP);
        if (side.lengthSqr() < EPSILON) {
            side = forward.cross(FALLBACK_AXIS);
        }
        side = side.normalize();

        List<AttackContext> children = new ArrayList<>(2);
        AttackContext left = reference.copy();
        left.setDirection(side);
        children.add(left);

        AttackContext right = reference.copy();
        right.setDirection(side.scale(-1.0));
        children.add(right);
        return children;
    }
}

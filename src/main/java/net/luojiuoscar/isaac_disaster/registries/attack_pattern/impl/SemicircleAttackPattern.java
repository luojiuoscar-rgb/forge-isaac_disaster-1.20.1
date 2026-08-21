package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class SemicircleAttackPattern implements AttackPattern {
    private static final Vec3 WORLD_UP = new Vec3(0.0, 1.0, 0.0);
    private static final Vec3 FALLBACK_AXIS = new Vec3(1.0, 0.0, 0.0);
    private static final double EPSILON = 1.0E-8;

    @Override
    public List<AttackContext> generate(AttackPatternContext context) {
        int count = context.getBulletCount();
        if (count <= 0) {
            return new ArrayList<>();
        }

        AttackContext reference = context.getReferenceContext();
        Vec3 forward = Vec3.directionFromRotation(reference.getXRot(), reference.getYRot()).normalize();
        Vec3 side = forward.cross(WORLD_UP);
        if (side.lengthSqr() < EPSILON) {
            side = forward.cross(FALLBACK_AXIS);
        }
        side = side.normalize();

        List<AttackContext> result = new ArrayList<>(count);
        if (count == 1) {
            AttackContext child = reference.copy();
            child.setDirection(forward);
            result.add(child);
            return result;
        }

        for (int index = 0; index < count; index++) {
            double angle = -Math.PI / 2.0 + Math.PI * index / (count - 1);
            Vec3 direction = forward.scale(Math.cos(angle))
                    .add(side.scale(Math.sin(angle)))
                    .normalize();

            AttackContext child = reference.copy();
            child.setDirection(direction);
            result.add(child);
        }
        return result;
    }
}

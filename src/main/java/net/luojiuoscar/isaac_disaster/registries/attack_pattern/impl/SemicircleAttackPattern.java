package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AbstractAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class SemicircleAttackPattern extends AbstractAttackPattern {
    @Override
    public List<AttackContext> generate(AttackPatternContext context) {
        int count = context.getBulletCount();
        if (count <= 0) {
            return new ArrayList<>();
        }

        AttackContext reference = context.getMainBulletContext();
        Vec3 forward = mainAxis(reference);
        Vec3 side = lateralAxis(forward);

        List<AttackContext> result = new ArrayList<>(count);
        if (count == 1) {
            result.add(copyWithMainAxis(reference, forward));
            return result;
        }

        for (int index = 0; index < count; index++) {
            double angle = -Math.PI / 2.0 + Math.PI * index / (count - 1);
            Vec3 direction = forward.scale(Math.cos(angle))
                    .add(side.scale(Math.sin(angle)))
                    .normalize();

            result.add(copyWithMainAxis(reference, direction));
        }
        return result;
    }
}

package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AbstractAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Random directions in the plane spanned by the view and its stable lateral axis. */
public final class ViewPlaneRandomAttackPattern extends AbstractAttackPattern {
    @Override
    public List<AttackContext> generate(AttackPatternContext context) {
        int count = context.getBulletCount();
        if (count <= 0) return List.of();
        AttackContext reference = context.getMainBulletContext();
        Vec3 axis = reference.getMainAxis();
        List<AttackContext> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(copyWithMainAxis(reference, randomDirection(axis, reference.getOwner().getRandom())));
        }
        return result;
    }

    static Vec3 randomDirection(Vec3 axis, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2;
        return axis.scale(Math.cos(angle)).add(GeometryHelper.lateralAxis(axis).scale(Math.sin(angle)));
    }
}

package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AbstractAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class MonstrosLungAttackPattern extends AbstractAttackPattern {
    private static final double COS_HALF_ANGLE = Math.cos(Math.toRadians(15.0));

    @Override
    public List<AttackContext> generate(AttackPatternContext context) {
        if (context.getBulletCount() <= 0) return List.of();

        AttackContext reference = context.getMainBulletContext();
        Vec3 axis = reference.getMainAxis();
        Vec3 helper = Math.abs(axis.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 tangent = axis.cross(helper).normalize();
        Vec3 bitangent = axis.cross(tangent).normalize();
        RandomSource random = reference.getOwner().getRandom();
        List<AttackContext> contexts = new ArrayList<>(context.getBulletCount());
        for (int i = 0; i < context.getBulletCount(); i++) {
            // Uniform cos(theta) gives uniform solid angle, including vertical aim.
            double cosine = 1.0 - random.nextDouble() * (1.0 - COS_HALF_ANGLE);
            double sine = Math.sqrt(Math.max(0.0, 1.0 - cosine * cosine));
            double azimuth = random.nextDouble() * Math.PI * 2.0;
            Vec3 direction = axis.scale(cosine)
                    .add(tangent.scale(sine * Math.cos(azimuth)))
                    .add(bitangent.scale(sine * Math.sin(azimuth)));
            AttackContext child = copyWithMainAxis(reference, direction);
            child.setBulletScale(reference.getBulletScale(), true);
            contexts.add(child);
        }
        return contexts;
    }
}

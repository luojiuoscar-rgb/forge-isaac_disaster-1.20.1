package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AbstractAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class SphericalRandomAttackPattern extends AbstractAttackPattern {
    @Override
    public List<AttackContext> generate(AttackPatternContext context) {
        int count = context.getBulletCount();
        if (count <= 0) return List.of();

        AttackContext reference = context.getMainBulletContext();
        RandomSource random = reference.getOwner().getRandom();
        List<AttackContext> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(copyWithMainAxis(reference, randomDirection(random)));
        }
        return result;
    }

    static Vec3 randomDirection(RandomSource random) {
        double y = random.nextDouble() * 2.0 - 1.0;
        double azimuth = random.nextDouble() * Math.PI * 2.0;
        double horizontal = Math.sqrt(1.0 - y * y);
        return new Vec3(horizontal * Math.cos(azimuth), y, horizontal * Math.sin(azimuth));
    }
}

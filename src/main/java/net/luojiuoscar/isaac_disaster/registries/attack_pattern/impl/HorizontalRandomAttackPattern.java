package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AbstractAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Generates independent random directions on the horizontal plane. */
public final class HorizontalRandomAttackPattern extends AbstractAttackPattern {
    @Override
    public List<AttackContext> generate(AttackPatternContext context) {
        int count = context.getBulletCount();
        if (count <= 0) return List.of();

        AttackContext reference = context.getMainBulletContext();
        List<AttackContext> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle = reference.getOwner().getRandom().nextDouble() * Math.PI * 2.0;
            result.add(copyWithMainAxis(reference, new Vec3(Math.cos(angle), 0.0, Math.sin(angle))));
        }
        return result;
    }
}

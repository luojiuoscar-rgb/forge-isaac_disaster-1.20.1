package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AbstractAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Generates the two tears perpendicular to the parent's firing direction. */
public final class ParasitePattern extends AbstractAttackPattern {
    @Override
    public List<AttackContext> generate(AttackPatternContext context) {
        AttackContext reference = context.getMainBulletContext();
        Vec3 forward = mainAxis(reference);
        Vec3 side = lateralAxis(forward);

        List<AttackContext> children = new ArrayList<>(2);
        children.add(copyWithMainAxis(reference, side));

        children.add(copyWithMainAxis(reference, side.scale(-1.0)));
        return children;
    }
}

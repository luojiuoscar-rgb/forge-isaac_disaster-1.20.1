package net.luojiuoscar.isaac_disaster.registries.attack_pattern;

import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.phys.Vec3;

/** Base implementation for Context cloning used by concrete attack patterns. */
public abstract class AbstractAttackPattern implements AttackPattern {
    protected Vec3 mainAxis(AttackContext context) {
        return context.getMainAxis();
    }

    protected AttackContext copyWithMainAxis(AttackContext reference, Vec3 axis) {
        return reference.toBuilder().mainAxis(axis).build();
    }

    protected Vec3 lateralAxis(Vec3 axis) {
        return GeometryHelper.lateralAxis(axis);
    }
}

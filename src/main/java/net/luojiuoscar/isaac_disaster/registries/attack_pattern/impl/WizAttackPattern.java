package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;

import java.util.ArrayList;
import java.util.List;

public final class WizAttackPattern {
    private WizAttackPattern() {
    }

    public static List<AttackContext> rotateContexts(List<AttackContext> baseContexts, float rotationDegrees) {
        List<AttackContext> contexts = new ArrayList<>(baseContexts.size());
        for (AttackContext context : baseContexts) {
            contexts.add(context.toBuilder().mainAxis(GeometryHelper.rotateAroundAxis(
                    context.getMainAxis(), new net.minecraft.world.phys.Vec3(0, 1, 0), -Math.toRadians(rotationDegrees))).build());
        }
        return contexts;
    }
}

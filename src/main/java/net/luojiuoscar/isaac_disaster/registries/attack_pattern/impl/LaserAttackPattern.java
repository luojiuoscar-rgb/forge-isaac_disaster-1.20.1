package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AbstractAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class LaserAttackPattern extends AbstractAttackPattern {
    private static final float ANGLE_INTERVAL = 8.0f;

    @Override
    public List<AttackContext> generate(AttackPatternContext context) {
        int bulletCount = context.getBulletCount();
        if (bulletCount <= 0) {
            return new ArrayList<>();
        }

        AttackContext mainBulletContext = context.getMainBulletContext();
        float curAngle = -ANGLE_INTERVAL * (bulletCount - 1) / 2.0f;
        Vec3 mainAxis = mainAxis(mainBulletContext);

        List<AttackContext> children = new ArrayList<>(bulletCount);
        for (int i = 0; i < bulletCount; i++) {
            children.add(copyWithMainAxis(mainBulletContext,
                    GeometryHelper.rotateAroundAxis(mainAxis, new Vec3(0, 1, 0), -Math.toRadians(curAngle))));
            curAngle += ANGLE_INTERVAL;
        }
        return children;
    }
}

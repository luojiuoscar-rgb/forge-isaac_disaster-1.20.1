package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AbstractAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class BulletAttackPattern extends AbstractAttackPattern {
    private static final double SIDE_OFFSET = 0.25;

    @Override
    public List<AttackContext> generate(AttackPatternContext context) {
        int bulletCount = context.getBulletCount();
        if (bulletCount <= 0) {
            return new ArrayList<>();
        }

        AttackContext mainBulletContext = context.getMainBulletContext();
        if (bulletCount == 2) {
            Vec3 mainAxis = mainAxis(mainBulletContext);
            Vec3 sideAxis = lateralAxis(mainAxis);

            List<AttackContext> children = new ArrayList<>(2);
            AttackContext right = mainBulletContext.toBuilder()
                    .position(mainBulletContext.getPos().add(sideAxis.scale(SIDE_OFFSET))).build();
            children.add(right);

            AttackContext left = mainBulletContext.toBuilder()
                    .position(mainBulletContext.getPos().add(sideAxis.scale(-SIDE_OFFSET))).build();
            children.add(left);
            return children;
        }

        float angleInterval = Math.max(11 - bulletCount, 5) * 2;
        float curAngle = -angleInterval * (bulletCount - 1) / 2.0f;
        Vec3 mainAxis = mainAxis(mainBulletContext);

        List<AttackContext> children = new ArrayList<>(bulletCount);
        for (int i = 0; i < bulletCount; i++) {
            children.add(copyWithMainAxis(mainBulletContext,
                    GeometryHelper.rotateAroundAxis(mainAxis, new Vec3(0, 1, 0), -Math.toRadians(curAngle))));
            curAngle += angleInterval;
        }
        return children;
    }
}

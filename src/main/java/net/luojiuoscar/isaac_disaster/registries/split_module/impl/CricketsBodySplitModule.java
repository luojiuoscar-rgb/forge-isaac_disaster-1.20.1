package net.luojiuoscar.isaac_disaster.registries.split_module.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.RingAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.BulletSourceType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.BrimstoneAttack;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModule;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModulePriority;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Cricket's Body's four-way tear split. */
public final class CricketsBodySplitModule extends SplitModule {
    private static final RingAttackPattern PATTERN = new RingAttackPattern();
    private static final Vec3 WORLD_UP = new Vec3(0.0, 1.0, 0.0);

    public CricketsBodySplitModule() {
        super(PATTERN, ModAttackTypes.BULLET.get());
    }

    @Override
    public boolean canTrigger(SplitContext context) {
        SplitTriggerType type = context.getTriggerType();
        if (type == SplitTriggerType.ENTITY || type == SplitTriggerType.END_OF_LIFE) {
            return isAllowedBrimstoneSequence(context);
        }
        if (type != SplitTriggerType.BLOCK) return false;
        return (context.getParent().getSourceType() == BulletSourceType.LASER
                || context.getParent().getSourceType() == BulletSourceType.BRIMSTONE)
                && context.getTriggerCounts().getBlockHits() == 1
                && isAllowedBrimstoneSequence(context);
    }

    private boolean isAllowedBrimstoneSequence(SplitContext context) {
        if (context.getParent().getSourceType() != BulletSourceType.BRIMSTONE) return true;
        if (!(context.getParent() instanceof LaserAttack.LaserProjectile laser)) return false;
        int index = laser.getAttackSequenceIndex();
        return index > 0 && index % 3 == 0;
    }

    @Override
    public int getBulletCount() {
        return 4;
    }

    @Override
    public List<AttackContext> generate(SplitContext context) {
        if (context.getTriggerType() == SplitTriggerType.BLOCK) {
            return generateBlockSplit(context);
        }
        return generateLegacySplit(context);
    }

    private List<AttackContext> generateLegacySplit(SplitContext context) {
        AttackContext reference = context.getReferenceContext();
        double angle = Math.toRadians(Objects.requireNonNull(context.getParent().getOwner(), "owner")
                .getRandom().nextDouble() * 45.0);
        Vec3 direction = Vec3.directionFromRotation(reference.getXRot(), reference.getYRot());
        reference.setDirection(AttackType.rotateAroundAxis(direction, WORLD_UP, angle));

        List<AttackContext> children = PATTERN.generate(new AttackPatternContext(reference, getBulletCount()));
        for (int i = 0; i < children.size(); i++) {
            AttackContext child = children.get(i);
            children.set(i, child.toBuilder()
                    .damage(reference.getDamage() * 0.5)
                    .range(context.getParent().getRange() * 0.5)
                    .build());
        }
        return children;
    }

    private List<AttackContext> generateBlockSplit(SplitContext context) {
        AttackContext reference = context.getReferenceContext();
        Vec3 planeNormal = resolveImpactPlaneNormal(context);
        double angle = Math.toRadians(Objects.requireNonNull(context.getParent().getOwner(), "owner")
                .getRandom().nextDouble() * 45.0);
        Vec3 incomingDirection = Vec3.directionFromRotation(reference.getXRot(), reference.getYRot());

        List<Vec3> directions = buildPlaneAlignedSpread(incomingDirection, planeNormal, getBulletCount(), angle);
        List<AttackContext> children = new ArrayList<>(directions.size());
        for (Vec3 direction : directions) {
            AttackContext child = reference.copy();
            child.setDirection(direction);
            children.add(child);
        }

        for (int i = 0; i < children.size(); i++) {
            AttackContext child = children.get(i);
            children.set(i, child.toBuilder()
                    .damage(reference.getDamage() * 0.5)
                    .range(context.getParent().getRange() * 0.5)
                    .build());
        }
        return children;
    }

    @Override
    public void applyInheritance(SplitContext context, List<AttackContext> children) {
        for (int i = 0; i < children.size(); i++) {
            AttackContext child = children.get(i);
            children.set(i, child.toBuilder()
                    .splitSequence(context.getSequence().copyForChild(context, child, false))
                    .build());
        }
    }

    @Override
    public boolean shouldInherit(SplitContext context, AttackContext childContext) {
        return true;
    }

    @Override
    public double getPriority() {
        return SplitModulePriority.CRICKETS_BODY.priority();
    }

    static Vec3 alignDirectionToImpactPlane(Vec3 direction, Vec3 planeNormal) {
        Vec3 normal = normalizeOrFallback(planeNormal);
        Vec3 projected = direction.subtract(normal.scale(direction.dot(normal)));
        if (projected.lengthSqr() < EPSILON) {
            projected = fallbackTangent(normal);
        }
        return projected.normalize();
    }

    static List<Vec3> buildPlaneAlignedSpread(Vec3 direction, Vec3 planeNormal, int bulletCount, double spinRadians) {
        if (bulletCount <= 0) {
            return List.of();
        }

        Vec3 normal = normalizeOrFallback(planeNormal);
        Vec3 forward = alignDirectionToImpactPlane(direction, normal);
        if (bulletCount == 1) {
            return List.of(forward);
        }

        Vec3 side = normal.cross(forward);
        if (side.lengthSqr() < EPSILON) {
            side = fallbackTangent(normal);
        }
        side = side.normalize();

        List<Vec3> directions = new ArrayList<>(bulletCount);
        for (int index = 0; index < bulletCount; index++) {
            double angle = spinRadians + (2.0 * Math.PI * index / bulletCount);
            Vec3 spread = forward.scale(Math.cos(angle)).add(side.scale(Math.sin(angle)));
            directions.add(spread.normalize());
        }
        return directions;
    }

    private static Vec3 resolveImpactPlaneNormal(SplitContext context) {
        BlockHitResult blockHit = context.getParent().getLastBlockHit();
        if (blockHit != null) {
            return Vec3.atLowerCornerOf(blockHit.getDirection().getNormal()).normalize();
        }
        return WORLD_UP;
    }

    private static Vec3 normalizeOrFallback(Vec3 vector) {
        if (vector == null || !Double.isFinite(vector.x) || !Double.isFinite(vector.y)
                || !Double.isFinite(vector.z) || vector.lengthSqr() < EPSILON) {
            return WORLD_UP;
        }
        return vector.normalize();
    }

    private static Vec3 fallbackTangent(Vec3 planeNormal) {
        Vec3 up = Math.abs(planeNormal.y) < 0.999 ? WORLD_UP : WORLD_EAST;
        Vec3 tangent = planeNormal.cross(up);
        if (tangent.lengthSqr() < EPSILON) {
            tangent = planeNormal.cross(WORLD_FORWARD);
        }
        if (tangent.lengthSqr() < EPSILON) {
            tangent = WORLD_EAST;
        }
        return tangent.normalize();
    }

    private static final Vec3 WORLD_EAST = new Vec3(1.0, 0.0, 0.0);
    private static final Vec3 WORLD_FORWARD = new Vec3(0.0, 0.0, 1.0);
    private static final double EPSILON = 1.0E-8;
}

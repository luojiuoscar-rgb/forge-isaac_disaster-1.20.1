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
import net.minecraft.world.phys.Vec3;

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
        AttackContext reference = context.getReferenceContext();
        double angle = Math.toRadians(Objects.requireNonNull(context.getParent().getOwner(), "owner")
                .getRandom().nextDouble() * 45.0);
        Vec3 direction = Vec3.directionFromRotation(reference.getXRot(), reference.getYRot());
        reference.setDirection(AttackType.rotateAroundAxis(direction, WORLD_UP, angle));

        List<AttackContext> children = PATTERN.generate(new AttackPatternContext(reference, getBulletCount()));
        for (AttackContext child : children) {
            child.setDamage(reference.getDamage() * 0.5);
            child.setBulletRange(context.getParent().getRange() * 0.5);
        }
        return children;
    }

    @Override
    public void applyInheritance(SplitContext context, List<AttackContext> children) {
        for (AttackContext child : children) {
            child.setSplitSequence(context.getSequence().copyForChild(context, child, false));
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
}

package net.luojiuoscar.isaac_disaster.registries.split_module.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.ParasitePattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.BulletSourceType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModule;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModulePriority;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;

import java.util.List;

/** The Parasite's recursive perpendicular tear split. */
public final class ParasiteSplitModule extends SplitModule {
    private static final ParasitePattern PATTERN = new ParasitePattern();

    public ParasiteSplitModule() {
        super(PATTERN, ModAttackTypes.BULLET.get());
    }

    @Override
    public boolean canTrigger(SplitContext context) {
        SplitTriggerType type = context.getTriggerType();
        if (type == SplitTriggerType.END_OF_LIFE) return false;
        if ((context.getParent().getSourceType() == BulletSourceType.LASER
                || context.getParent().getSourceType() == BulletSourceType.BRIMSTONE)
                && context.getModuleTriggerCount() > 0) return false;
        if (type == SplitTriggerType.BLOCK && context.getTriggerCounts().getBlockHits() != 1) return false;

        return true;
    }

    @Override
    public List<AttackContext> generate(SplitContext context) {
        List<AttackContext> children = getPattern().generate(
                new AttackPatternContext(context.getReferenceContext(), getBulletCount()));
        AttackContext reference = context.getReferenceContext();
        for (AttackContext child : children) {
            child.setDamage(reference.getDamage() * 0.5);
            child.setBulletRange(context.getParent().getRange() * 0.5);
        }
        return children;
    }

    @Override
    public void applyInheritance(SplitContext context, List<AttackContext> children) {
        for (AttackContext child : children) {
            child.setSplitSequence(context.getSequence().copyForChild(context, child, true));
        }
    }

    @Override
    public boolean shouldInherit(SplitContext context, AttackContext childContext) {
        if (context.getParent().getSourceType() == BulletSourceType.LASER
                || context.getParent().getSourceType() == BulletSourceType.BRIMSTONE) return false;
        return childContext.getDamage() >= 1.0F;
    }

    @Override
    public int getBulletCount() {
        return 2;
    }

    @Override
    public double getPriority() {
        return SplitModulePriority.THE_PARASITE.priority();
    }

    @Override
    public AttackType resolveChildAttackType(SplitContext context) {
        if (context.getParent().getSourceType() == BulletSourceType.BRIMSTONE) {
            return ModAttackTypes.BRIMSTONE.get();
        }
        if (context.getParent().getSourceType() == BulletSourceType.LASER) {
            return ModAttackTypes.LASER.get();
        }
        return super.resolveChildAttackType(context);
    }
}

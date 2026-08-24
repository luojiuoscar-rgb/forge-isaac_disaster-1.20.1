package net.luojiuoscar.isaac_disaster.registries.split_module.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.ParasitePattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModule;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModulePriority;

import java.util.List;

/** The Parasite's recursive perpendicular tear split. */
public final class ParasiteSplitModule extends SplitModule {
    private static final ParasitePattern PATTERN = new ParasitePattern();

    public ParasiteSplitModule() {
        super(PATTERN, ModAttackTypes.BULLET.get());
    }

    @Override
    public boolean canTrigger(SplitContext context) {
        return true;
    }

    @Override
    public List<AttackContext> generate(SplitContext context) {
        List<AttackContext> children = getPattern().generate(
                new AttackPatternContext(context.getReferenceContext(), getBulletCount()));
        AttackContext reference = context.getReferenceContext();
        for (AttackContext child : children) {
            child.setDamage(reference.getDamage() * 0.5);
            int parentLifetime = context.getParent().getTotalLifeTick();
            if (parentLifetime > 0) {
                child.setBulletLifetime(Math.max(1, parentLifetime / 2));
            }
        }
        return children;
    }

    @Override
    public void applyInheritance(SplitContext context, List<AttackContext> children) {
        for (AttackContext child : children) {
            child.setSplitSequence(context.getSequence().copyForChild(context, child));
        }
    }

    @Override
    public boolean shouldInherit(SplitContext context, AttackContext childContext) {
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
}

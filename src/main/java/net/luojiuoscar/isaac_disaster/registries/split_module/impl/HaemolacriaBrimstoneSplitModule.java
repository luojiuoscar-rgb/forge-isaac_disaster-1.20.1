package net.luojiuoscar.isaac_disaster.registries.split_module.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.SphericalRandomAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModule;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModulePriority;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Haemolacria split that delegates each child direction to Brimstone. */
public final class HaemolacriaBrimstoneSplitModule extends SplitModule {
    private static final SphericalRandomAttackPattern PATTERN = new SphericalRandomAttackPattern();

    public HaemolacriaBrimstoneSplitModule() {
        super(PATTERN, ModAttackTypes.BRIMSTONE.get());
    }

    @Override
    public boolean canTrigger(SplitContext context) {
        if (!ModAttackTypes.BULLET.getId().equals(context.getParent().getRootTypeId())
                || context.getModuleTriggerCount() > 0
                || !HaemolacriaSplitModule.ownsAttackType(context, ModAttackTypes.BRIMSTONE.getId())) return false;
        return HaemolacriaSplitModule.canBurstOn(context.getTriggerType(), context.getParent().isPiercing(),
                context.getParent().isSpectral());
    }

    @Override
    public int getBulletCount() {
        return ThreadLocalRandom.current().nextInt(3, 8);
    }

    @Override
    public List<AttackContext> generate(SplitContext context) {
        AttackContext reference = context.getReferenceContext();
        List<AttackContext> children = PATTERN.generate(new AttackPatternContext(reference, getBulletCount()));
        children.replaceAll(child -> child.toBuilder()
                .damage(reference.getDamage() * HaemolacriaSplitModule.damageMultiplier(reference.getOwner().getRandom()))
                .build());
        return children;
    }

    @Override
    public void applyInheritance(SplitContext context, List<AttackContext> children) {
        children.replaceAll(child -> child.toBuilder()
                .splitSequence(context.getSequence().copyForChild(context, child, true))
                .build());
    }

    @Override
    public boolean shouldInherit(SplitContext context, AttackContext childContext) {
        return false;
    }

    @Override
    public boolean shouldPlayChildSound(SplitContext context) {
        return true;
    }

    @Override
    public double getPriority() {
        return SplitModulePriority.HAEMOLACRIA_BRIMSTONE.priority();
    }
}

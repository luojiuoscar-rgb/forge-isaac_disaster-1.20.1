package net.luojiuoscar.isaac_disaster.registries.split_module.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.SphericalRandomAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModule;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModulePriority;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class HaemolacriaSplitModule extends SplitModule {
    private static final SphericalRandomAttackPattern PATTERN = new SphericalRandomAttackPattern();

    public HaemolacriaSplitModule() {
        super(PATTERN, ModAttackTypes.BULLET.get());
    }

    @Override
    public boolean canTrigger(SplitContext context) {
        if (!ModAttackTypes.BULLET.getId().equals(context.getParent().getRootTypeId())
                || context.getModuleTriggerCount() > 0) return false;
        return canBurstOn(context.getTriggerType(), context.getParent().isPiercing(),
                context.getParent().isSpectral());
    }

    static boolean canBurstOn(SplitTriggerType triggerType, boolean piercing, boolean spectral) {
        return switch (triggerType) {
            case ENTITY -> !piercing;
            case BLOCK -> !spectral;
            case END_OF_LIFE -> true;
        };
    }

    @Override
    public int getBulletCount() {
        return rollBulletCount();
    }

    static int rollBulletCount() {
        return ThreadLocalRandom.current().nextInt(6, 12);
    }

    static double damageMultiplier(RandomSource random) {
        return 0.5 + random.nextDouble() / 3.0;
    }

    @Override
    public List<AttackContext> generate(SplitContext context) {
        AttackContext reference = context.getReferenceContext();
        List<AttackContext> children = PATTERN.generate(new AttackPatternContext(reference, getBulletCount()));
        children.replaceAll(child -> child.toBuilder()
                .damage(reference.getDamage() * damageMultiplier(reference.getOwner().getRandom()))
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
    public double getPriority() {
        return SplitModulePriority.HAEMOLACRIA.priority();
    }
}

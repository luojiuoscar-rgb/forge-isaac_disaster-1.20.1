package net.luojiuoscar.isaac_disaster.registries.split_module.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.HorizontalRandomAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.BulletSourceType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModule;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModulePriority;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** Compound Fracture's one-time random horizontal tear split. */
public class CompoundFractureSplitModule extends SplitModule {
    private static final HorizontalRandomAttackPattern PATTERN = new HorizontalRandomAttackPattern();

    public CompoundFractureSplitModule() {
        super(PATTERN, ModAttackTypes.BULLET.get());
    }

    @Override
    public boolean canTrigger(SplitContext context) {
        if (context.getParent().getSourceType() == BulletSourceType.LASER
                || context.getParent().getSourceType() == BulletSourceType.BRIMSTONE) return false;
        if (context.getTriggerType() != SplitTriggerType.ENTITY
                && context.getTriggerType() != SplitTriggerType.BLOCK) return false;
        return context.getModuleTriggerCount() == 0;
    }

    @Override
    public int getBulletCount() {
        return ThreadLocalRandom.current().nextInt(1, 4);
    }

    @Override
    public List<AttackContext> generate(SplitContext context) {
        AttackContext reference = context.getReferenceContext();
        List<AttackContext> children = PATTERN.generate(new AttackPatternContext(reference, getBulletCount()));
        children.replaceAll(attackContext -> {
            Set<ResourceLocation> visualIds = new HashSet<>(attackContext.getVisualIds());
            visualIds.add(ModBulletVisuals.COMPOUND_FRACTURE_BONE_TEAR.getId());
            return attackContext.toBuilder()
                    .damage(reference.getDamage() * 0.5)
                    .visuals(visualIds)
                    .build();
        });
        return children;
    }

    @Override
    public void applyInheritance(SplitContext context, List<AttackContext> children) {
        for (int i = 0; i < children.size(); i++) {
            AttackContext child = children.get(i);
            children.set(i, child.toBuilder()
                    .splitSequence(context.getSequence().copyForChild(context, child, true))
                    .build());
        }
    }

    @Override
    public boolean shouldInherit(SplitContext context, AttackContext childContext) {
        return false;
    }

    @Override
    public double getPriority() {
        return SplitModulePriority.COMPOUND_FRACTURE.priority();
    }

}

package net.luojiuoscar.isaac_disaster.registries.split_module.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.SphericalRandomAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModule;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitModulePriority;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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
                || context.getModuleTriggerCount() > 0
                || ownsAttackType(context, ModAttackTypes.BRIMSTONE.getId())) return false;
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

    static boolean ownsAttackType(SplitContext context, ResourceLocation attackTypeId) {
        LivingEntity owner = context.getParent().getOwner();
        if (!(owner instanceof Player player)) return false;
        return player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY)
                .map(ability -> ability.getAttackTypes().getOrDefault(attackTypeId, 0) > 0)
                .orElse(false);
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
    public AttackType resolveChildAttackType(SplitContext context) {
        if (ownsAttackType(context, ModAttackTypes.LASER.getId())) {
            return ModAttackTypes.LASER.get();
        }
        return super.resolveChildAttackType(context);
    }

    @Override
    public boolean shouldPlayChildSound(SplitContext context) {
        return ownsAttackType(context, ModAttackTypes.LASER.getId());
    }

    @Override
    public double getPriority() {
        return SplitModulePriority.HAEMOLACRIA.priority();
    }
}

package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectPriority;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraftforge.registries.RegistryManager;

import java.util.List;

public final class FireMindAttachment extends AttackPreparationEffect {
    private static final List<SimpleTrigger> TRIGGERS = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_AFTER, ModExecutableEffects.FIRE_MIND_IMPACT),
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_BLOCK, ModExecutableEffects.FIRE_MIND_IMPACT)
    );

    @Override
    public double getPriority() {
        return ExecutableEffectPriority.LOWEST.priority;
    }

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        attackContext.addSimpleTriggers(TRIGGERS);
        if (attackContext.isLaserAttack()) return;
        var registry = RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        if (registry == null) return;
        var fire = registry.getValue(ModBulletColors.FIRE_MIND.getId());
        var current = attackContext.getColorRl() == null ? null : registry.getValue(attackContext.getColorRl());
        if (fire != null && (current == null || current.priority() < fire.priority())) {
            attackContext.setColorRl(ModBulletColors.FIRE_MIND.getId());
        }
    }
}

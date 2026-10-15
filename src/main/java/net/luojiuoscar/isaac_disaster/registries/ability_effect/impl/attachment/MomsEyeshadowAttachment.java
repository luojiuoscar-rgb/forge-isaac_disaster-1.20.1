package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.MomsEyeshadow.getTriggerChance;

public final class MomsEyeshadowAttachment extends AttackPreparationEffect {
    private static final List<SimpleTrigger> bullet_triggers = List.of(
            new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.MOMS_EYESHADOW)
    );

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        LivingEntity entity = context.getEntity();
        if (entity.getRandom().nextDouble() < getTriggerChance(entity)){
            attackContext.setColorRl(ModBulletColors.CHARM.getId());
            attackContext.addSimpleTriggers(bullet_triggers);
        }
    }
}

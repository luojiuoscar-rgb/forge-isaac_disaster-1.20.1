package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;
import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.MomsPerfume.getTriggerChance;

public final class MomsPerfumeAttachment extends AttackPreparationEffect {

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        LivingEntity entity = context.getEntity();
        List<SimpleTrigger> bulletTriggers = List.of(
                new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE, ModExecutableEffects.MOMS_PERFUME)
        );
        if (entity.getRandom().nextDouble() < getTriggerChance(entity)) {
            attackContext.setColorRl(ModBulletColors.FEAR.getId());
            attackContext.addSimpleTriggers(bulletTriggers);
        }
    }
}

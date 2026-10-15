package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.MomsContacts.chance;

public final class MomsContactsAttachment extends AttackPreparationEffect {

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        if (context.getEntity().getRandom().nextDouble() >= chance(context.getEntity())) return;
        attackContext.setColorRl(ModBulletColors.PETRIFIED.getId());
        attackContext.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE,
                ModExecutableEffects.MOMS_CONTACTS));
    }
}

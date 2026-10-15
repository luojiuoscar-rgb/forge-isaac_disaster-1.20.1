package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.minecraft.server.level.ServerPlayer;
import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.LeadPencil.applyBloodColor;

public final class LeadPencilAttachment extends AttackPreparationEffect {

    @Override
    protected void prepare(ExecutableEffectContext context, AttackContext attackContext) {
        if (!(context.getEntity() instanceof ServerPlayer)) return;
        if (!ModAttackTypes.BULLET.getId().equals(attackContext.getRootTypeId())
                || ModAttackTypes.C_SECTION.getId().equals(attackContext.getTypeId())) return;
        if (context.getEntity().getRandom().nextDouble() < 0.5) {
            applyBloodColor(attackContext);
        }
    }
}

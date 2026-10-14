package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import java.util.List;

public final class Scorpio extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY_RESTRICTED, ModExecutableEffects.SCORPIO)
    ));

    public Scorpio() { super(TRIGGER); }

    @Override
    public void attachToBullet(ExecutableEffectContext context, AttackContext attackContext) {
        attackContext.setColorRl(ModBulletColors.POISON.getId());
        attackContext.addSimpleTrigger(new SimpleTrigger(ModTriggerTypes.BULLET_HIT_ENTITY_BEFORE,
                ModExecutableEffects.SCORPIO));
    }
}

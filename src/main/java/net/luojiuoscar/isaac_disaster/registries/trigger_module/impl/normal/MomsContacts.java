package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.*;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;

public final class MomsContacts extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ATTACK_CONTEXT_PREPARE, ModExecutableEffects.MOMS_CONTACTS_ATTACHMENT),
            new SimpleTrigger(ModTriggerTypes.HIT_ENTITY_RESTRICTED, ModExecutableEffects.MOMS_CONTACTS,
                    context -> context.getEntity().getRandom().nextDouble() < chance(context.getEntity()))
    ));

    public MomsContacts() { super(TRIGGER); }

    static double chance(double luck) { return 1.0 / Math.max(2.0, 5.0 - Math.floor(luck * 0.15)); }
    public static double chance(LivingEntity entity) { return chance(getLuck(entity)); }

}

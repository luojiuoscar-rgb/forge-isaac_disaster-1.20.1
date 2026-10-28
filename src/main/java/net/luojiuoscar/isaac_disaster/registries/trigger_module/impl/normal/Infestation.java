package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModule;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.List;
import static net.luojiuoscar.isaac_disaster.registries.ability.passive.impl.Infestation.chance;

public final class Infestation extends TriggerModule {
    private static final CompositeTrigger TRIGGER = new CompositeTrigger(List.of(
            new SimpleTrigger(ModTriggerTypes.ON_HURT, ModExecutableEffects.INFESTATION,
                    context -> {
                        var entity = context.getEntity();
                        var luck = entity.getAttribute(Attributes.LUCK);
                        double probability = chance(luck == null ? 0 : luck.getValue());
                        return probability > 0 && entity.getRandom().nextDouble() < probability;
                    })));

    public Infestation() { super(TRIGGER); }
}

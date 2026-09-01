package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;

public interface IExecutableEffect {
    IExecutableEffect EMPTY = context -> {};

    void apply(ExecutableEffectContext context);

    /**
     * Returns the single trigger type required by this effect, or the empty
     * placeholder when the effect is not tied to one trigger type.
     */
    default TriggerType getRequiredTriggerType() {
        return ModTriggerTypes.EMTPY;
    }
}

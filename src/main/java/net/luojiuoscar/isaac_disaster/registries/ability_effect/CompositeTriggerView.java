package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import org.jetbrains.annotations.Nullable;
import oshi.annotation.concurrent.Immutable;

import java.util.List;

@Immutable
public class CompositeTriggerView {
    private final List<SimpleTrigger> triggers;

    public CompositeTriggerView(List<SimpleTrigger> triggers) {
        this.triggers = List.copyOf(triggers);
    }

    public void fire(ExecutableEffectContext context, @Nullable TriggerType type) {
        var batch = new ExecutableEffectBatch();
        batch.add(this, context, type, () -> true);
        batch.fire();
    }

    List<SimpleTrigger> triggers() {
        return triggers;
    }

    /** Returns true if the given effect is found */
    public boolean contains(IExecutableEffect effect){
        for (SimpleTrigger trigger : triggers){
            if (trigger.effect.get().equals(effect)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "CompositeTrigger: " + triggers;
    }
}

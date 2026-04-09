package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CompositeTrigger {
    public static final CompositeTrigger EMPTY = new CompositeTrigger(List.of());

    private final List<SimpleTrigger> trigger;

    public CompositeTrigger(List<SimpleTrigger> trigger) {
        this.trigger = trigger;
    }

    /* 传入Empty type或null代表不进行检测 **/
    public void fire(ExecutableEffectContext context, @Nullable TriggerType type) {
        for (SimpleTrigger t : trigger) {
            if (type == null || t.type.isPlaceholder() || t.type.is(type)){
                t.fire(context);
            }
        }
    }

    public List<SimpleTrigger> getTrigger() {
        return trigger;
    }

    @Override
    public String toString() {
        return "CompositeTrigger: " + trigger;
    }
}
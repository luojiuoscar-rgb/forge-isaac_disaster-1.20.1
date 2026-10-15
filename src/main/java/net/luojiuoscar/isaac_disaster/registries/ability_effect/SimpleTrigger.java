package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 记录type->effect
 * 一个triggerModule可以持有多个simpleTrigger
 */
public class SimpleTrigger {
    public final TriggerType type;
    public final Supplier<? extends IExecutableEffect> effect;
    private final Predicate<ExecutableEffectContext> condition;

    public SimpleTrigger(TriggerType type,
                         Supplier<? extends IExecutableEffect> effect,
                         Predicate<ExecutableEffectContext> condition) {
        this.type = type;
        this.effect = effect;
        this.condition = condition != null ? condition : ctx -> true;
    }

    public SimpleTrigger(TriggerType type, Supplier<? extends IExecutableEffect> effect){
        this(type, effect, null);
    }

    private boolean shouldFire(ExecutableEffectContext context) {
        return condition.test(context);
    }

    public void fire(ExecutableEffectContext context){
        if (shouldFire(context)) {
            effect.get().apply(context);
        }
    }

    public TriggerType getType() {
        return type;
    }

    public final double getPriority() {
        return effect.get().getPriority();
    }

    @Override
    public String toString() {
        return "SimpleTrigger: " + type + " -> " + effect.get();
    }
}

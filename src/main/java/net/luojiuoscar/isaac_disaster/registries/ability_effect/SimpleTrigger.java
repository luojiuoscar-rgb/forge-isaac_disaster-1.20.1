package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

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
    @Nullable
    private final ResourceLocation source;

    public SimpleTrigger(TriggerType type,
                         Supplier<? extends IExecutableEffect> effect,
                         Predicate<ExecutableEffectContext> condition) {
        this(type, effect, condition, null);
    }

    public SimpleTrigger(TriggerType type,
                         Supplier<? extends IExecutableEffect> effect,
                         Predicate<ExecutableEffectContext> condition,
                         @Nullable ResourceLocation source) {
        this.type = type;
        this.effect = effect;
        this.condition = condition != null ? condition : ctx -> true;
        this.source = source;
    }

    public SimpleTrigger(TriggerType type, Supplier<? extends IExecutableEffect> effect){
        this(type, effect, null);
    }

    private boolean shouldFire(ExecutableEffectContext context) {
        return condition.test(context);
    }

    public void fire(ExecutableEffectContext context){
        if (shouldFire(context)) {
            ExecutableEffectContext execution = context;
            if (source != null) {
                execution = context.copy(null);
                execution.set(ContextKeys.SOURCE, source);
            }
            effect.get().apply(execution);
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

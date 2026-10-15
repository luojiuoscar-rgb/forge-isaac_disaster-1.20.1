package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Collects one trigger phase before executing effects across all participating modules. */
public final class ExecutableEffectBatch {
    private final List<Invocation> invocations = new ArrayList<>();

    public void add(CompositeTriggerView view, ExecutableEffectContext context,
                    @Nullable TriggerType type, BooleanSupplier allowed) {
        for (SimpleTrigger trigger : view.triggers()) {
            if (type == null || trigger.type.is(type)) {
                invocations.add(new Invocation(trigger, view, context, allowed));
            }
        }
    }

    public void fire() {
        // Snapshot before execution: nested dispatch and trigger mutations belong to a later batch.
        var ordered = new ArrayList<>(invocations);
        ordered.sort(Comparator.comparingDouble((Invocation invocation) ->
                invocation.trigger().getPriority()).reversed());
        for (Invocation invocation : ordered) {
            if (invocation.allowed().getAsBoolean()) {
                invocation.context().set(ContextKeys.COMPOSITE_TRIGGER_VIEW, invocation.view());
                invocation.trigger().fire(invocation.context());
            }
        }
    }

    private record Invocation(SimpleTrigger trigger, CompositeTriggerView view,
                              ExecutableEffectContext context, BooleanSupplier allowed) {}
}

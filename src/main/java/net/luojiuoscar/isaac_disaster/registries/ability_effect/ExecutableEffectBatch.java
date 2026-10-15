package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Collects one trigger phase before executing effects across all participating modules. */
public final class ExecutableEffectBatch {
    private static final Comparator<Invocation> PRIORITY = (first, second) ->
            Double.compare(second.trigger().getPriority(), first.trigger().getPriority());
    private final List<Invocation> invocations = new ArrayList<>();

    public void add(CompositeTriggerView view, ExecutableEffectContext context,
                    @Nullable TriggerType type, BooleanSupplier allowed) {
        for (SimpleTrigger trigger : view.triggers(type)) {
            invocations.add(new Invocation(trigger, view, context, allowed));
        }
    }

    public void fire() {
        if (invocations.isEmpty()) return;
        if (invocations.size() == 1) {
            fire(invocations.get(0));
            return;
        }
        // Snapshot before execution: nested dispatch and trigger mutations belong to a later batch.
        var ordered = invocations.toArray(new Invocation[invocations.size()]);
        Arrays.sort(ordered, PRIORITY);
        for (Invocation invocation : ordered) {
            fire(invocation);
        }
    }

    private static void fire(Invocation invocation) {
        if (invocation.allowed().getAsBoolean()) {
            invocation.context().set(ContextKeys.COMPOSITE_TRIGGER_VIEW, invocation.view());
            invocation.trigger().fire(invocation.context());
        }
    }

    private record Invocation(SimpleTrigger trigger, CompositeTriggerView view,
                              ExecutableEffectContext context, BooleanSupplier allowed) {}
}

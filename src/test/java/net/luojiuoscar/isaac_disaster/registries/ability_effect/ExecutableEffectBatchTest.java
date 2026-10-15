package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class ExecutableEffectBatchTest {
    private static SimpleTrigger trigger(double priority, Consumer<ExecutableEffectContext> action,
                                         Predicate<ExecutableEffectContext> condition) {
        return new SimpleTrigger(null, () -> new IExecutableEffect() {
            @Override
            public double getPriority() { return priority; }

            @Override
            public void apply(ExecutableEffectContext context) { action.accept(context); }
        }, condition);
    }

    @Test
    void crossModuleOrderingRetainsContextAndDefersBothGuardsAndConditions() {
        var first = new ExecutableEffectContext(null);
        first.set(ContextKeys.AMPLIFIER, 3.0);
        var second = first.copy(null);
        second.set(ContextKeys.AMPLIFIER, 2.0);
        double[] amount = {2};
        var batch = new ExecutableEffectBatch();
        batch.add(new CompositeTriggerView(List.of(trigger(-100,
                context -> amount[0] *= context.get(ContextKeys.AMPLIFIER), context -> amount[0] == 4))),
                first, null, () -> amount[0] == 4);
        batch.add(new CompositeTriggerView(List.of(trigger(0,
                context -> amount[0] += context.get(ContextKeys.AMPLIFIER), null))), second, null, () -> true);
        batch.fire();
        assertEquals(12, amount[0]);
        assertEquals(3, first.get(ContextKeys.AMPLIFIER));
        assertEquals(2, second.get(ContextKeys.AMPLIFIER));
    }

    @Test
    void equalPrioritiesRetainOrderAcrossViewsAndNestedExecution() {
        var trace = new ArrayList<String>();
        var context = new ExecutableEffectContext(null);
        var batch = new ExecutableEffectBatch();
        batch.add(new CompositeTriggerView(List.of(trigger(0, ctx -> {
            trace.add("outer-first");
            new CompositeTriggerView(List.of(trigger(100, nested -> trace.add("nested"), null)))
                    .fire(ctx.copy(null), null);
        }, null))), context, null, () -> true);
        batch.add(new CompositeTriggerView(List.of(trigger(0, ctx -> trace.add("outer-second"), null))),
                context.copy(null), null, () -> true);
        batch.fire();
        assertEquals(List.of("outer-first", "nested", "outer-second"), trace);
    }

    @Test
    void exceptionsPropagateAndStopTheCurrentExecution() {
        var expected = new IllegalStateException("test effect failed");
        var batch = new ExecutableEffectBatch();
        batch.add(new CompositeTriggerView(List.of(
                trigger(100, context -> { throw expected; }, null),
                trigger(0, context -> fail("Must not continue after an exception"), null))),
                new ExecutableEffectContext(null), null, () -> true);
        assertSame(expected, assertThrows(IllegalStateException.class, batch::fire));
    }
}

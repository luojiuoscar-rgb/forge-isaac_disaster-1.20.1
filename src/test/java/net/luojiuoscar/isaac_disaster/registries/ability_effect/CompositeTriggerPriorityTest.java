package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectPriority;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositeTriggerPriorityTest {
    private static SimpleTrigger trigger(double priority, Runnable action) {
        return new SimpleTrigger(null, () -> new IExecutableEffect() {
            @Override
            public double getPriority() { return priority; }

            @Override
            public void apply(ExecutableEffectContext context) { action.run(); }
        });
    }

    @Test
    void lateMultiplierRunsAfterAdditionsRegardlessOfInsertionOrder() {
        double[] damage = {2};
        var view = new CompositeTriggerView(List.of(
                trigger(ExecutableEffectPriority.LOWEST.priority, () -> damage[0] *= 4),
                trigger(ExecutableEffectPriority.MEDIUM.priority, () -> damage[0] += 3)));
        view.fire(new ExecutableEffectContext(null), null);
        assertEquals(20, damage[0]);
    }

    @Test
    void equalPrioritiesPreserveTheirOriginalSequence() {
        var order = new ArrayList<String>();
        var view = new CompositeTriggerView(List.of(
                trigger(-100, () -> order.add("last")),
                trigger(0, () -> order.add("first")),
                trigger(0, () -> order.add("second")),
                trigger(100, () -> order.add("high"))));
        view.fire(new ExecutableEffectContext(null), null);
        assertEquals(List.of("high", "first", "second", "last"), order);
    }

    @Test
    void viewRebuildAndCopiesPreservePriorityOfLateAdditions() {
        double[] damage = {2};
        var composite = new CompositeTrigger(List.of(trigger(-100, () -> damage[0] *= 4)));
        var initial = composite.getView();
        composite.add(trigger(0, () -> damage[0] += 3));
        initial.fire(new ExecutableEffectContext(null), null);
        assertEquals(8, damage[0]);
        damage[0] = 2;
        composite.copy().getView().fire(new ExecutableEffectContext(null), null);
        assertEquals(20, damage[0]);
    }
}

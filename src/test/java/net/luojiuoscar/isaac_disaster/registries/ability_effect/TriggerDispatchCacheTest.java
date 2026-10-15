package net.luojiuoscar.isaac_disaster.registries.ability_effect;

import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class TriggerDispatchCacheTest {
    @Test
    void repeatedTypedDispatchCachesOrderButReevaluatesConditions() {
        var reads = new AtomicInteger();
        var allowed = new AtomicBoolean(true);
        var trace = new ArrayList<String>();
        var composite = new CompositeTrigger(List.of(
                trigger(-100, "last", reads, allowed, trace),
                trigger(100, "first", reads, allowed, trace)));
        var context = new ExecutableEffectContext(null);
        composite.fire(context, ModTriggerTypes.ON_HURT);
        assertEquals(List.of("first", "last"), trace);
        allowed.set(false);
        composite.fire(context, ModTriggerTypes.ON_HURT);
        assertEquals(List.of("first", "last"), trace);
        assertEquals(2, reads.get(), "Priority is stable definition metadata; do not resolve it on every dispatch");
    }

    @Test
    void copiesShareImmutableViewsUntilEitherCompositionChanges() {
        var trace = new ArrayList<String>();
        var composite = new CompositeTrigger(List.of(trigger(0, "original", new AtomicInteger(), new AtomicBoolean(true), trace)));
        var view = composite.getView();
        var copy = composite.copy();
        assertSame(view, copy.getView());
        copy.add(trigger(100, "copy", new AtomicInteger(), new AtomicBoolean(true), trace));
        assertNotSame(view, copy.getView());
        var context = new ExecutableEffectContext(null);
        composite.fire(context, ModTriggerTypes.ON_HURT);
        copy.fire(context, ModTriggerTypes.ON_HURT);
        assertEquals(List.of("original", "copy", "original"), trace);
    }

    @Test
    void viewsAndCopiesDoNotResolveEffectsBeforeRegistryReadiness() {
        var ready = new AtomicBoolean();
        var executions = new AtomicInteger();
        IExecutableEffect effect = context -> executions.incrementAndGet();
        var composite = new CompositeTrigger(List.of(new SimpleTrigger(ModTriggerTypes.ON_HURT, () -> {
            assertTrue(ready.get(), "Effect lookup must remain lazy");
            return effect;
        })));
        var view = composite.getView();
        var copy = composite.copy();
        ready.set(true);
        view.fire(new ExecutableEffectContext(null), ModTriggerTypes.ON_HURT);
        copy.fire(new ExecutableEffectContext(null), ModTriggerTypes.ON_HURT);
        assertEquals(2, executions.get());
    }

    @Test
    void cachedTypeSelectionsKeepPlaceholderRulesAndIgnoreUnrelatedEffects() {
        var executions = new AtomicInteger();
        IExecutableEffect effect = context -> executions.incrementAndGet();
        var view = new CompositeTriggerView(List.of(
                new SimpleTrigger(ModTriggerTypes.EMTPY, () -> effect),
                new SimpleTrigger(ModTriggerTypes.ON_HURT, () -> effect),
                new SimpleTrigger(ModTriggerTypes.KILL_ENTITY, () -> {
                    fail("Unrelated effect must not be resolved");
                    return effect;
                })));
        var context = new ExecutableEffectContext(null);
        for (int i = 0; i < 3; i++) {
            view.fire(context, ModTriggerTypes.ON_HURT);
            view.fire(context, ModTriggerTypes.ATTACK_PLAN);
        }
        assertEquals(9, executions.get());
    }

    @Test
    void mutationsDuringExecutionOnlyAffectTheNextDispatch() {
        var trace = new ArrayList<String>();
        var composite = new CompositeTrigger();
        IExecutableEffect late = context -> trace.add("late");
        IExecutableEffect mutate = context -> {
            trace.add("mutate");
            composite.add(new SimpleTrigger(ModTriggerTypes.ON_HURT, () -> late));
        };
        composite.add(new SimpleTrigger(ModTriggerTypes.ON_HURT, () -> mutate));
        var context = new ExecutableEffectContext(null);
        composite.fire(context, ModTriggerTypes.ON_HURT);
        assertEquals(List.of("mutate"), trace);
        composite.fire(context, ModTriggerTypes.ON_HURT);
        assertEquals(List.of("mutate", "mutate", "late"), trace);
    }

    @Test
    void concurrentReadersCanPrepareDifferentTypesWithoutSharingExecutionState() throws Exception {
        var executions = new AtomicInteger();
        var start = new CountDownLatch(1);
        IExecutableEffect effect = context -> executions.incrementAndGet();
        var view = new CompositeTriggerView(List.of(
                new SimpleTrigger(ModTriggerTypes.ON_HURT, () -> effect),
                new SimpleTrigger(ModTriggerTypes.KILL_ENTITY, () -> effect)));
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> {
                start.await();
                var context = new ExecutableEffectContext(null);
                for (int i = 0; i < 1000; i++) view.fire(context, ModTriggerTypes.ON_HURT);
                return null;
            });
            var second = executor.submit(() -> {
                start.await();
                var context = new ExecutableEffectContext(null);
                for (int i = 0; i < 1000; i++) view.fire(context, ModTriggerTypes.KILL_ENTITY);
                return null;
            });
            start.countDown();
            first.get();
            second.get();
            assertEquals(2000, executions.get());
        } finally {
            executor.shutdownNow();
        }
    }

    private static SimpleTrigger trigger(double priority, String name, AtomicInteger reads,
                                         AtomicBoolean allowed, List<String> trace) {
        IExecutableEffect effect = new IExecutableEffect() {
            @Override public double getPriority() { reads.incrementAndGet(); return priority; }
            @Override public void apply(ExecutableEffectContext context) { trace.add(name); }
        };
        return new SimpleTrigger(ModTriggerTypes.ON_HURT, () -> effect, context -> allowed.get());
    }
}

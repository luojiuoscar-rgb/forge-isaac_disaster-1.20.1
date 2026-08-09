package net.luojiuoscar.isaac_disaster.registries.trigger_module.rule;

import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKey;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleInstance;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerType;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TriggerModuleRuleTest {
    private static final ResourceLocation MODULE_A = ResourceLocation.fromNamespaceAndPath("test", "module_a");
    private static final ResourceLocation MODULE_B = ResourceLocation.fromNamespaceAndPath("test", "module_b");
    private static final TriggerType HIT = new TriggerType(ResourceLocation.fromNamespaceAndPath("test", "hit"));

    @Test
    void snapshotIsImmutableAndTracksModuleStacks() {
        TriggerModuleSnapshot snapshot = new TriggerModuleSnapshot(List.of(
                new TriggerModuleInstance(MODULE_A, 2, 1.0),
                new TriggerModuleInstance(MODULE_B, 1, 0.0)
        ));

        assertTrue(snapshot.contains(MODULE_A));
        assertEquals(2, snapshot.getStacks(MODULE_A));
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.modules().add(new TriggerModuleInstance(MODULE_A, 1, 0.0)));
    }

    @Test
    void ruleMatchesEquivalentTriggerTypeIds() {
        TriggerModuleRule rule = new TestRule(Set.of(MODULE_A), Set.of(HIT), true);
        TriggerType equivalentHit = new TriggerType(HIT.getId());

        assertTrue(rule.appliesTo(MODULE_A, equivalentHit));
        assertFalse(rule.appliesTo(MODULE_B, equivalentHit));
    }

    @Test
    void oneRejectingRuleBlocksTheCandidate() {
        TriggerModuleSnapshot snapshot = new TriggerModuleSnapshot(List.of(
                new TriggerModuleInstance(MODULE_A, 1, 0.0)
        ));
        TriggerModuleRuleContext context = new TriggerModuleRuleContext(
                snapshot.modules().get(0), HIT, new ExecutableEffectContext(null), snapshot);

        assertFalse(TriggerModuleRuleIndex.allows(List.of(
                new TestRule(Set.of(MODULE_A), Set.of(HIT), true),
                new TestRule(Set.of(MODULE_A), Set.of(HIT), false)
        ), context));
    }

    @Test
    void contextCollectionsAreReadOnlySnapshots() {
        ContextKey<List<String>> key = new ContextKey<>();
        List<String> sourceValues = new ArrayList<>(List.of("before"));
        ExecutableEffectContext effectContext = new ExecutableEffectContext(null);
        effectContext.set(key, sourceValues);
        TriggerModuleRuleContext context = new TriggerModuleRuleContext(
                new TriggerModuleInstance(MODULE_A, 1, 0.0), HIT, effectContext,
                TriggerModuleSnapshot.empty());

        List<String> visibleValues = context.get(key);
        sourceValues.add("after");

        assertEquals(List.of("before"), visibleValues);
        assertThrows(UnsupportedOperationException.class, () -> visibleValues.add("blocked"));
    }

    private static final class TestRule extends TriggerModuleRule {
        private final boolean allowed;

        private TestRule(Set<ResourceLocation> targetModules, Set<TriggerType> triggerTypes, boolean allowed) {
            super(targetModules, triggerTypes);
            this.allowed = allowed;
        }

        @Override
        public boolean allows(TriggerModuleRuleContext context) {
            return allowed;
        }
    }
}

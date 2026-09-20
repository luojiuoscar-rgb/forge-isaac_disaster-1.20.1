package net.luojiuoscar.isaac_disaster.registries.trajectory;

import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.trajectory.rule.*;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TrajectoryRuleTest {
    private static ResourceLocation id(String path) { return ResourceLocation.parse("test:" + path); }
    private static TrajectorySpec spec(String path) { return new TrajectorySpec(id(path), 0); }
    private static TrajectoryRule rule(String a, String b, String output) {
        return TrajectoryRule.replace(Set.of(id(a), id(b)), output == null ? null : id(output), null, 0);
    }
    @Test void replacementPreservesOrderAndCacheIsImmutable() {
        var resolver = new TrajectoryRuleResolver(Map.of(id("r"), rule("c", "d", "e")));
        List<TrajectorySpec> input = List.of(spec("a"), spec("b"), spec("c"), spec("d"));
        var result = resolver.resolve(ModAttackTypes.BULLET.getId(), input);
        assertEquals(List.of(spec("a"), spec("b"), spec("e")), result);
        assertSame(result, resolver.resolve(ModAttackTypes.BULLET.getId(), input));
        assertThrows(UnsupportedOperationException.class, () -> result.clear());
        assertEquals(4, input.size());
    }
    @Test void emptyOutputDeletesOnlyMatchedModules() {
        var resolver = new TrajectoryRuleResolver(Map.of(id("r"), rule("a", "c", null)));
        assertEquals(List.of(spec("b")), resolver.resolve(null, List.of(spec("a"), spec("b"), spec("c"))));
    }
    @Test void newlyEnabledEarlierRuleIsRevisitedAndCyclesTerminate() {
        var resolver = new TrajectoryRuleResolver(Map.of(
                id("0"), rule("c", "d", "e"),
                id("1"), rule("a", "b", "c"),
                id("2"), TrajectoryRule.replace(Set.of(id("e")), id("a"), null, 0),
                id("3"), TrajectoryRule.replace(Set.of(id("a")), id("e"), null, 0)));
        assertEquals(List.of(spec("e")), resolver.resolve(null, List.of(spec("a"), spec("b"), spec("d"))));
    }
    @Test void replacementRetainsExistingStacksAndDoesNotCrossRootTypes() {
        var resolver = new TrajectoryRuleResolver(Map.of(id("r"),
                TrajectoryRule.replace(Set.of(id("planet"), id("mirror")), id("planet"), ModAttackTypes.BULLET.getId(), 0)));
        var input = List.of(new TrajectorySpec(id("planet"), 3), new TrajectorySpec(id("mirror"), 1));
        assertEquals(List.of(input.get(0)), resolver.resolve(ModAttackTypes.BULLET.getId(), input));
        assertEquals(input, resolver.resolve(ModAttackTypes.LASER.getId(), input));
    }
    @Test void priorityAndNewOutputStackMergeAreDeterministic() {
        var resolver = new TrajectoryRuleResolver(Map.of(
                id("z"), TrajectoryRule.replace(Set.of(id("c"), id("d")), id("e"), null, -1),
                id("a"), rule("b", "c", "x")));
        var input = List.of(spec("a"), spec("b"), new TrajectorySpec(id("c"), 2), new TrajectorySpec(id("d"), 1), spec("e"));
        assertEquals(List.of(spec("a"), spec("b"), new TrajectorySpec(id("e"), 2)), resolver.resolve(null, input));
    }
    @Test void customRootsMatchByValueAndKeepSeparateCacheEntries() {
        var root = ResourceLocation.parse("addon:stream");
        var a = new TrajectorySpec(id("a"), 1);
        var b = new TrajectorySpec(id("b"), 0);
        var resolver = new TrajectoryRuleResolver(Map.of(id("external"),
                TrajectoryRule.replace(Set.of(a.id(), b.id()), a.id(), root, 0)));
        var input = List.of(a, b);
        var output = resolver.resolve(ResourceLocation.parse("addon:stream"), input);
        assertEquals(List.of(a), output);
        assertSame(output, resolver.resolve(ResourceLocation.parse("addon:stream"), input));
        assertEquals(input, resolver.resolve(ResourceLocation.parse("addon:other"), input));
    }

}

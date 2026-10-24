package net.luojiuoscar.isaac_disaster.groundeffect;

import net.luojiuoscar.isaac_disaster.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.groundeffect.impl.WaterPuddleEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.server.GroundEffectManager;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static net.luojiuoscar.isaac_disaster.groundeffect.GroundEffectTestFixtures.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class GroundEffectTypeTest {
    @Test
    void localFactoryCreatesIndependentInstances() {
        var type = WATER;
        var first = type.create(1, 2, new UUID(1, 2), Vec3.ZERO, 0.5D, 100, 1.0F, 3);
        var second = type.create(2, 1, null, new Vec3(3, 0, 0), 0.75D, 20, 1.0F, 4);
        first.tick();
        assertSame(type, first.getType());

        assertEquals(1, first.age());
        assertEquals(0, second.age());
        assertEquals(0.75D, second.radius());
        assertEquals(20, second.lifetime());
        assertNotEquals(first.identity(), second.identity());
    }

    @Test
    void managerCreatesAndRefreshesAdditionalTypesThroughTheirFactories() {
        var type = new GroundEffectType<>(TestEffect::new);
        var manager = new GroundEffectManager();
        UUID owner = new UUID(1, 2);
        var first = manager.spawn(type, owner, Vec3.ZERO, 0.5D, 100, 1.0F);
        assertNotNull(first);
        first.tick();
        assertSame(first, manager.spawn(type, owner, Vec3.ZERO, 0.5D, 100, 1.0F));
        assertEquals(0, first.age());
        assertSame(type, first.getType());

        assertEquals(1, manager.activeCount());
        var water = water(manager, owner, Vec3.ZERO, 1.0D, 100, 1.0F);
        assertNotSame(first, water);
        assertSame(WATER, water.getType());
        assertEquals(1, manager.activeCount(), "A later type still permanently replaces fully covered paint");
    }

    @Test
    void failingFactoryCannotEraseExistingCoverage() {
        GroundEffectType<TestEffect> broken = new GroundEffectType<>((type, slot, generation, owner, position, radius, lifetime, damage, order, animationDurationTicks) -> {
                    throw new IllegalStateException("Broken addon factory");
                });
        var manager = new GroundEffectManager();
        var old = water(manager, null, Vec3.ZERO);
        var coverage = old.coverage();
        assertNull(assertDoesNotThrow(() -> manager.spawn(broken, null, Vec3.ZERO, 1.0D, 100, 1.0F)));
        assertEquals(1, manager.activeCount());
        assertSame(coverage, old.coverage());
        assertTrue(old.contains(Vec3.ZERO));
        assertNotNull(water(manager, null, new Vec3(4, 0, 0)));
    }

    @Test
    void factoryCannotChangeRequestedIdentityOrSpawnParameters() {
        UUID ownerId = new UUID(1, 2);
        for (int field = 0; field < 9; field++) {
            int changedField = field;
            GroundEffectType<TestEffect> broken = new GroundEffectType<>((type, slot, generation, owner, position, radius, lifetime, damage, order, animationDurationTicks) -> new TestEffect(type,
                            changedField == 0 ? slot + 1 : slot,
                            changedField == 1 ? generation + 1 : generation,
                            changedField == 2 ? null : owner,
                            changedField == 3 ? position.add(1, 0, 0) : position,
                            changedField == 4 ? radius + 1 : radius,
                            changedField == 5 ? lifetime + 1 : lifetime,
                            changedField == 7 ? damage + 1 : damage,
                            changedField == 6 ? order + 1 : order, changedField == 8 ? 10 : animationDurationTicks));
            var manager = new GroundEffectManager();
            var old = water(manager, null, Vec3.ZERO);
            var coverage = old.coverage();
            assertNull(assertDoesNotThrow(
                    () -> manager.spawn(broken, ownerId, Vec3.ZERO, 1.0D, 100, 1.0F)), "Changed field " + field);
            assertEquals(1, manager.activeCount());
            assertSame(coverage, old.coverage());
            var next = water(manager, null, new Vec3(4, 0, 0));
            assertNotNull(next);
            assertEquals(1, next.slot(), "A rejected factory must release its reserved slot");
            assertEquals(1, next.creationOrder());
        }
    }

    @Test
    void factoryReceivesNormalizedRadiusAndLifetime() {
        var type = new GroundEffectType<>(TestEffect::new);
        var effect = type.create(0, 1, null, Vec3.ZERO, Double.NaN, -1, 1.0F, 0);
        assertEquals(0.0D, effect.radius());
        assertEquals(0, effect.lifetime());
    }

    @Test
    void missingFactoriesAndInvalidPositionsAreRejectedWithoutEscaping() {
        var empty = new GroundEffectType<TestEffect>(null);
        assertNull(assertDoesNotThrow(() -> empty.create(0, 1, null, Vec3.ZERO, 1, 100, 1, 0)));
        assertNull(assertDoesNotThrow(() -> WATER.create(0, 1, null, null, 1, 100, 1, 0)));
        assertNull(assertDoesNotThrow(() -> new GroundEffectManager().spawn(WATER, null,
                new Vec3(Double.NaN, 0, 0), 1, 100, 1)));
    }

    private static final class TestEffect extends AbstractGroundEffect {
        TestEffect(GroundEffectType<TestEffect> type, int slot, int generation, UUID owner,
                   Vec3 position, double radius, int lifetime, float damage, long order, int animationDurationTicks) {
            super(type, slot, generation, owner, position, radius, lifetime, damage, order, animationDurationTicks);
        }
        @Override public int getColor() { return 0xFFFFFF; }
        @Override public void onContactEffect(GroundEffectContact contact) { }
    }
}

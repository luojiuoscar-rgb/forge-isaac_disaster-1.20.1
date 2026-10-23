package net.luojiuoscar.isaac_disaster.groundeffect;

import net.luojiuoscar.isaac_disaster.groundeffect.core.AbstractGroundEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.core.GroundEffectContact;
import net.luojiuoscar.isaac_disaster.groundeffect.impl.WaterPuddleEffect;
import net.luojiuoscar.isaac_disaster.groundeffect.server.GroundEffectManager;
import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class GroundEffectTypeTest {
    @Test
    void waterDefinitionCreatesIndependentInstancesWithTheStableNetworkTypeId() {
        var type = WaterPuddleEffect.DEFINITION;
        var first = type.create(1, 2, new UUID(1, 2), Vec3.ZERO, 0.5D, 100, 3);
        var second = type.create(2, 1, null, new Vec3(3, 0, 0), 0.75D, 20, 4);
        first.tick();
        assertSame(type, first.getType());
        assertEquals(ResourceLocation.parse("isaac_disaster:water"), first.typeId());
        assertEquals(1, first.age());
        assertEquals(0, second.age());
        assertEquals(0.75D, second.radius());
        assertEquals(20, second.lifetime());
        assertNotEquals(first.identity(), second.identity());
    }

    @Test
    void managerCreatesAndRefreshesAdditionalTypesThroughTheirFactories() {
        var type = new GroundEffectType<>(ResourceLocation.parse("test:ground"), TestEffect::new);
        var manager = new GroundEffectManager();
        UUID owner = new UUID(1, 2);
        var first = manager.spawn(type, owner, Vec3.ZERO, 0.5D, 100);
        assertNotNull(first);
        first.tick();
        assertSame(first, manager.spawn(type, owner, Vec3.ZERO, 0.5D, 100));
        assertEquals(0, first.age());
        assertSame(type, first.getType());
        assertEquals(ResourceLocation.parse("test:ground"), first.typeId());
        assertEquals(1, manager.activeCount());
        var water = manager.spawnWater(owner, Vec3.ZERO, 1.0D, 100);
        assertNotSame(first, water);
        assertSame(WaterPuddleEffect.DEFINITION, water.getType());
        assertEquals(1, manager.activeCount(), "A later type still permanently replaces fully covered paint");
    }

    @Test
    void failingFactoryCannotEraseExistingCoverage() {
        GroundEffectType<TestEffect> broken = new GroundEffectType<>(ResourceLocation.parse("test:broken"),
                (type, slot, generation, owner, position, radius, lifetime, order) -> {
                    throw new IllegalStateException("Broken addon factory");
                });
        var manager = new GroundEffectManager();
        var old = manager.spawnWater(null, Vec3.ZERO);
        var coverage = old.coverage();
        assertThrows(IllegalStateException.class, () -> manager.spawn(broken, null, Vec3.ZERO, 1.0D, 100));
        assertEquals(1, manager.activeCount());
        assertSame(coverage, old.coverage());
        assertTrue(old.contains(Vec3.ZERO));
        assertNotNull(manager.spawnWater(null, new Vec3(4, 0, 0)));
    }

    @Test
    void factoryCannotChangeRequestedIdentityOrSpawnParameters() {
        UUID ownerId = new UUID(1, 2);
        for (int field = 0; field < 7; field++) {
            int changedField = field;
            GroundEffectType<TestEffect> broken = new GroundEffectType<>(ResourceLocation.parse("test:invalid"),
                    (type, slot, generation, owner, position, radius, lifetime, order) -> new TestEffect(type,
                            changedField == 0 ? slot + 1 : slot,
                            changedField == 1 ? generation + 1 : generation,
                            changedField == 2 ? null : owner,
                            changedField == 3 ? position.add(1, 0, 0) : position,
                            changedField == 4 ? radius + 1 : radius,
                            changedField == 5 ? lifetime + 1 : lifetime,
                            changedField == 6 ? order + 1 : order));
            var manager = new GroundEffectManager();
            var old = manager.spawnWater(null, Vec3.ZERO);
            var coverage = old.coverage();
            assertThrows(IllegalStateException.class,
                    () -> manager.spawn(broken, ownerId, Vec3.ZERO, 1.0D, 100), "Changed field " + field);
            assertEquals(1, manager.activeCount());
            assertSame(coverage, old.coverage());
            var next = manager.spawnWater(null, new Vec3(4, 0, 0));
            assertNotNull(next);
            assertEquals(1, next.slot(), "A rejected factory must release its reserved slot");
            assertEquals(1, next.creationOrder());
        }
    }

    @Test
    void factoryReceivesNormalizedRadiusAndLifetime() {
        var type = new GroundEffectType<>(ResourceLocation.parse("test:ground"), TestEffect::new);
        var effect = type.create(0, 1, null, Vec3.ZERO, Double.NaN, -1, 0);
        assertEquals(0.0D, effect.radius());
        assertEquals(0, effect.lifetime());
    }

    private static final class TestEffect extends AbstractGroundEffect {
        TestEffect(GroundEffectType<TestEffect> type, int slot, int generation, UUID owner,
                   Vec3 position, double radius, int lifetime, long order) {
            super(type, slot, generation, owner, position, radius, lifetime, order);
        }
        @Override public int getColor() { return 0xFFFFFF; }
        @Override public void onContactEffect(GroundEffectContact contact) { }
    }
}

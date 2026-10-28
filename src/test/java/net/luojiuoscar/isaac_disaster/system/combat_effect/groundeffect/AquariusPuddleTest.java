package net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect;

import net.luojiuoscar.isaac_disaster.registries.ground_effect.GroundEffectType;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.impl.AquariusPuddleEffect;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectManager;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.GroundEffectTestFixtures.water;

class AquariusPuddleTest {
    @Test
    void homingNeverChangesHeightOrOvershootsAndCapsDiagonalSpeed() {
        Vec3 from = new Vec3(1, 64.002, 2);
        Vec3 moved = AquariusPuddleEffect.horizontalStep(from, new Vec3(10, -100, 10));
        assertEquals(from.y, moved.y);
        assertEquals(0.25, moved.subtract(from).length(), 1e-9);
        Vec3 close = AquariusPuddleEffect.horizontalStep(from, from.add(0.02, 50, 0));
        assertEquals(from.x + 0.02, close.x, 1e-9);
        assertEquals(from.y, close.y);
        assertEquals(from, AquariusPuddleEffect.horizontalStep(from, from.add(0, 5, 0)));
    }

    @Test
    void independentAttachmentNodesAreNeverRepaintedIntoAnOlderInstance() {
        var manager = new GroundEffectManager();
        var type = new GroundEffectType<AquariusPuddleEffect>(AquariusPuddleEffect::new);
        var owner = UUID.randomUUID();
        var first = manager.spawn(type, owner, Vec3.ZERO, 0.5, 100, 13.2f);
        for (int i = 0; i < 10; i++) first.tick();
        var second = manager.spawn(type, owner, Vec3.ZERO, 0.5, 100, 13.2f);
        assertNotSame(first, second);
        assertEquals(10, first.age());
        assertEquals(0, second.age());
    }

    @Test
    void relocationUpdatesGridWithoutResettingLifetimeShapeOrPaintingOtherPuddles() {
        var manager = new GroundEffectManager();
        var moving = water(manager, null, Vec3.ZERO, 0.5, 100, 13.2f);
        var destination = water(manager, null, new Vec3(10, 0, 0));
        for (int i = 0; i < 20; i++) water(manager, null, new Vec3(100 + 2 * i, 0, 0));
        for (int i = 0; i < 8; i++) moving.tick();
        var shape = moving.footprint();
        int count = manager.activeCount();
        manager.relocate(moving, destination.position());
        assertTrue(manager.queryVisible(Vec3.ZERO, 0.1).isEmpty());
        assertEquals(2, manager.queryVisible(destination.position(), 0.1).size());
        assertEquals(count, manager.activeCount());
        assertEquals(8, moving.age());
        assertSame(shape, moving.footprint());
        assertEquals(0, moving.revision());
        moving.synchronizePosition();
        assertEquals(1, moving.revision());
        moving.synchronizePosition();
        assertEquals(1, moving.revision());
    }
}

package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BulletBounceOnEntityTest {
    private static final double EPSILON = 1.0E-9;

    @Test
    void convertsCollisionCenterToEntityBottomAndPreservesCenterAfterEpsilon() {
        Vec3 contact = new Vec3(2.0, 5.0, -3.0);
        Vec3 position = BulletBounceOnEntity.positionFromCenter(contact, 0.4, new Vec3(0.0, 1.0, 0.0));

        assertEquals(2.0, position.x, EPSILON);
        assertEquals(4.8001, position.y, EPSILON);
        assertEquals(-3.0, position.z, EPSILON);
        assertEquals(contact.y + 1.0E-4, position.y + 0.2, EPSILON);
    }

    @Test
    void scalesBottomConversionWithProjectileHeight() {
        Vec3 contact = new Vec3(0.0, 10.0, 0.0);
        Vec3 position = BulletBounceOnEntity.positionFromCenter(contact, 1.8, new Vec3(1.0, 0.0, 0.0));

        assertEquals(0.0001, position.x, EPSILON);
        assertEquals(9.1, position.y, EPSILON);
        assertEquals(0.0, position.z, EPSILON);
        assertEquals(contact.y, position.y + 0.9, EPSILON);
    }
}

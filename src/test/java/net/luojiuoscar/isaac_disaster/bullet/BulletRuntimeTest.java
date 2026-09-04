package net.luojiuoscar.isaac_disaster.bullet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class BulletRuntimeTest {
    @Test
    void invalidContextIsDiscardedWithoutThrowing() {
        assertNull(BulletRuntime.INSTANCE.spawn(null));
    }
}

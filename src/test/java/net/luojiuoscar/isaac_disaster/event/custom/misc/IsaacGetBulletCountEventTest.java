package net.luojiuoscar.isaac_disaster.event.custom.misc;

import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class IsaacGetBulletCountEventTest {
    @Test
    void countChangesRetainTheActualAttackType() {
        var event = new IsaacGetBulletCountEvent(null, TestAttackTypes.BULLET, 3);
        event.setCount(4);
        assertSame(TestAttackTypes.BULLET, event.getAttackType());
        assertEquals(4, event.getCount());
    }
}

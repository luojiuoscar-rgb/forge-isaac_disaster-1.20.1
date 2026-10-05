package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AttackSequenceIndexTest {
    @Test
    void ordinaryBulletUsesTheZeroSequenceIndex() {
        assertEquals(0, BulletState.builder().build().getAttackSequenceIndex());
    }
}

package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.PatternTestSupport;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BrimstoneAttackTest {
    @Test
    void usesTheExpectedShotCount() {
        assertEquals(13, BrimstoneAttack.SHOT_COUNT);
    }

    @Test
    void shootSingleForwardsRuntimeSequenceIndex() throws Exception {
        AttackContext context = PatternTestSupport.context(10.0f, 20.0f);
        RecordingBrimstoneAttack attack = new RecordingBrimstoneAttack();

        attack.shootSingle(context, 7);

        assertSame(context, attack.lastContext);
        assertEquals(7, attack.lastSequenceIndex);
    }

    private static final class RecordingBrimstoneAttack extends BrimstoneAttack {
        private AttackContext lastContext;
        private int lastSequenceIndex = -1;

        private RecordingBrimstoneAttack() {
            super(0.0);
        }

        @Override
        protected void shootSingleLaser(AttackContext ctx, int attackSequenceIndex) {
            this.lastContext = ctx;
            this.lastSequenceIndex = attackSequenceIndex;
        }
    }
}

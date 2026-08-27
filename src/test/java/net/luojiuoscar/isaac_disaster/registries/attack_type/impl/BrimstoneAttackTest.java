package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.PatternTestSupport;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BrimstoneAttackTest {
    @Test
    void usesTheExpectedShotCount() {
        assertEquals(13, BrimstoneAttack.SHOT_COUNT);
    }

    @Test
    void refreshShotContextUpdatesGeometryWhenControllable() throws Exception {
        AttackContext base = PatternTestSupport.context(10.0f, 20.0f);
        AttackContext working = base.toBuilder().build();
        Vec3 spawn = new Vec3(4.0, 5.0, 6.0);

        AttackContext returned = BrimstoneAttack.refreshBrimstoneShotContext(
                working, spawn, GeometryHelper.mainAxisFromRotation(30.0f, 40.0f), true);

        assertSame(working, returned);
        assertEquals(spawn.x, working.getPos().x, 1.0E-6);
        assertEquals(spawn.y, working.getPos().y, 1.0E-6);
        assertEquals(spawn.z, working.getPos().z, 1.0E-6);
        assertEquals(30.0, GeometryHelper.rotationFromMainAxis(working.getMainAxis()).xRot(), 1.0E-2);
        assertEquals(40.0, GeometryHelper.rotationFromMainAxis(working.getMainAxis()).yRot(), 1.0E-2);
        assertEquals(0.0, base.getPos().distanceTo(Vec3.ZERO), 1.0E-6);
        assertEquals(20.0, PatternTestSupport.yRot(base), 1.0E-2);
    }

    @Test
    void refreshShotContextKeepsRotationWhenUncontrollable() throws Exception {
        AttackContext base = PatternTestSupport.context(10.0f, 20.0f);
        AttackContext working = base.toBuilder().build();
        Vec3 spawn = new Vec3(1.0, 2.0, 3.0);

        AttackContext returned = BrimstoneAttack.refreshBrimstoneShotContext(
                working, spawn, GeometryHelper.mainAxisFromRotation(30.0f, 40.0f), false);

        assertSame(working, returned);
        assertEquals(spawn.x, working.getPos().x, 1.0E-6);
        assertEquals(spawn.y, working.getPos().y, 1.0E-6);
        assertEquals(spawn.z, working.getPos().z, 1.0E-6);
        assertEquals(20.0, PatternTestSupport.yRot(working), 1.0E-2);
        assertEquals(20.0, PatternTestSupport.yRot(base), 1.0E-2);
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

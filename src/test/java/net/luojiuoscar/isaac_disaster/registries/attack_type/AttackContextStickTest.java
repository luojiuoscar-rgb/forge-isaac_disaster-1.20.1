package net.luojiuoscar.isaac_disaster.registries.attack_type;

import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.BulletAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.PatternTestSupport;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttackContextStickTest {
    @Test
    void durationSurvivesConfigurationCopiesAndChildConstructionWithoutSharingRuntime() throws Exception {
        AttackContext context = PatternTestSupport.context(0, 0);
        assertEquals(0, context.getStickDurationTicks());
        context.setMaxStickDurationTicks(123);
        for (AttackContext copy : new AttackContext[]{context.copy(), context.copyConfiguration(),
                context.toBuilder().build()}) {
            assertEquals(123, copy.getStickDurationTicks());
            copy.setMaxStickDurationTicks(5);
            assertEquals(123, copy.getStickDurationTicks());
            copy.setMaxStickDurationTicks(150);
            assertEquals(150, copy.getStickDurationTicks());
            assertEquals(123, context.getStickDurationTicks());
        }
        var children = new BulletAttackPattern().generate(new AttackPatternContext(context, 2));
        var first = BulletState.builder().attackContext(children.get(0)).build();
        var second = BulletState.builder().attackContext(children.get(1)).build();
        assertEquals(123, first.stickState().durationTicks());
        assertEquals(123, second.stickState().durationTicks());
        assertNotSame(first.stickState(), second.stickState());
        assertTrue(first.isFlying() && second.isFlying());
        assertEquals(0, first.stickState().elapsedTicks());
        assertNull(first.stickState().targetUuid());
    }

    @Test
    void freezeLocksDurationAndNegativeRequestsAreClamped() throws Exception {
        AttackContext context = PatternTestSupport.context(0, 0);
        context.setMaxStickDurationTicks(77);
        context.freeze();
        context.setMaxStickDurationTicks(200);
        assertEquals(77, context.getStickDurationTicks());
        assertEquals(77, context.copyConfiguration().getStickDurationTicks());
        assertEquals(77, context.toBuilder().stickDurationTicks(-1).build().getStickDurationTicks());
        assertDoesNotThrow(() -> context.copy().setMaxStickDurationTicks(-1));
        assertEquals(0, PatternTestSupport.context(0, 0).toBuilder().stickDurationTicks(-1).build().getStickDurationTicks());
    }
    @Test
    void preparationRequestsKeepMaximumRegardlessOfOrderRepetitionOrZero() throws Exception {
        for (int[] requests : new int[][]{{100, 160, 100, 0}, {160, 0, 100, 160}}) {
            AttackContext context = PatternTestSupport.context(0, 0);
            for (int ticks : requests) context.setMaxStickDurationTicks(ticks);
            assertEquals(160, context.getStickDurationTicks());
        }
    }

    @Test
    void buildersCannotShortenInheritedOrEarlierRequests() throws Exception {
        AttackContext context = PatternTestSupport.context(0, 0);
        context.setMaxStickDurationTicks(160);
        AttackContext child = context.toBuilder().stickDurationTicks(100).stickDurationTicks(0).build();
        assertEquals(160, child.getStickDurationTicks());
        assertEquals(160, BulletState.builder().attackContext(child).stickDurationTicks(80).build()
                .stickState().durationTicks());
        assertEquals(200, BulletState.builder().stickDurationTicks(200).stickDurationTicks(0)
                .stickDurationTicks(100).build().stickState().durationTicks());
    }

    @Test
    void invalidNumericDirectionsUseTheDefaultAxis() throws Exception {
        AttackContext context = PatternTestSupport.context(0, 0);
        context.setMainAxis(Vec3.ZERO);
        assertEquals(new Vec3(1, 0, 0), context.getMainAxis());
        context.setMainAxis(new Vec3(Double.NaN, 0, 0));
        assertEquals(new Vec3(1, 0, 0), context.getMainAxis());
    }
}

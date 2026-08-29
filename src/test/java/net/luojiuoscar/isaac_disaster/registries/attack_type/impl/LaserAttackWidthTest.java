package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.PatternTestSupport;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LaserAttackWidthTest {
    private static final double EPSILON = 1.0E-4;

    @Test
    void normalLaserAppliesTheQuarterWidthCalibration() throws Exception {
        assertEquals(0.5, getWidth(new LaserAttack(0.0), frozenContextWithFinalScale(4.0)), EPSILON);
    }

    @Test
    void brimstoneUsesTheFixedWidthForEveryBulletScale() throws Exception {
        BrimstoneAttack attack = new BrimstoneAttack(0.0);
        assertEquals(1.0, getWidth(attack, frozenContextWithFinalScale(1.0)), EPSILON);
        assertEquals(1.0, getWidth(attack, frozenContextWithFinalScale(100.0)), EPSILON);
    }

    private static AttackContext frozenContextWithFinalScale(double scale) throws Exception {
        AttackContext context = PatternTestSupport.context(0.0f, 0.0f);
        context.setBulletScale(scale, true);
        context.freeze();
        return context;
    }

    private static double getWidth(LaserAttack attack, AttackContext context) throws Exception {
        Method method = attack.getClass().getDeclaredMethod("getWidth", AttackContext.class);
        method.setAccessible(true);
        return (double) method.invoke(attack, context);
    }
}

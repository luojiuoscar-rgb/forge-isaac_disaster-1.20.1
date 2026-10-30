package net.luojiuoscar.isaac_disaster.entity.familiar.impl.loki_baby;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LokiBabyEntityTest {
    @Test
    void volleysAreFourUnitAxesInOnePlaneAtHorizontalTiltedAndVerticalAim() throws Exception {
        LokiBabyEntity baby = allocate();
        for (Vec3 aim : List.of(new Vec3(0, 0, 1), new Vec3(1, 2, 3).normalize(),
                new Vec3(0, 1, 0), new Vec3(0, -1, 0), new Vec3(1e-6, 1, -1e-6).normalize())) {
            var directions = baby.tearDirections(aim);
            assertEquals(4, directions.size());
            assertEquals(aim, directions.get(0));
            assertEquals(Vec3.ZERO, directions.get(0).add(directions.get(1)));
            assertEquals(Vec3.ZERO, directions.get(2).add(directions.get(3)));
            for (Vec3 direction : directions) assertEquals(1, direction.length(), 1e-8);
            assertEquals(0, directions.get(0).dot(directions.get(2)), 1e-8);
            Vec3 normal = directions.get(0).cross(directions.get(2));
            for (Vec3 direction : directions) assertEquals(0, direction.dot(normal), 1e-8);
        }
    }

    @Test
    void actionDurationIsIndependentFromItsAttackInterval() throws Exception {
        LokiBabyEntity baby = allocate();
        assertEquals(12, baby.getAttackDurationTicks());
        assertEquals(15, baby.attackIntervalTicks());
        assertEquals(3, baby.heldAttackWindupTicks());
        assertEquals(2, baby.tearDamageCoefficient());
        assertEquals(0.6F, baby.getBaseModelScale());
    }

    private static LokiBabyEntity allocate() throws Exception {
        Class<?> type = Class.forName("sun.misc.Unsafe");
        Field field = type.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (LokiBabyEntity) type.getMethod("allocateInstance", Class.class)
                .invoke(field.get(null), LokiBabyEntity.class);
    }
}

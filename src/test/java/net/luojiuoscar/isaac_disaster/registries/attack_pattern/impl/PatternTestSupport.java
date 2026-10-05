package net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl;

import net.luojiuoscar.isaac_disaster.registries.attack_type.TestAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySequence;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public final class PatternTestSupport {
    private static final double DEFAULT_DAMAGE = 4.0;
    private static final double DEFAULT_RANGE = 12.0;
    private static final double DEFAULT_SPEED = 2.0;

    private PatternTestSupport() {
    }

    public static AttackContext context(float xRot, float yRot) throws Exception {
        return context(xRot, yRot, Vec3.ZERO);
    }

    public static AttackContext context(float xRot, float yRot, Vec3 pos) throws Exception {
        Object unsafe = unsafe();
        Class<?> unsafeClass = unsafe.getClass();
        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        AttackContext context = (AttackContext) allocateInstance.invoke(unsafe, AttackContext.class);

        setObject(unsafe, context, "colorRl", null);
        setObject(unsafe, context, "visualIds", Set.of());
        setObject(unsafe, context, "trigger", new CompositeTrigger());
        setObject(unsafe, context, "trajectorySequence", new TrajectorySequence());
        setObject(unsafe, context, "pos", pos);
        setObject(unsafe, context, "mainAxis", GeometryHelper.mainAxisFromRotation(xRot, yRot));
        setFloat(unsafe, context, "damage", (float) DEFAULT_DAMAGE);
        setDouble(unsafe, context, "bulletScaleModifier", 0.0D);
        setDouble(unsafe, context, "bulletScale", 0.25D);
        setObject(unsafe, context, "attackType", TestAttackTypes.BULLET);
        setDouble(unsafe, context, "bulletRange", DEFAULT_RANGE);
        setDouble(unsafe, context, "bulletSpeed", DEFAULT_SPEED);
        setObject(unsafe, context, "splitSequence", new SplitSequence());
        setBoolean(unsafe, context, "useFixedLaunchTransform", false);
        setObject(unsafe, context, "shooter", new Object());
        setObject(unsafe, context, "owner", new Object());
        return context;
    }

    public static float yRot(AttackContext context) {
        return GeometryHelper.rotationFromMainAxis(context.getMainAxis()).yRot();
    }

    private static Object unsafe() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        return theUnsafe.get(null);
    }

    private static void setObject(Object unsafe, Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        long offset = objectFieldOffset(unsafe, field);
        Method putObject = unsafe.getClass().getMethod("putObject", Object.class, long.class, Object.class);
        putObject.invoke(unsafe, target, offset, value);
    }

    private static void setFloat(Object unsafe, Object target, String fieldName, float value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        long offset = objectFieldOffset(unsafe, field);
        Method putFloat = unsafe.getClass().getMethod("putFloat", Object.class, long.class, float.class);
        putFloat.invoke(unsafe, target, offset, value);
    }

    private static void setDouble(Object unsafe, Object target, String fieldName, double value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        long offset = objectFieldOffset(unsafe, field);
        Method putDouble = unsafe.getClass().getMethod("putDouble", Object.class, long.class, double.class);
        putDouble.invoke(unsafe, target, offset, value);
    }

    private static void setBoolean(Object unsafe, Object target, String fieldName, boolean value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        long offset = objectFieldOffset(unsafe, field);
        Method putBoolean = unsafe.getClass().getMethod("putBoolean", Object.class, long.class, boolean.class);
        putBoolean.invoke(unsafe, target, offset, value);
    }

    private static long objectFieldOffset(Object unsafe, Field field) throws Exception {
        Method objectFieldOffset = unsafe.getClass().getMethod("objectFieldOffset", Field.class);
        return (long) objectFieldOffset.invoke(unsafe, field);
    }
}

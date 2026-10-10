package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class LaserProjectileBlockHitStateTest {
    @Test
    void storesTheFullLastBlockHitResult() throws Exception {
        AbstractLaserAttack.LaserProjectile projectile = allocateProjectile();

        assertNull(projectile.getLastBlockHit());

        BlockHitResult hit = new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false);
        projectile.setLastBlockHit(hit);

        assertSame(hit, projectile.getLastBlockHit());
    }

    private static AbstractLaserAttack.LaserProjectile allocateProjectile() throws Exception {
        var unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);
        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        AbstractLaserAttack.LaserProjectile projectile =
                (AbstractLaserAttack.LaserProjectile) allocateInstance.invoke(unsafe, AbstractLaserAttack.LaserProjectile.class);

        Field attackSequenceIndex = AbstractLaserAttack.LaserProjectile.class.getDeclaredField("attackSequenceIndex");
        attackSequenceIndex.setAccessible(true);
        attackSequenceIndex.setInt(projectile, 0);
        Field lastBlockHit = AbstractLaserAttack.LaserProjectile.class.getDeclaredField("lastBlockHit");
        lastBlockHit.setAccessible(true);
        lastBlockHit.set(projectile, null);
        return projectile;
    }
}

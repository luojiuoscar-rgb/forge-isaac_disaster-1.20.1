package net.luojiuoscar.isaac_disaster.bullet.client;

import java.lang.reflect.Field;
import java.util.UUID;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletStickState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulletAttachmentRenderTest {
    @Test void followsTargetFrameInterpolationAndRejectsReusedEntityId() throws Exception {
        UUID uuid = new UUID(1, 2);
        BulletState state = BulletState.builder().position(new Vec3(99, 99, 99)).build();
        state.applyStickSnapshot(new BulletStickState.Snapshot(BulletStickState.Phase.STUCK,
            80, 3, 0, 42, uuid, new Vec3(0.5, 1, -0.5)));
        var target = allocate(RenderTarget.class);
        target.xo = 2; target.yo = 4; target.zo = 6;
        Field position = Entity.class.getDeclaredField("position");
        position.setAccessible(true);
        position.set(target, new Vec3(6, 8, 10));
        target.setUUID(uuid);
        Vec3 authority = new Vec3(20, 30, 40);
        assertEquals(new Vec3(3.5, 6, 6.5), BulletClientRenderer.attachedPosition(state, 0.25F, id -> {
            assertEquals(42, id);
            return target;
        }, authority));
        target.setUUID(new UUID(3, 4));
        assertEquals(authority, BulletClientRenderer.attachedPosition(state, 0.25F, id -> target, authority));
        assertTrue(state.isStuck());
    }

    private static <T> T allocate(Class<T> type) throws Exception {
        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Field field = unsafeType.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return type.cast(unsafeType.getMethod("allocateInstance", Class.class).invoke(field.get(null), type));
    }

    private static final class RenderTarget extends Entity {
        private RenderTarget() { super(null, null); }
        @Override protected void defineSynchedData() { }
        @Override protected void readAdditionalSaveData(CompoundTag tag) { }
        @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    }
}

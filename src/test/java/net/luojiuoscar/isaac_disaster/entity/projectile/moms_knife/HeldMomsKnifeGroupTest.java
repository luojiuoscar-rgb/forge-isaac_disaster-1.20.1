package net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife;

import net.minecraft.resources.ResourceLocation;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeldMomsKnifeGroupTest {
    @Test
    void slotsKeepSourceIdentityAndNormalizeNegativeIndices() {
        var source = ResourceLocation.parse("test:first");
        assertEquals(new HeldMomsKnifeGroup.Slot(source, 0), new HeldMomsKnifeGroup.Slot(source, -3));
        assertNotEquals(new HeldMomsKnifeGroup.Slot(source, 0),
                new HeldMomsKnifeGroup.Slot(ResourceLocation.parse("test:second"), 0));
    }

    @Test
    void sharedPosesKeepOffsetsAndNormalizeInvalidDirections() {
        var pose = new HeldMomsKnifeGroup.Pose(new Vec3(0.25, -0.5, 0), new Vec3(0, 7, 0));
        assertEquals(new Vec3(0.25, -0.5, 0), pose.offset());
        assertEquals(new Vec3(0, 1, 0), pose.direction());
        var invalid = new HeldMomsKnifeGroup.Pose(new Vec3(Double.NaN, 0, 0), Vec3.ZERO);
        assertEquals(Vec3.ZERO, invalid.offset());
        assertEquals(new Vec3(0, 0, 1), invalid.direction());
    }

    @Test
    void fanHandlesExtendFromThePlayerAlongEachBladesOwnDirection() {
        for (double radius : new double[]{0.6, 1, 2.4})
                for (int count : new int[]{3, 4, 16}) for (int slot = 0; slot < count; slot++) {
            var layout = HeldMomsKnifeGroup.layout(count, slot, radius);
            Vec3 gripFromPlayer = PlayerMomsKnifeMotion.FORWARD.scale(radius).add(layout.offset());
            assertEquals(radius, gripFromPlayer.length(), 1e-12);
            assertTrue(gripFromPlayer.normalize().distanceTo(layout.direction()) < 1e-12,
                    "The handle and blade must lie on the same ray from the player");
        }
    }

    @Test
    void layoutsAreSymmetricWithParallelDualKnivesAndTheOrdinaryFanSpacing() {
        assertEquals(Vec3.ZERO, HeldMomsKnifeGroup.layout(1, 0, 1).offset());
        assertEquals(PlayerMomsKnifeMotion.FORWARD, HeldMomsKnifeGroup.layout(1, 0, 1).direction());
        assertEquals(new Vec3(0.25, 0, 0), HeldMomsKnifeGroup.layout(2, 0, 1).offset());
        assertEquals(new Vec3(-0.25, 0, 0), HeldMomsKnifeGroup.layout(2, 1, 1).offset());
        assertEquals(HeldMomsKnifeGroup.layout(2, 0, 1).direction(), HeldMomsKnifeGroup.layout(2, 1, 1).direction());
        for (int count : new int[]{3, 4, 6, 16}) {
            double step = Math.toRadians(2 * Math.max(11 - count, 5));
            for (int slot = 0; slot < count; slot++) {
                var layout = HeldMomsKnifeGroup.layout(count, slot, 1);
                assertTrue(PlayerMomsKnifeMotion.FORWARD.add(layout.offset()).distanceTo(layout.direction()) < 1e-12);
                assertEquals(1, layout.direction().length(), 1e-12);
                var opposite = HeldMomsKnifeGroup.layout(count, count - 1 - slot, 1);
                assertEquals(-layout.direction().x, opposite.direction().x, 1e-12);
                assertEquals(layout.direction().z, opposite.direction().z, 1e-12);
                if (slot > 0) assertEquals(step, Math.acos(layout.direction().dot(
                        HeldMomsKnifeGroup.layout(count, slot - 1, 1).direction())), 1e-12);
            }
        }
        assertEquals(HeldMomsKnifeGroup.layout(16, 15, 1), HeldMomsKnifeGroup.layout(100, 99, 1));
    }

    @Test
    void displacementTransformsPreserveQuarterBlockOffsetsAndZeroAtVerticalPitch() {
        for (float yaw : new float[]{0, 37, 180}) for (float pitch : new float[]{-90, -35, 0, 90}) {
            for (Vec3 local : new Vec3[]{Vec3.ZERO, new Vec3(0.25, 0, 0), new Vec3(0, -0.3, 0.7)}) {
                Vec3 world = PlayerMomsKnifeMotion.offsetToWorld(local, yaw, pitch);
                assertEquals(local.length(), world.length(), 1e-7);
                assertTrue(PlayerMomsKnifeMotion.offsetToLocal(world, yaw, pitch).distanceTo(local) < 1e-7);
            }
        }
    }

    @Test
    void spawnDataPreservesSlotOffsetLaunchDirectionAndCurrentTrackingDirection() {
        for (int slot : new int[]{-1, 0, 15}) {
            Vec3 current = new Vec3(0, 1, 0);
            Vec3 base = new Vec3(1, 0, 1).normalize();
            var original = new PlayerMomsKnifeSpawnData((byte) 0, 123, current, 8, 0.7f, 100, 27, -30,
                    slot, new Vec3(0.25, -0.125, 0), base,
                    ResourceLocation.parse("addon:extra_knives"));
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                original.write(buffer);
                var copy = PlayerMomsKnifeSpawnData.read(buffer);
                assertEquals(slot, copy.heldSlot());
                assertEquals(original.heldSource(), copy.heldSource());
                assertEquals(original.localOffset(), copy.localOffset());
                assertTrue(current.distanceTo(copy.direction()) < 1e-12);
                assertTrue(base.distanceTo(copy.baseDirection()) < 1e-12);
                assertEquals(0, buffer.readableBytes());
            } finally { buffer.release(); }
        }
    }
}

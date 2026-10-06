package net.luojiuoscar.isaac_disaster.bullet;

import net.luojiuoscar.isaac_disaster.bullet.collision.LaserCollisionBatch;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LaserCollisionBatchTest {
    @Test
    void unionsRegisteredBeamSweepsWithTheirCollisionMargin() {
        LaserCollisionBatch batch = new LaserCollisionBatch();
        batch.addSweep(new Vec3(0, 0, 0), new Vec3(10, 0, 0), 2.0D);
        batch.addSweep(new Vec3(4, 4, 0), new Vec3(4, 8, 0), 1.0D);

        assertEquals(new AABB(-1, -1, -1, 11, 8.5, 1), batch.bounds());
    }

    @Test
    void buildsASeparateSweepForTheRemainingPathAfterAContact() {
        assertEquals(
                new AABB(3, -1, -1, 11, 1, 1),
                LaserCollisionBatch.remainingSweepBounds(
                        new Vec3(4, 0, 0), new Vec3(10, 0, 0), 2.0D));
    }

    @Test
    void ordersBlockBeforeEntityWhenContactsShareTheSamePathParameter() {
        List<LaserCollisionBatch.Contact> contacts = new ArrayList<>(List.of(
                new LaserCollisionBatch.Contact(0.5D, LaserCollisionBatch.ContactType.ENTITY),
                new LaserCollisionBatch.Contact(0.5D, LaserCollisionBatch.ContactType.BLOCK),
                new LaserCollisionBatch.Contact(0.25D, LaserCollisionBatch.ContactType.ENTITY)));

        contacts.sort(LaserCollisionBatch.contactOrder());

        assertEquals(LaserCollisionBatch.ContactType.ENTITY, contacts.get(0).type());
        assertEquals(LaserCollisionBatch.ContactType.BLOCK, contacts.get(1).type());
        assertEquals(LaserCollisionBatch.ContactType.ENTITY, contacts.get(2).type());
    }
}

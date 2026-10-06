package net.luojiuoscar.isaac_disaster.bullet.collision;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Shared broad-phase state for instantaneous laser sweeps executed in one server tick.
 *
 * <p>The index is deliberately only a candidate cache. Callers must re-check liveness,
 * ownership, current bounds, and per-beam hit state immediately before dispatching an event.</p>
 */
public final class LaserCollisionBatch {
    private final EntityGrid entityGrid = new EntityGrid();
    private AABB bounds;

    /** Registers one beam's swept volume in the union used to build the broad phase. */
    public void addSweep(Vec3 start, Vec3 end, double width) {
        AABB sweep = sweptBounds(start, end, width);
        addSweep(sweep);
    }

    /** Adds a precomputed swept volume to the union used by the broad phase. */
    public void addSweep(AABB sweep) {
        if (sweep == null) return;
        bounds = bounds == null ? sweep : bounds.minmax(sweep);
    }

    /** Returns the collision volume that remains after a laser reaches a contact point. */
    public static AABB remainingSweepBounds(Vec3 contact, Vec3 end, double width) {
        return sweptBounds(contact, end, width);
    }

    /** Returns the union of all registered sweeps, or {@code null} before registration. */
    public AABB bounds() {
        return bounds;
    }

    /** Rebuilds the shared index with one world query. */
    public void rebuild(ServerLevel level) {
        Collection<LivingEntity> entities = bounds == null
                ? List.of()
                : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive);
        entityGrid.rebuild(entities);
    }

    /** Fills a caller-owned buffer with candidates overlapping one beam sweep. */
    public void query(Vec3 start, Vec3 end, double width, List<LivingEntity> result) {
        entityGrid.query(sweptBounds(start, end, width), result);
    }

    /** Builds the broad-phase volume for a beam, including its collision radius. */
    public static AABB sweptBounds(Vec3 start, Vec3 end, double width) {
        double margin = Math.max(0.0D, width * 0.5D);
        return new AABB(start, end).inflate(margin, margin, margin);
    }

    /** Sorts contacts by path position and keeps block-before-entity ordering on ties. */
    public static Comparator<Contact> contactOrder() {
        return Comparator.comparingDouble(Contact::parameter)
                .thenComparingInt(contact -> contact.type() == ContactType.BLOCK ? 0 : 1);
    }

    public enum ContactType {
        BLOCK,
        ENTITY
    }

    public record Contact(double parameter, ContactType type) {
    }
}

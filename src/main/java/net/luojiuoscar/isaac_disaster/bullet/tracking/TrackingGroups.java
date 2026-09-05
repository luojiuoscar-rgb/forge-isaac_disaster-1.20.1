package net.luojiuoscar.isaac_disaster.bullet.tracking;

import net.luojiuoscar.isaac_disaster.bullet.collision.EntityGrid;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shares nearby tracking candidates between compatible bullets in the same 8-block region.
 *
 * <p>Candidate lookup is deliberately a group operation. The individual bullets retain their own
 * target and steering result, so a target switch cannot overwrite another bullet's velocity.</p>
 */
public final class TrackingGroups {
    private final Map<Key, Group> groups = new HashMap<>();
    private final Map<BulletState, Group> memberships = new IdentityHashMap<>();
    private final List<LivingEntity> queryBuffer = new ArrayList<>();

    /** Removes this tick's groups before their active bullets are registered. */
    public void clear() {
        groups.clear();
        memberships.clear();
    }

    /** Registers a homing bullet under its owner, steering settings, and local spatial region. */
    public void add(BulletState bullet) {
        long ownerId = bullet.getOwner() == null ? -1L : bullet.getOwner().getId();
        int rangeCells = Math.max(1, (int) Math.ceil(bullet.homingRange() / 2.0D));
        int steeringMode = (int) Math.round(bullet.homingSteer() * 100.0D);
        Key key = new Key(ownerId, rangeCells, steeringMode,
                floorDiv(bullet.position().x, 8.0D), floorDiv(bullet.position().y, 8.0D), floorDiv(bullet.position().z, 8.0D));
        Group group = groups.computeIfAbsent(key, ignored -> new Group());
        group.add(bullet);
        memberships.put(bullet, group);
    }

    /** Queries the entity grid once per group and retains only a bounded candidate set. */
    public void refresh(EntityGrid grid) {
        for (Group group : groups.values()) {
            group.candidates.clear();
            AABB bounds = null;
            for (BulletState bullet : group.bullets) {
                Vec3 searchCenter = TrackingProfile.forBullet(bullet).searchCenter(bullet);
                AABB bulletBounds = new AABB(searchCenter, searchCenter).inflate(bullet.homingRange());
                bounds = bounds == null ? bulletBounds : bounds.minmax(bulletBounds);
            }
            if (bounds == null) continue;
            grid.query(bounds, queryBuffer);
            // Keep the complete spatial result. Priority-aware selection belongs to each bullet;
            // truncating here can discard a farther high-priority hostile target.
            group.candidates.addAll(queryBuffer);
        }
    }

    /** Gets the current candidate buffer for one bullet's tracking group. */
    public List<LivingEntity> candidates(BulletState bullet) {
        Group group = memberships.get(bullet);
        return group == null ? List.of() : group.candidates;
    }

    /** Returns the current tick's group count for diagnostics and tests. */
    public int size() { return groups.size(); }

    private static int floorDiv(double coordinate, double cellSize) {
        return (int) Math.floor(coordinate / cellSize);
    }

    private record Key(long ownerId, int rangeCells, int steeringMode, int regionX, int regionY, int regionZ) { }

    private static final class Group {
        private final List<BulletState> bullets = new ArrayList<>();
        private final List<LivingEntity> candidates = new ArrayList<>();

        private void add(BulletState bullet) { bullets.add(bullet); }
    }
}

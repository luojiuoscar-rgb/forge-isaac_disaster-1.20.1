package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerFamiliarDataProvider;
import net.luojiuoscar.isaac_disaster.helper.TrackingTargetRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.*;

/** Owner-wide decisions: one enemy scan and target-centric assignment pass every four ticks. */
final class SpiderOwnerFrame {
    private static final Map<ServerLevel, Map<UUID, WeakReference<SpiderOwnerFrame>>> FRAMES = new WeakHashMap<>();
    private final SpiderTargetAssignments<LivingEntity> assignments = new SpiderTargetAssignments<>();
    private final Set<AbstractSpiderFamiliarEntity> roster = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<LivingEntity, Boolean> validity = new IdentityHashMap<>();
    private final Map<SpiderTargetAssignments.Cell, List<LivingEntity>> contactCells = new HashMap<>();
    private final List<LivingEntity> largeTargets = new ArrayList<>();
    private List<LivingEntity> candidates = List.of();
    private long tick = Long.MIN_VALUE;
    private long scanTick = Long.MIN_VALUE;
    private int scanCount;
    private int assignmentPassCount;
    private int priorityEvaluationCount;

    static SpiderOwnerFrame get(ServerPlayer owner) {
        var frames = FRAMES.computeIfAbsent(owner.serverLevel(), ignored -> new HashMap<>());
        var reference = frames.get(owner.getUUID());
        var frame = reference == null ? null : reference.get();
        if (frame == null) {
            frames.entrySet().removeIf(entry -> entry.getValue().get() == null);
            frame = new SpiderOwnerFrame();
            frames.put(owner.getUUID(), new WeakReference<>(frame));
        }
        frame.refresh(owner, owner.level().getGameTime());
        return frame;
    }

    void refresh(ServerPlayer owner, long gameTick) {
        if (tick == gameTick) return;
        tick = gameTick;
        validity.clear();
        var previousRoster = new ArrayList<>(roster);
        roster.clear();
        // The capability is authoritative; do not query the world for every owner's spiders.
        owner.getCapability(PlayerFamiliarDataProvider.PLAYER_FAMILIAR_DATA).ifPresent(data -> {
            for (var entry : data.getEntries()) {
                for (UUID id : entry.getEntityIds()) {
                    if (owner.serverLevel().getEntity(id) instanceof AbstractSpiderFamiliarEntity spider
                            && available(owner, spider)) roster.add(spider);
                }
            }
        });
        for (var spider : previousRoster) {
            if (!roster.contains(spider)) assignments.remove(spider.getId());
        }
        if (scanTick == Long.MIN_VALUE || gameTick < scanTick || gameTick - scanTick >= 4) {
            scanTick = gameTick;
            scanCount++;
            Vec3 center = owner.getBoundingBox().getCenter();
            candidates = owner.serverLevel().getEntitiesOfClass(LivingEntity.class,
                    new AABB(center, center).inflate(8), candidate -> validForTick(owner, candidate));
            var points = new ArrayList<SpiderTargetAssignments.Spider>(roster.size());
            for (var spider : roster) {
                points.add(new SpiderTargetAssignments.Spider(spider.getId(), spider.position()));
            }
            var targets = new ArrayList<SpiderTargetAssignments.Target<LivingEntity>>(candidates.size());
            for (LivingEntity target : candidates) {
                priorityEvaluationCount++;
                targets.add(new SpiderTargetAssignments.Target<>(target, target.getId(),
                        TrackingTargetRules.priority(owner.serverLevel(), owner, target), target.getBoundingBox().getCenter()));
            }
            assignments.rebuild(points, targets);
            assignmentPassCount++;
        }
        rebuildContactCells();
    }

    private static boolean available(ServerPlayer owner, AbstractSpiderFamiliarEntity spider) {
        return !spider.isRemoved() && !spider.isRecallPending() && spider.level() == owner.level()
                && spider.getOwner() == owner;
    }

    LivingEntity attackTarget(ServerPlayer owner, AbstractSpiderFamiliarEntity spider, long gameTick) {
        refresh(owner, gameTick);
        if (!roster.contains(spider) || !available(owner, spider)) {
            remove(spider);
            return null;
        }
        LivingEntity target = assignments.target(spider.getId());
        return target != null && validForTick(owner, target) ? target : null;
    }

    /** Recalls clear the old group before the entity is admitted at its new position. */
    void remove(AbstractSpiderFamiliarEntity spider) {
        roster.remove(spider);
        assignments.remove(spider.getId());
    }

    boolean validForTick(ServerPlayer owner, LivingEntity target) {
        return target.isAlive() && !target.isRemoved() && target.level() == owner.level()
                && validity.computeIfAbsent(target, candidate -> validTarget(owner, candidate));
    }

    static boolean validTarget(ServerPlayer owner, LivingEntity target) {
        return target.level() == owner.level() && !target.isRemoved()
                && target.getBoundingBox().getCenter().distanceToSqr(owner.getBoundingBox().getCenter()) <= 64
                && TrackingTargetRules.eligibleFlyOrSpider(owner.serverLevel(), owner, target);
    }

    private void rebuildContactCells() {
        contactCells.clear();
        largeTargets.clear();
        for (LivingEntity target : candidates) {
            if (!target.isAlive() || target.isRemoved()) continue;
            AABB box = target.getBoundingBox();
            int minX = cell(box.minX), maxX = cell(box.maxX);
            int minY = cell(box.minY), maxY = cell(box.maxY);
            int minZ = cell(box.minZ), maxZ = cell(box.maxZ);
            // Huge scaled entities must not allocate a cell for their entire volume.
            if (tooManyCells(minX, maxX, minY, maxY, minZ, maxZ)) {
                largeTargets.add(target);
                continue;
            }
            for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++) {
                contactCells.computeIfAbsent(new SpiderTargetAssignments.Cell(x, y, z), ignored -> new ArrayList<>()).add(target);
            }
        }
    }

    /** Bounds include the whole moving spider body, including stationary overlap. */
    List<LivingEntity> contactCandidates(AABB sweptBounds) {
        if (contactCells.isEmpty() && largeTargets.isEmpty()) return List.of();
        Set<LivingEntity> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        var result = new ArrayList<LivingEntity>();
        int minX = cell(sweptBounds.minX), maxX = cell(sweptBounds.maxX);
        int minY = cell(sweptBounds.minY), maxY = cell(sweptBounds.maxY);
        int minZ = cell(sweptBounds.minZ), maxZ = cell(sweptBounds.maxZ);
        // Normal movement visits only a few cells; long diagnostic sweeps stay bounded too.
        if (tooManyCells(minX, maxX, minY, maxY, minZ, maxZ)) {
            for (LivingEntity target : candidates) collectContact(target, sweptBounds, seen, result);
        } else {
            for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++) {
                var occupants = contactCells.get(new SpiderTargetAssignments.Cell(x, y, z));
                if (occupants != null) for (LivingEntity target : occupants) collectContact(target, sweptBounds, seen, result);
            }
            for (LivingEntity target : largeTargets) collectContact(target, sweptBounds, seen, result);
        }
        result.sort(Comparator.comparingInt(LivingEntity::getId));
        return result;
    }

    private static void collectContact(LivingEntity target, AABB bounds, Set<LivingEntity> seen, List<LivingEntity> result) {
        if (!seen.add(target)) return;
        AABB box = target.getBoundingBox();
        if (bounds.minX <= box.maxX && bounds.maxX >= box.minX
                && bounds.minY <= box.maxY && bounds.maxY >= box.minY
                && bounds.minZ <= box.maxZ && bounds.maxZ >= box.minZ) result.add(target);
    }

    private static int cell(double coordinate) { return SpiderTargetAssignments.cell(coordinate); }

    private static boolean tooManyCells(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        long width = (long) maxX - minX + 1;
        long height = (long) maxY - minY + 1;
        long depth = (long) maxZ - minZ + 1;
        return width > 512 || height > 512 || depth > 512 || width * height * depth > 512;
    }
    int scans() { return scanCount; }
    int assignmentPasses() { return assignmentPassCount; }
    int priorityEvaluations() { return priorityEvaluationCount; }
}

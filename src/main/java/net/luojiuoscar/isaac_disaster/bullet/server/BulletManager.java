package net.luojiuoscar.isaac_disaster.bullet.server;

import net.luojiuoscar.isaac_disaster.bullet.collision.EntityGrid;
import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.luojiuoscar.isaac_disaster.bullet.collision.VoxelDda;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletSteeringMode;
import net.luojiuoscar.isaac_disaster.bullet.tracking.TrackingGroups;
import net.luojiuoscar.isaac_disaster.bullet.tracking.TrackingProfile;
import net.luojiuoscar.isaac_disaster.event.ForgeEvents;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletCorrectionS2CPacket;
import net.luojiuoscar.isaac_disaster.networking.packet.bullet.BulletShatterS2CPacket;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletEndOfLifeEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletTickEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.BulletSourceType;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitExecutor;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/** Owns transient bullet states and applies lifecycle changes after the active traversal. */
public final class BulletManager {
    private final int streamEpoch;
    private final List<BulletState> slots = new ArrayList<>();
    private final List<Integer> active = new ArrayList<>();
    private final ArrayDeque<Integer> free = new ArrayDeque<>();
    private final List<Runnable> deferred = new ArrayList<>();
    private final Set<Long> deferredRemovalIds = new HashSet<>();
    private int[] generations = new int[0];
    private int[] activeIndex = new int[0];
    private final EntityGrid entityGrid = new EntityGrid();
    private final TrackingGroups trackingGroups = new TrackingGroups();
    private final List<LivingEntity> entityCandidates = new ArrayList<>();
    private final Map<Long, BulletCorrectionS2CPacket> pendingCorrections = new LinkedHashMap<>();
    private final List<Long> pendingDespawns = new ArrayList<>();
    private final List<BulletShatterS2CPacket.Entry> pendingShatters = new ArrayList<>();

    /** Creates a manager for one transient server stream. */
    public BulletManager() { this(0); }

    /** Creates a manager whose sparse packets carry the supplied stream epoch. */
    public BulletManager(int streamEpoch) { this.streamEpoch = streamEpoch; }

    /** Allocates a slot and adds a state to the active list in O(1) amortized time. */
    public BulletState spawn(BulletState state) {
        int slot;
        if (free.isEmpty()) {
            slot = slots.size();
            slots.add(null);
            int[] next = new int[slots.size()];
            System.arraycopy(generations, 0, next, 0, generations.length);
            generations = next;
            int[] nextIndex = new int[slots.size()];
            System.arraycopy(activeIndex, 0, nextIndex, 0, activeIndex.length);
            activeIndex = nextIndex;
        } else slot = free.removeFirst();
        int generation = ++generations[slot];
        state.assignSlot(slot, generation);
        slots.set(slot, state);
        activeIndex[slot] = active.size();
        active.add(slot);
        return state;
    }

    /** Recycles a state slot if its generation still matches the live entry. */
    public boolean remove(BulletState state) {
        if (state == null) return false;
        int slot = state.slot();
        if (slot < 0
                || slot >= slots.size()
                || slots.get(slot) != state
                || generations[slot] != state.generation()) return false;

        slots.set(slot, null);
        int index = activeIndex[slot];
        int last = active.remove(active.size() - 1);
        if (index < active.size()) {
            active.set(index, last);
            activeIndex[last] = index;
        }
        activeIndex[slot] = -1;
        free.addFirst(slot);
        return true;
    }

    /** Removes every transient state and invalidates queued work without executing effect callbacks. */
    public List<Long> clear() {
        List<Long> removed = new ArrayList<>(active.size());
        deferred.clear();
        deferredRemovalIds.clear();
        pendingCorrections.clear();
        pendingDespawns.clear();
        pendingShatters.clear();
        for (int slot : active) {
            BulletState state = slots.get(slot);
            if (state != null) {
                removed.add(((long) state.generation() << 32) | (state.slot() & 0xFFFFFFFFL));
                slots.set(slot, null);
                activeIndex[slot] = -1;
                free.addFirst(slot);
            }
        }
        active.clear();
        return removed;
    }

    /** Queues a collection mutation for application after the active traversal. */
    public void defer(Runnable action) { deferred.add(action); }

    /** Queues one idempotent removal while preserving deferred split/effect ordering. */
    private void deferRemoval(BulletState state) {
        if (state == null) return;
        long identity = identity(state.slot(), state.generation());
        if (deferredRemovalIds.add(identity)) deferred.add(() -> {
            if (remove(state)) pendingDespawns.add(identity);
            deferredRemovalIds.remove(identity);
        });
    }

    /** Advances all active states and applies deferred lifecycle mutations. */
    public void tick() {
        for (Integer integer : active) {
            BulletState state = slots.get(integer);
            if (state == null || !state.tickPhysics()) deferRemoval(state);
        }

        for (Runnable action : List.copyOf(deferred)) action.run();
        deferred.clear();
    }

    /** Runs the authoritative movement, tracking, block sweep, and living-entity sweep for a level. */
    public void tick(ServerLevel level) {
        if (active.isEmpty()) return;
        pendingCorrections.clear();
        pendingDespawns.clear();
        AABB queryBounds = null;

        for (Integer integer : active) {
            BulletState bullet = slots.get(integer);
            if (bullet == null) continue;
            double searchMargin = Math.max(16.0D, bullet.velocity().length() + bullet.size() + bullet.homingRange());
            AABB bounds = new AABB(bullet.position(), bullet.position()).inflate(searchMargin);
            queryBounds = queryBounds == null ? bounds : queryBounds.minmax(bounds);
        }
        if (queryBounds != null) {
            entityGrid.rebuild(level.getEntitiesOfClass(LivingEntity.class, queryBounds, LivingEntity::isAlive));
        }
        boolean steeringTick = (level.getGameTime() & 3L) == 0L;
        trackingGroups.clear();

        for (Integer integer : active) {
            BulletState bullet = slots.get(integer);
            if (bullet == null || !(bullet.isHoming() || bullet.isControllable())) continue;
            bullet.tickTargetSearchCooldown();
            if (steeringTick && bullet.isHoming() && needsTrackingTarget(bullet) && bullet.canSearchTrackingTarget())
                trackingGroups.add(bullet);
        }
        if (steeringTick) {
            trackingGroups.refresh(entityGrid);
        }

        for (Integer integer : active) {
            BulletState bullet = slots.get(integer);
            if (bullet == null) continue;
            MinecraftForge.EVENT_BUS.post(new BulletTickEvent(bullet));
            if (bullet.isHoming() || bullet.isControllable()) updateTracking(bullet, steeringTick);
            if (bullet.isHoming() || bullet.isControllable()) applySteering(bullet);
            Vec3 start = bullet.position();
            if (!bullet.tickPhysics()) {
                bullet.recordSplitTrigger(SplitTriggerType.END_OF_LIFE);
                defer(() -> SplitExecutor.execute(bullet, SplitTriggerType.END_OF_LIFE));
                MinecraftForge.EVENT_BUS.post(new TearBulletEndOfLifeEvent(bullet));
                pendingShatters.add(shatterEntry(bullet, start, bullet.velocity()));
                deferRemoval(bullet);
                continue;
            }
            Vec3 end = bullet.position();
            resolveCollisions(level, start, end, bullet);
        }
        for (Runnable action : List.copyOf(deferred)) action.run();
        deferred.clear();
    }

    private void updateTracking(BulletState bullet, boolean allowSearch) {
        bullet.setSteeringTarget(null, false);
        TrackingProfile profile = TrackingProfile.forBullet(bullet);
        LivingEntity target = bullet.trackingTarget();
        if (needsTrackingTarget(bullet) && allowSearch) {
            if (!bullet.canSearchTrackingTarget() && !bullet.isControllable()) return;
            target = null;
            int bestPriority = Integer.MAX_VALUE;
            double best = Double.MAX_VALUE;
            Vec3 searchCenter = profile.searchCenter(bullet);
            for (LivingEntity candidate : trackingGroups.candidates(bullet)) {
                if (candidate == bullet.getOwner() || EntityHelper.isFriendly(candidate, bullet.getOwner())
                        || bullet.rememberHitTargets() && bullet.getDamagedEntities().contains(candidate.getUUID())) continue;
                if (!(bullet.getOwner() != null && bullet.getOwner().level() instanceof ServerLevel level)
                        || !profile.eligible(level, bullet.getOwner(), candidate)) continue;
                if (candidate.distanceToSqr(bullet.position().x, bullet.position().y, bullet.position().z)
                        > profile.range() * profile.range()) continue;
                int priority = TrackingProfile.priority((ServerLevel) bullet.getOwner().level(), bullet.getOwner(), candidate);
                double distance = candidate.distanceToSqr(searchCenter.x, searchCenter.y, searchCenter.z);
                if (priority < bestPriority || priority == bestPriority && distance < best) {
                    bestPriority = priority; best = distance; target = candidate;
                }
            }
            bullet.setTrackingTarget(target);
            bullet.setTargetSearchCooldown(4);
        }
        if (target != null) {
            Vec3 targetPosition = profile.targetCenter(target);
            bullet.setSteeringTarget(targetPosition, false);
            bullet.setDesiredVelocity(profile.desiredVelocity(bullet, targetPosition, false));
        } else if (bullet.isControllable() && bullet.getOwner() != null) {
            LivingEntity owner = bullet.getOwner();
            Vec3 controlPoint = owner.getEyePosition().add(owner.getLookAngle().scale(bullet.controlRange()));
            bullet.setSteeringTarget(controlPoint, true);
            bullet.setDesiredVelocity(profile.desiredVelocity(bullet, controlPoint, true));
        } else {
            bullet.setDesiredVelocity(profile.cruiseVelocity(bullet));
        }
    }

    /** Applies the selected steering mode; direct Fetus tracking refreshes its target point every tick. */
    private static void applySteering(BulletState bullet) {
        if (bullet.steeringMode() != BulletSteeringMode.DIRECT
                && (bullet.trackingTarget() == null || !bullet.trackingTarget().isAlive())) {
            // A lost homing target should not leave the last slowed desired velocity
            // active until the next four-tick search pass.
            bullet.setDesiredVelocity(TrackingProfile.forBullet(bullet).cruiseVelocity(bullet));
        }
        bullet.applySteering();
    }


    /** Determines whether the currently cached target must be reselected from the spatial index. */
    private static boolean needsTrackingTarget(BulletState bullet) {
        LivingEntity target = bullet.trackingTarget();
        return target == null || !target.isAlive()
                || target.distanceToSqr(bullet.position().x, bullet.position().y, bullet.position().z)
                > bullet.homingRange() * bullet.homingRange();
    }

    /** Resolves block and entity sweeps together so the earliest contact wins. */
    private void resolveCollisions(ServerLevel level, Vec3 start, Vec3 end, BulletState bullet) {
        BlockSweep block = bullet.isSpectral() && bullet.getSplitTriggerCounts().getBlockHits() > 0
                ? null : findBlock(level, start, end, bullet);
        List<EntitySweep> entities = findEntities(start, end, bullet);
        for (EntitySweep entity : entities) {
            if (!bullet.isSpectral() && block != null && entity.parameter() >= block.parameter()) break;
            if (!bullet.isAlive()) return;
            Vec3 point = start.lerp(end, entity.parameter());
            bullet.setPosition(point);
            if (!handleEntity(level, point, entity.target(), bullet)) return;
        }
        if (block != null && bullet.isAlive()) {
            if (bullet.isSpectral()) handleSpectralBlockContact(block, bullet);
            else handleBlock(level, block, bullet);
            if (bullet.isSpectral() && bullet.isAlive()) bullet.setPosition(end);
        } else if (bullet.isAlive()) bullet.setPosition(end);
    }

    /** Records a one-time block-contact split for bullets that intentionally pass through blocks. */
    private void handleSpectralBlockContact(BlockSweep block, BulletState bullet) {
        if (bullet.getSplitTriggerCounts().getBlockHits() > 0) {
            return;
        }
        Vec3 point = block.hit().getLocation();
        bullet.setPosition(point);
        bullet.setLastBlockHit(block.hit());
        bullet.recordSplitTrigger(SplitTriggerType.BLOCK);
        Vec3 splitVelocity = bullet.velocity();
        defer(() -> SplitExecutor.executeAt(bullet, SplitTriggerType.BLOCK, point, splitVelocity));
    }

    /** Finds the earliest block contact using a dimension-expanded voxel sweep. */
    private BlockSweep findBlock(ServerLevel level, Vec3 start, Vec3 end, BulletState bullet) {
        final BlockSweep[] hit = {null};
        double halfWidth = bullet.collisionWidth() * 0.5D;
        double halfHeight = bullet.collisionHeight() * 0.5D;
        int horizontalPadding = (int) Math.ceil(halfWidth);
        int verticalPadding = (int) Math.ceil(halfHeight);
        VoxelDda.traverse(start, end, (x, y, z, t) -> {
            for (int offsetX = -horizontalPadding; offsetX <= horizontalPadding; offsetX++) {
                for (int offsetY = -verticalPadding; offsetY <= verticalPadding; offsetY++) {
                    for (int offsetZ = -horizontalPadding; offsetZ <= horizontalPadding; offsetZ++) {
                        considerBlock(level, start, end, halfWidth, halfHeight, new BlockPos(x + offsetX, y + offsetY, z + offsetZ), hit);
                    }
                }
            }
            return false;
        });
        return hit[0];
    }

    /** Tests one DDA-adjacent block and retains the earliest dimension-expanded collision. */
    private static void considerBlock(ServerLevel level, Vec3 start, Vec3 end, double halfWidth, double halfHeight,
                                      BlockPos pos, BlockSweep[] best) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;
        if (state.isSolidRender(level, pos)) {
            considerBlockBox(start, end, halfWidth, halfHeight, pos,
                    new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), best);
            return;
        }
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) return;
        for (AABB part : shape.toAabbs()) {
            considerBlockBox(start, end, halfWidth, halfHeight, pos, part, best);
        }
    }

    /** Tests one collision box after applying the bullet's horizontal and vertical extents. */
    private static void considerBlockBox(Vec3 start, Vec3 end, double halfWidth, double halfHeight,
                                         BlockPos pos, AABB box, BlockSweep[] best) {
        AABB expanded = box.move(pos).inflate(halfWidth, halfHeight, halfWidth);
        SweptCollision.SegmentHit sweep = SweptCollision.segmentAabbDetailed(start, end, expanded);
        if (!sweep.hit() || sweep.outwardFace() == null
                || best[0] != null && sweep.parameter() >= best[0].parameter()) return;
        Vec3 point = start.lerp(end, sweep.parameter());
        best[0] = new BlockSweep(new BlockHitResult(point, sweep.outwardFace(), pos, false), sweep.parameter());
    }

    private void handleBlock(ServerLevel level, BlockSweep block, BulletState bullet) {
        BlockHitResult result = block.hit();
            Vec3 point = result.getLocation();
            bullet.setPosition(point);
            // Effects observe a stable non-overlapping center. The hit result still
            // retains the exact contact point for damage and split callbacks.
            bullet.pushOutOfBlock(Vec3.atLowerCornerOf(result.getDirection().getNormal()));
            bullet.setLastBlockHit(result);
            bullet.recordSplitTrigger(SplitTriggerType.BLOCK);
            Vec3 splitPosition = point;
            Vec3 splitVelocity = bullet.velocity();
            defer(() -> SplitExecutor.executeAt(bullet, SplitTriggerType.BLOCK, splitPosition, splitVelocity));
            boolean canceled = MinecraftForge.EVENT_BUS.post(new IsaacAttackHitBlockEvent(
                    bullet, bullet.getOwner(), ModAttackTypes.BULLET.getId(), bullet.getTriggers(), result));
            if (!canceled) {
                pendingShatters.add(shatterEntry(bullet, point, splitVelocity));
                bullet.kill();
                deferRemoval(bullet);
            }
            else queueCorrection(bullet, 0.65F);
    }

    private List<EntitySweep> findEntities(Vec3 start, Vec3 end, BulletState bullet) {
        AABB bounds = new AABB(start, end).inflate(Math.max(0.01, bullet.collisionWidth() * 0.5D),
                Math.max(0.01, bullet.collisionHeight() * 0.5D), Math.max(0.01, bullet.collisionWidth() * 0.5D));
        List<EntitySweep> hits = new ArrayList<>();
        entityGrid.query(bounds, entityCandidates);
        for (LivingEntity target : entityCandidates) {
            if (target == bullet.getOwner() || !target.isAlive() || EntityHelper.isFriendly(target, bullet.getOwner())
                    || bullet.rememberHitTargets() && bullet.getDamagedEntities().contains(target.getUUID()) || bullet.hitCooldownTicks() > 0) continue;
            SweptCollision.OptionalDoubleHit sweep = SweptCollision.segmentAabb(start, end,
                    target.getBoundingBox().inflate(bullet.collisionWidth() * 0.5D, bullet.collisionHeight() * 0.5D, bullet.collisionWidth() * 0.5D));
            if (!sweep.hit()) continue;
            hits.add(new EntitySweep(target, sweep.parameter()));
        }
        hits.sort(Comparator.comparingDouble(EntitySweep::parameter));
        return hits;
    }

    private boolean handleEntity(ServerLevel level, Vec3 point, LivingEntity target, BulletState bullet) {
            if (!bullet.isAlive()) return false;
            EntityHitResult hit = new EntityHitResult(target, point);
            IsaacAttackBeforeHitEntityEvent before = new IsaacAttackBeforeHitEntityEvent(bullet, bullet.getOwner(), ModAttackTypes.BULLET.getId(), bullet.getTriggers(), hit, bullet.getDamage());
            if (MinecraftForge.EVENT_BUS.post(before)) return true;
            // FetusBullet's attack interval is checked by applyDamage. Recheck it here because
            // all candidates are swept before event dispatch, so a later same-tick candidate must
            // observe the cooldown set by an earlier successful hit.
            if (bullet.hitCooldownTicks() > 0) return true;
            ForgeEvents.registerOptimizedTearKnockback(target, bullet.velocity());
            if (target.hurt(tearDamageSource(level, bullet), (float) before.getDamage())) {
                if (bullet.rememberHitTargets()) bullet.getDamagedEntities().add(target.getUUID());
                bullet.setHitCooldownTicks(bullet.getSourceType() == net.luojiuoscar.isaac_disaster.registries.attack_type.BulletSourceType.FETUS_BULLET ? 6 : 0);
                bullet.recordSplitTrigger(SplitTriggerType.ENTITY);
                Vec3 splitPosition = point;
                Vec3 splitVelocity = bullet.velocity();
                defer(() -> SplitExecutor.executeAt(bullet, SplitTriggerType.ENTITY, splitPosition, splitVelocity));
                boolean canceled = MinecraftForge.EVENT_BUS.post(new IsaacAttackAfterHitEvent(
                        bullet, bullet.getOwner(), ModAttackTypes.BULLET.getId(), bullet.getTriggers(), hit,
                        before.getDamage(), target.getHealth()));
                if (!bullet.isPiercing() && !canceled) {
                    pendingShatters.add(shatterEntry(bullet, point, splitVelocity));
                    bullet.kill();
                    deferRemoval(bullet);
                }
                else if (canceled) queueCorrection(bullet, 0.65F);
                if (canceled) return false;
            }
            return true;
    }

    /** Builds the registered tear damage source while deliberately omitting a fabricated projectile entity. */
    private static DamageSource tearDamageSource(ServerLevel level, BulletState bullet) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModDamageType.TEAR), null, bullet.getOwner());
    }

    /** Coalesces same-tick redirects so each slot/generation receives at most one correction. */
    private void queueCorrection(BulletState bullet, float blend) {
        long identity = ((long) bullet.generation() << 32) | (bullet.slot() & 0xFFFFFFFFL);
        pendingCorrections.put(identity, new BulletCorrectionS2CPacket(
                streamEpoch, bullet.slot(), bullet.generation(), bullet.position(), bullet.velocity(), blend));
    }

    /** One candidate hit ordered by its parameter along this tick's movement segment. */
    private record EntitySweep(LivingEntity target, double parameter) { }
    private record BlockSweep(BlockHitResult hit, double parameter) { }

    public int activeCount() { return active.size(); }
    public int capacity() { return slots.size(); }
    public BulletState get(int slot, int generation) {
        return slot >= 0 && slot < slots.size() && generations[slot] == generation ? slots.get(slot) : null;
    }
    public List<BulletState> activeStates() {
        List<BulletState> result = new ArrayList<>(active.size());
        for (int slot : active) result.add(slots.get(slot));
        return List.copyOf(result);
    }

    /** Returns and clears despawn identities produced by the last authoritative tick. */
    public List<Long> drainDespawns() {
        if (pendingDespawns.isEmpty()) return List.of();
        List<Long> result = List.copyOf(pendingDespawns);
        pendingDespawns.clear();
        return result;
    }

    /** Returns and clears sparse corrections produced by the last authoritative tick. */
    public List<BulletCorrectionS2CPacket> drainCorrections() {
        if (pendingCorrections.isEmpty()) return List.of();
        List<BulletCorrectionS2CPacket> result = List.copyOf(pendingCorrections.values());
        pendingCorrections.clear();
        return result;
    }

    /** Returns and clears shatter visuals generated by the last authoritative tick. */
    public List<BulletShatterS2CPacket.Entry> drainShatters() {
        if (pendingShatters.isEmpty()) return List.of();
        List<BulletShatterS2CPacket.Entry> result = List.copyOf(pendingShatters);
        pendingShatters.clear();
        return result;
    }

    private static BulletShatterS2CPacket.Entry shatterEntry(BulletState bullet, Vec3 position, Vec3 velocity) {
        return new BulletShatterS2CPacket.Entry(position, velocity, bullet.renderScale(), bullet.color(), bullet.alpha(),
                new ArrayList<>(bullet.visualIds()), bullet.getSourceType() == BulletSourceType.FETUS_BULLET);
    }

    /** Packs the reusable slot and its generation into the stream identity format. */
    private static long identity(int slot, int generation) {
        return ((long) generation << 32) | (slot & 0xFFFFFFFFL);
    }
}

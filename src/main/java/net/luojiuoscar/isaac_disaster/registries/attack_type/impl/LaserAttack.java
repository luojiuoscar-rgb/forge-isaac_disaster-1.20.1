package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.function.IntUnaryOperator;
import net.luojiuoscar.isaac_disaster.bullet.collision.LaserCollisionBatch;
import net.luojiuoscar.isaac_disaster.bullet.collision.SweptCollision;
import net.luojiuoscar.isaac_disaster.bullet.collision.VoxelDda;
import net.luojiuoscar.isaac_disaster.networking.ModMessages;
import net.luojiuoscar.isaac_disaster.networking.packet.laser.LaserBeamBatchS2CPacket;
import net.luojiuoscar.isaac_disaster.bullet.core.TrajectoryEvaluator;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.helper.ProjectileCollisionHelper;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.LaserAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.util.DamagedEntities;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitExecutor;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerCounts;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryMotion;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryRuntime;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class LaserAttack extends AttackType {
    private static final LaserAttackPattern PATTERN = new LaserAttackPattern();
    /** Hard upper bound for one synchronous laser attack. */
    private static final int MAX_LASER_STEPS = 4_096;
    private static final double BASE_LASER_WIDTH = 0.25D;
    private static final double BASE_HOMING_TURN = 0.25D;
    private static final double MAX_HOMING_COLLISION_ANGLE = 0.5D;
    private final ThreadLocal<Map<ServerLevel, LaserCollisionBatch>> activeCollisionBatches =
        new ThreadLocal<>();
    private final ThreadLocal<VisualBatchState> activeVisualBatch = new ThreadLocal<>();
    private static final ThreadLocal<Integer> STRAIGHT_PATH_DEPTH =
        ThreadLocal.withInitial(() -> 0);

    public LaserAttack(int priorityTier, double priority) {
        super(priorityTier, priority);
    }

    public LaserAttack(double priority) {
        super(priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackTypes.LASER.getId();
    }

    /** All laser-derived attack definitions share the laser trajectory family. */
    @Override
    public ResourceLocation getRootId() {
        return ModAttackTypes.LASER.getId();
    }

    @Override
    public void makeSound(LivingEntity entity) {
        entity
            .level()
            .playSound(
                null,
                entity.blockPosition(),
                ModSounds.LASER_SHOT.get(),
                SoundSource.PLAYERS,
                0.6f,
                1.0f);
    }

    // ================= LaserProjectile 封装 =================
    public static class LaserProjectile implements IBulletObject {
        public Vec3 position;
        public Vec3 direction;
        public double traveled;
        public double step;
        public double width;
        public float damage;
        public boolean homing;
        public boolean spectral;
        private final DamagedEntities damagedEntities = new DamagedEntities();
        public final LivingEntity owner;
        public final Entity shooter;
        public boolean isCurrentlyHoming;
        public int tickCount;
        public LivingEntity homingTarget;
        private final AttackContext attackContext;
        private final CompositeTrigger triggers;
        private final SplitSequence splitSequence;
        private int attackSequenceIndex;
        private final double range;
        private final SplitTriggerCounts splitTriggerCounts = new SplitTriggerCounts();
        private final Set<BlockPos> hitBlockPositions = new HashSet<>();
        private BlockHitResult lastBlockHit;
        private final TrajectoryRuntime trajectoryRuntime;

        public LaserProjectile(AttackContext attackContext) {
            this.owner = attackContext.getOwner();
            this.shooter = attackContext.getShooter();
            this.position = attackContext.getPos();
            this.direction = attackContext.getMainAxis();
            this.damage = attackContext.getDamage();
            this.homing = false;
            this.spectral = false;
            this.traveled = 0;
            this.isCurrentlyHoming = false;
            this.tickCount = 0;
            this.homingTarget = null;
            this.attackContext = attackContext.copy();
            this.triggers = attackContext.copyTrigger();
            this.splitSequence = attackContext.copySplitSequence();
            this.attackSequenceIndex = 0;
            this.range = attackContext.getBulletRange();
            this.lastBlockHit = null;
            this.hitBlockPositions.addAll(attackContext.getHitBlockPositions());

            this.trajectoryRuntime =
                TrajectoryRuntime.forSpawn(
                    this.position,
                    this.direction,
                    attackContext.getTrajectorySpecs(),
                    this.shooter,
                    attackContext.getInheritedTrajectorySnapshot());
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public void setStep(double step) {
            this.step = step;
        }

        public void setHoming(boolean homing) {
            this.homing = homing;
        }

        public void setSpectral(boolean spectral) {
            this.spectral = spectral;
        }

        public void setAttackSequenceIndex(int attackSequenceIndex) {
            this.attackSequenceIndex = attackSequenceIndex;
        }

        @Override
        public void setLastBlockHit(@Nullable BlockHitResult lastBlockHit) {
            this.lastBlockHit = lastBlockHit;
        }

        @Override
        public AttackContext getAttackContext() {
            return attackContext.toBuilder()
                    .hitBlockPositions(hitBlockPositions)
                    .inheritTrajectorySnapshot(trajectoryRuntime.snapshot())
                    .build();
        }

        @Override
        public ResourceLocation getTypeId() {
            return attackContext.getTypeId();
        }

        @Override
        public ResourceLocation getRootTypeId() {
            return attackContext.getRootTypeId();
        }

        @Override
        public TrajectoryRuntime getTrajectoryRuntime() {
            return trajectoryRuntime;
        }

        @Override
        public int getTrajectoryAge() {
            return tickCount;
        }

        public int getAttackSequenceIndex() {
            return attackSequenceIndex;
        }

        @Nullable
        @Override
        public BlockHitResult getLastBlockHit() {
            return lastBlockHit;
        }

        @Override
        public SplitSequence getSplitSequence() {
            return splitSequence;
        }

        @Override
        public SplitTriggerCounts getSplitTriggerCounts() {
            return splitTriggerCounts.copy();
        }

        @Override
        public void recordSplitTrigger(SplitTriggerType type) {
            splitTriggerCounts.increment(type);
        }

        /** Returns a read-only snapshot of exact blocks contacted during this laser shot. */
        @Override
        public Set<BlockPos> getHitBlockPositions() {
            return Set.copyOf(hitBlockPositions);
        }

        /** Records one exact block contact and rejects repeated contacts to that block. */
        @Override
        public boolean markBlockHit(@Nullable BlockPos position) {
            return position != null && hitBlockPositions.add(position.immutable());
        }

        @Override
        public float getDamage() {
            return this.damage;
        }

        @Override
        public Vec3 getVelocity() {
            return this.direction;
        }

        @Override
        public double getTraveled() {
            return this.traveled;
        }

        @Override
        public double getRange() {
            return range;
        }

        @Override
        public Vec3 getPosition() {
            return this.position;
        }

        /** Updates the laser collision-center position. */
        @Override
        public void setCenter(Vec3 center) {
            if (center != null) this.position = center;
        }

        /** Updates the laser position. */
        @Override
        public void setPosition(Vec3 position) {
            if (position != null) this.position = position;
        }

        /** Updates the laser direction/velocity. */
        @Override
        public void setVelocity(Vec3 velocity) {
            if (velocity != null) this.direction = velocity;
        }

        @Override
        public float getCollisionWidth() {
            return (float) width;
        }

        @Override
        public float getCollisionHeight() {
            return (float) width;
        }

        @Nullable
        @Override
        public LivingEntity getOwner() {
            return this.owner;
        }

        @Nullable
        @Override
        public Object getShooter() {
            return this.shooter;
        }

        @Override
        public boolean noGravity() {
            return true;
        }

        @Override
        public boolean isHoming() {
            return this.homing;
        }

        @Override
        public boolean isSpectral() {
            return this.spectral;
        }

        @Override
        public boolean isControllable() {
            return false;
        }

        @Override
        public boolean isPiercing() {
            return true;
        }

        @Override
        public ResourceLocation getColorId() {
            return attackContext.getColorRl();
        }

        @Override
        public CompositeTrigger getTriggers() {
            return triggers;
        }

        @Override
        public DamagedEntities getDamagedEntities() {
            return damagedEntities;
        }
    }

    // ================== handleAttack ==================
    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        AttackContext ctx = createAttackContext(player, player);
        if (ctx == null) return List.of();
        return PATTERN.generate(new AttackPatternContext(ctx, bulletCount));
    }

    @Override
    public void performAttack(List<AttackContext> ctxList) {
        performLaserBatch(ctxList, ignored -> 0);
    }

    /** Executes a group of already prepared laser contexts under one shared collision batch. */
    protected final void performLaserBatch(
        List<AttackContext> ctxList, IntUnaryOperator sequenceIndexProvider) {
        if (ctxList == null || ctxList.isEmpty()) return;
        if (STRAIGHT_PATH_DEPTH.get() > 0) {
            for (int index = 0; index < ctxList.size(); index++) {
                AttackContext ctx = ctxList.get(index);
                if (ctx != null) shootSingleLaser(ctx, sequenceIndexProvider.applyAsInt(index));
            }
            return;
        }

        Map<ServerLevel, LaserCollisionBatch> batches = new IdentityHashMap<>();
        for (AttackContext ctx : ctxList) {
            if (ctx == null || !isStraightPathCandidate(ctx)) continue;
            ServerLevel level = getLaserLevel(ctx.getOwner());
            if (level == null) continue;
            Vec3 direction = ctx.getMainAxis();
            if (direction.lengthSqr() <= 1.0E-8D) continue;
            LaserCollisionBatch batch = batches.computeIfAbsent(level, ignored -> new LaserCollisionBatch());
            Vec3 start = ctx.getPos();
            Vec3 end = start.add(direction.normalize().scale(ctx.getBulletRange()));
            batch.addSweep(start, end, getWidth(ctx));
        }
        for (Map.Entry<ServerLevel, LaserCollisionBatch> entry : batches.entrySet()) {
            entry.getValue().rebuild(entry.getKey());
        }
        Map<ServerLevel, LaserCollisionBatch> previousBatches = activeCollisionBatches.get();
        VisualBatchState previousVisualBatch = activeVisualBatch.get();
        VisualBatchState visualBatch = previousVisualBatch == null
            ? new VisualBatchState() : previousVisualBatch;
        activeCollisionBatches.set(batches);
        activeVisualBatch.set(visualBatch);
        try {
            for (int index = 0; index < ctxList.size(); index++) {
                AttackContext ctx = ctxList.get(index);
                if (ctx != null) shootSingleLaser(ctx, sequenceIndexProvider.applyAsInt(index));
            }
        } finally {
            if (previousVisualBatch == null) visualBatch.flush();
            if (previousBatches == null) activeCollisionBatches.remove();
            else activeCollisionBatches.set(previousBatches);
            if (previousVisualBatch == null) activeVisualBatch.remove();
            else activeVisualBatch.set(previousVisualBatch);
        }
    }

    private boolean isStraightPathCandidate(AttackContext context) {
        return !isHoming(context.getOwner())
            && context.getTrajectorySpecs().isEmpty()
            && context.getMainAxis().lengthSqr() > 1.0E-8D;
    }

    // ================== shotLaser ==================
    @Override
    public void shoot(AttackContext ctx) {
        shootSingleLaser(ctx, 0);
    }

    protected void shootSingleLaser(AttackContext ctx, int attackSequenceIndex) {
        LivingEntity entity = ctx.getOwner();
        ServerLevel level = getLaserLevel(entity);
        if (level == null) return;

        Vec3 direction = ctx.getMainAxis();
        double width = getWidth(ctx);

        LaserProjectile laser = new LaserProjectile(ctx);
        laser.direction = direction;
        laser.setWidth(width);
        laser.setStep(Math.max(0.5, width * 2));
        laser.setHoming(isHoming(entity));
        laser.setSpectral(isSpectral(entity));
        laser.setAttackSequenceIndex(attackSequenceIndex);

        // Child lasers created during a split share the outer traversal's thread-local state.
        // They must use the compatibility path instead of rebuilding or consuming the parent's
        // straight-path batch recursively.
        if (STRAIGHT_PATH_DEPTH.get() > 0) {
            runSegmentedLaser(laser, level, ctx);
            return;
        }

        LaserCollisionBatch batch = activeBatch(level);
        if (batch == null && isStraightPathCandidate(ctx)) {
            batch = new LaserCollisionBatch();
            batch.addSweep(laser.position,
                laser.position.add(laser.direction.normalize().scale(laser.range)), width);
            batch.rebuild(level);
        }

        if (batch != null && canUseStraightPath(laser)) {
            VisualBatchState previousVisualBatch = activeVisualBatch.get();
            VisualBatchState visualBatch = previousVisualBatch == null
                ? new VisualBatchState() : previousVisualBatch;
            activeVisualBatch.set(visualBatch);
            try {
                shootStraightLaser(laser, level, ctx, batch);
            } finally {
                if (previousVisualBatch == null) visualBatch.flush();
                if (previousVisualBatch == null) activeVisualBatch.remove();
                else activeVisualBatch.set(previousVisualBatch);
            }
            return;
        }

        runSegmentedLaser(laser, level, ctx);
    }

    private LaserCollisionBatch activeBatch(ServerLevel level) {
        Map<ServerLevel, LaserCollisionBatch> batches = activeCollisionBatches.get();
        return batches == null ? null : batches.get(level);
    }

    private boolean canUseStraightPath(LaserProjectile laser) {
        return !laser.homing
            && laser.getTrajectorySpecs().isEmpty()
            && laser.direction.lengthSqr() > 1.0E-8D;
    }

    private void runSegmentedLaser(LaserProjectile laser, ServerLevel level, AttackContext context) {
        int steps = 0;
        while (laser.traveled < laser.range && steps < MAX_LASER_STEPS) {
            steps++;
            stepLaser(laser, level, context);
        }
        if (laser.traveled < laser.range) laser.traveled = laser.range;
    }

    /**
     * Resolves a straight laser with one block traversal and one shared entity broad-phase query.
     * Collision events still run in the legacy coarse-segment order so block callbacks remain
     * ahead of entity callbacks within the same segment.
     */
    private void shootStraightLaser(
        LaserProjectile laser, ServerLevel level, AttackContext context, LaserCollisionBatch batch) {
        int segmentCount = Math.max(1, (int) Math.ceil(laser.range / laser.step));
        Vec3 start = laser.position;
        Vec3 direction = laser.direction.normalize();
        Vec3 end = start.add(direction.scale(laser.range));
        Map<Integer, StraightBlockHit> blocks = indexBlockContacts(
            level, start, end, laser.width, laser.range, laser.step, segmentCount);
        Map<Integer, List<StraightEntityHit>> entities = indexEntityContacts(
            laser, batch, start, end, laser.range, laser.step, segmentCount);

        int previousDepth = STRAIGHT_PATH_DEPTH.get();
        STRAIGHT_PATH_DEPTH.set(previousDepth + 1);
        try {
            double distance = 0.0D;
            for (int segment = 0; segment < segmentCount; segment++) {
                double nextDistance = Math.min(laser.range, distance + laser.step);
                Vec3 segmentStart = start.add(direction.scale(distance));
                Vec3 segmentEnd = start.add(direction.scale(nextDistance));
                laser.position = segmentStart;
                laser.direction = direction;

                StraightBlockHit block = blocks.get(segment);
                if (block != null) {
                    boolean firstBlockContact =
                        !laser.getHitBlockPositions().contains(block.hit().getBlockPos());
                    net.minecraft.world.level.block.state.BlockState beforeState =
                        level.getBlockState(block.hit().getBlockPos());
                    Vec3 beforeDirection = laser.direction;
                    SegmentResult result = handleStraightBlockContact(
                        laser, segmentStart, segmentEnd, block.hit());
                    boolean blockMutated = !sameVector(beforeDirection, laser.direction)
                        || !sameVector(block.hit().getLocation(), laser.position)
                        || !beforeState.equals(level.getBlockState(block.hit().getBlockPos()));
                    if (result == SegmentResult.STOPPED && !blockMutated) {
                        queueStraightVisual(laser.owner, start, block.hit().getLocation(),
                            laser.width, context.getColorRl());
                        return;
                    }
                    if (result == SegmentResult.INTERRUPTED || blockMutated
                        || (firstBlockContact && laser.spectral)) {
                        queueStraightVisual(laser.owner, start, block.hit().getLocation(),
                            laser.width, context.getColorRl());
                        continueFromStraightContact(
                            laser, level, context, batch,
                            block.hit().getLocation(), block.parameter(), null);
                        return;
                    }
                }

                List<StraightEntityHit> segmentEntities = entities.get(segment);
                if (segmentEntities != null) {
                    Vec3 half = new Vec3(laser.width * 0.5D, laser.width * 0.5D, laser.width * 0.5D);
                    for (StraightEntityHit indexed : segmentEntities) {
                        LivingEntity target = indexed.target();
                        if (target == laser.owner
                            || !target.isAlive()
                            || EntityHelper.isFriendly(target, laser.owner)
                            || laser.damagedEntities.contains(target.getUUID())) continue;
                        Optional<Vec3> currentHit = ProjectileCollisionHelper.clipExpandedTarget(
                            segmentStart, segmentEnd, target.getBoundingBox(), half);
                        if (currentHit.isEmpty()) continue;

                        Vec3 beforePosition = laser.position;
                        handleEntityHit(laser, target, currentHit.get(), laser.getTriggers());
                        double currentParameter = ProjectileCollisionHelper.pathParameter(
                            start, end, currentHit.get());
                        queueStraightVisual(laser.owner, start, currentHit.get(),
                            laser.width, context.getColorRl());
                        // Entity callbacks may spawn, remove, move, or resize entities that were
                        // outside the original broad-phase snapshot. Resume from the contact so
                        // the remaining path observes the current world state.
                        continueFromStraightContact(
                            laser, level, context, batch,
                            currentHit.get(), currentParameter, beforePosition);
                        return;
                    }
                }

                laser.position = segmentEnd;
                laser.traveled = nextDistance;
                distance = nextDistance;
            }
            laser.position = end;
            laser.direction = direction;
            laser.traveled = laser.range;
            laser.tickCount += segmentCount;
            queueStraightVisual(laser.owner, start, end, laser.width, context.getColorRl());
        } finally {
            STRAIGHT_PATH_DEPTH.set(previousDepth);
        }
    }

    private void continueFromStraightContact(
        LaserProjectile laser,
        ServerLevel level,
        AttackContext context,
        LaserCollisionBatch batch,
        Vec3 contactPosition,
        double parameter,
        @Nullable Vec3 unchangedPosition) {
        // Entity callbacks are allowed to move the laser explicitly. If they only changed the
        // direction, the old segment start would otherwise be paired with the new traveled value;
        // advance from the actual contact point in that case. Block callbacks already position the
        // laser before dispatch and pass null so their epsilon/bounce adjustments are preserved.
        boolean positionUnchanged = unchangedPosition != null && sameVector(unchangedPosition, laser.position);
        if (positionUnchanged) {
            laser.position = contactPosition;
        }
        laser.traveled = Math.max(0.0D, Math.min(laser.range, parameter * laser.range));
        if (positionUnchanged && laser.direction.lengthSqr() > 1.0E-8D
            && laser.range - laser.traveled > 1.0E-5D) {
            Vec3 outgoing = laser.direction.normalize();
            laser.position = laser.position.add(outgoing.scale(1.0E-4D));
            laser.traveled = Math.min(laser.range, laser.traveled + 1.0E-4D);
        }
        if (batch != null && laser.direction.lengthSqr() > 1.0E-8D) {
            Vec3 remainingStart = laser.position;
            Vec3 remainingEnd = remainingStart.add(
                laser.direction.normalize().scale(Math.max(0.0D, laser.range - laser.traveled)));
            batch.addSweep(LaserCollisionBatch.remainingSweepBounds(
                remainingStart, remainingEnd, laser.width));
            batch.rebuild(level);
        }
        laser.tickCount += Math.max(0, (int) Math.floor(laser.traveled / Math.max(0.5D, laser.step)));
        runSegmentedLaser(laser, level, context);
    }

    private static boolean sameVector(Vec3 left, Vec3 right) {
        return left != null && right != null && left.distanceToSqr(right) <= 1.0E-12D;
    }

    /** AABB.contains is strict in this Minecraft version; collision clips land on its boundary. */
    private static boolean containsInclusive(AABB box, Vec3 point) {
        double epsilon = 1.0E-7D;
        return point.x >= box.minX - epsilon && point.x <= box.maxX + epsilon
            && point.y >= box.minY - epsilon && point.y <= box.maxY + epsilon
            && point.z >= box.minZ - epsilon && point.z <= box.maxZ + epsilon;
    }

    private Map<Integer, StraightBlockHit> indexBlockContacts(
        ServerLevel level,
        Vec3 start,
        Vec3 end,
        double width,
        double range,
        double step,
        int segmentCount) {
        Map<Integer, StraightBlockHit> nearest = new HashMap<>();
        Set<BlockPos> tested = new HashSet<>();
        int padding = Math.max(0, (int) Math.ceil(width * 0.5D));
        double half = width * 0.5D;
        VoxelDda.traverse(start, end, (x, y, z, ignored) -> {
            for (int offsetX = -padding; offsetX <= padding; offsetX++) {
                for (int offsetY = -padding; offsetY <= padding; offsetY++) {
                    for (int offsetZ = -padding; offsetZ <= padding; offsetZ++) {
                        BlockPos pos = new BlockPos(x + offsetX, y + offsetY, z + offsetZ);
                        if (!tested.add(pos) || !level.hasChunkAt(pos)) continue;
                        var state = level.getBlockState(pos);
                        if (state.isAir()) continue;
                        for (AABB part : state.getCollisionShape(level, pos).toAabbs()) {
                            SweptCollision.SegmentHit sweep = SweptCollision.segmentAabbDetailed(
                                start, end, part.move(pos).inflate(half, half, half));
                            if (!sweep.hit() || sweep.outwardFace() == null) continue;
                            int segment = segmentFor(sweep.parameter(), range, step, segmentCount);
                            Vec3 hitPosition = start.lerp(end, sweep.parameter());
                            StraightBlockHit candidate = new StraightBlockHit(
                                new BlockHitResult(hitPosition, sweep.outwardFace(), pos.immutable(), false),
                                sweep.parameter(), segment);
                            StraightBlockHit previous = nearest.get(segment);
                            if (previous == null || candidate.parameter() < previous.parameter()) {
                                nearest.put(segment, candidate);
                            }
                        }
                    }
                }
            }
            return false;
        });
        return nearest;
    }

    private Map<Integer, List<StraightEntityHit>> indexEntityContacts(
        LaserProjectile laser,
        LaserCollisionBatch batch,
        Vec3 start,
        Vec3 end,
        double range,
        double step,
        int segmentCount) {
        List<LivingEntity> candidates = new ArrayList<>();
        batch.query(start, end, laser.width, candidates);
        Vec3 half = new Vec3(laser.width * 0.5D, laser.width * 0.5D, laser.width * 0.5D);
        Map<Integer, List<StraightEntityHit>> indexed = new HashMap<>();
        for (LivingEntity target : candidates) {
            if (!target.isAlive()) continue;
            Optional<Vec3> hit = ProjectileCollisionHelper.clipExpandedTarget(
                start, end, target.getBoundingBox(), half);
            if (hit.isEmpty()) continue;
            double parameter = ProjectileCollisionHelper.pathParameter(start, end, hit.get());
            int segment = segmentFor(parameter, range, step, segmentCount);
            indexed.computeIfAbsent(segment, ignored -> new ArrayList<>())
                .add(new StraightEntityHit(target, hit.get(), parameter, segment));
        }
        Comparator<StraightEntityHit> order = Comparator
            .comparingDouble(StraightEntityHit::parameter)
            .thenComparingInt(hit -> hit.target().getId());
        indexed.values().forEach(list -> list.sort(order));
        return indexed;
    }

    private static int segmentFor(double parameter, double range, double step, int segmentCount) {
        int segment = (int) Math.floor(parameter * range / step + 1.0E-9D);
        return Math.max(0, Math.min(segmentCount - 1, segment));
    }

    private SegmentResult handleStraightBlockContact(
        LaserProjectile laser, Vec3 start, Vec3 end, BlockHitResult blockHit) {
        boolean firstContact = laser.markBlockHit(blockHit.getBlockPos());
        if (firstContact) {
            laser.setLastBlockHit(blockHit);
            laser.recordSplitTrigger(SplitTriggerType.BLOCK);
            laser.setPosition(blockHit.getLocation());
            SplitExecutor.execute(laser, SplitTriggerType.BLOCK);
            IsaacAttackHitBlockEvent event = new IsaacAttackHitBlockEvent(
                laser, laser.owner, getId(), laser.getTriggers(), blockHit);
            MinecraftForge.EVENT_BUS.post(event);
            if (!laser.spectral) {
                double parameter = ProjectileCollisionHelper.pathParameter(start, end, blockHit.getLocation());
                laser.traveled = Math.min(laser.range, laser.traveled + (end.distanceTo(start) * parameter));
                if (!event.isCanceled()) {
                    laser.traveled = laser.range;
                    return SegmentResult.STOPPED;
                }
                if (laser.position.equals(blockHit.getLocation())
                    && end.distanceTo(start) * (1.0D - parameter) > 1.0E-5D
                    && laser.direction.lengthSqr() > 1.0E-8D) {
                    laser.position = laser.position.add(laser.direction.normalize().scale(1.0E-4D));
                }
                return SegmentResult.INTERRUPTED;
            }
        } else if (!laser.spectral) {
            laser.position = blockHit.getLocation();
            laser.traveled = laser.range;
            return SegmentResult.STOPPED;
        }
        return SegmentResult.COMPLETE;
    }

    private void handleEntityHit(
        LaserProjectile laser, LivingEntity target, Vec3 hitPosition, net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger triggers) {
        EntityHitResult hitResult = new EntityHitResult(target, hitPosition);
        IsaacAttackBeforeHitEntityEvent beforeHit = new IsaacAttackBeforeHitEntityEvent(
            laser, laser.owner, getId(), triggers, hitResult, laser.damage);
        if (MinecraftForge.EVENT_BUS.post(beforeHit)) return;
        if (!target.isAlive()
            || EntityHelper.isFriendly(target, laser.owner)
            || !containsInclusive(
                target.getBoundingBox().inflate(laser.width * 0.5D), hitPosition)) return;
        float actualDamage = (float) beforeHit.getDamage();
        if (!applyDamage(laser.owner, target, actualDamage)) return;
        laser.damagedEntities.add(target.getUUID());
        laser.recordSplitTrigger(SplitTriggerType.ENTITY);
        SplitExecutor.executeAt(laser, SplitTriggerType.ENTITY, hitPosition, laser.direction);
        laser.homingTarget = null;
        IsaacAttackAfterHitEvent afterHit = new IsaacAttackAfterHitEvent(
            laser,
            laser.owner,
            ModAttackTypes.LASER.getId(),
            triggers,
            hitResult,
            actualDamage,
            target.getHealth());
        MinecraftForge.EVENT_BUS.post(afterHit);
    }

    private record StraightBlockHit(BlockHitResult hit, double parameter, int segment) {
    }

    private record StraightEntityHit(
        LivingEntity target, Vec3 hitPosition, double parameter, int segment) {
    }

    // ================== stepLaser ==================
    protected void stepLaser(LaserProjectile laser, ServerLevel level, AttackContext context) {
        // --------- Homing ---------
        Vec3 previousDirection = laser.direction.lengthSqr() > 1.0E-8D
            ? laser.direction.normalize() : context.getMainAxis().normalize();
        laser.direction = previousDirection;
        laser.isCurrentlyHoming = false;
        if (laser.homing) {
            // Search again every ten steering steps.
            if (laser.tickCount % 10 == 0
                || laser.homingTarget == null
                || !laser.homingTarget.isAlive()) {
                laser.homingTarget =
                    EntityHelper.findNearestTrackingTarget(
                        level,
                        laser.owner,
                        laser.position,
                        8.0,
                        e -> !laser.damagedEntities.contains(e.getUUID()));
            }

            // 高频平滑转向
            if (laser.homingTarget != null && laser.homingTarget.isAlive()) {
                Vec3 toTarget = laser.homingTarget.getEyePosition().subtract(laser.position);
                if (toTarget.lengthSqr() > 1.0E-8D) {
                    double maxTurn = BASE_HOMING_TURN * Math.max(1.0D, laser.width / BASE_LASER_WIDTH);
                    laser.direction = turnTowards(laser.direction, toTarget.normalize(), maxTurn);
                }
                laser.isCurrentlyHoming = true;
            }
        }

        if (laser.isCurrentlyHoming) laser.getTrajectoryRuntime().suspend();
        // --------- Trajectories ---------
        TrajectoryMotion trajectoryMotion = null;
        if (!laser.isCurrentlyHoming && !laser.getTrajectorySpecs().isEmpty()) {
            laser.getTrajectoryRuntime().updateAnchor(laser.shooter);
            laser.getTrajectoryRuntime().beginStep();
            trajectoryMotion =
                TrajectoryEvaluator.evaluate(laser, laser.direction.scale(laser.step), laser.step, 1.0D);
            if (trajectoryMotion.status() == TrajectoryMotion.Status.WORK_LIMIT) {
                laser.getTrajectoryRuntime().rollbackStep();
                laser.traveled = laser.range;
                return;
            }
            if (trajectoryMotion.desiredVelocity() != null
                && trajectoryMotion.desiredVelocity().lengthSqr() > 1.0E-8D) {
                laser.direction = trajectoryMotion.desiredVelocity().normalize();
            }
        }

        // --------- 更新方向 ---------
        if (laser.direction.lengthSqr() > 1e-6) laser.direction = laser.direction.normalize();

        Vec3 start = laser.position;
        HomingArc homingArc = laser.isCurrentlyHoming
            ? HomingArc.between(start, previousDirection, laser.direction, laser.step) : null;
        Vec3 nextPos;
        if (homingArc != null) {
            nextPos = homingArc.positionAt(1.0D);
        } else if (trajectoryMotion != null && trajectoryMotion.desiredPosition() != null) {
            nextPos = trajectoryMotion.desiredPosition();
        } else {
            nextPos = start.add(laser.direction.scale(laser.step));
        }
        spawnInterpolatedParticles(level, start, nextPos, laser.width, context.getColorRl(), homingArc);

        int segments = homingArc == null ? 1 : homingArc.collisionSegments();
        double segmentLength = laser.step / segments;
        double chargedDistance = trajectoryMotion == null
            ? laser.step : trajectoryMotion.chargedDistance(laser.step);
        Vec3 finalDirection = laser.direction;
        for (int i = 0; i < segments; i++) {
            Vec3 segmentStart = laser.position;
            Vec3 segmentEnd = homingArc == null ? nextPos
                : homingArc.positionAt((i + 1.0D) / segments);
            Vec3 segmentDirection = homingArc == null ? laser.direction
                : segmentEnd.subtract(segmentStart).normalize();
            laser.direction = segmentDirection;
            SegmentResult result = traceLaserSegment(laser, level, segmentStart, segmentEnd,
                segmentLength, chargedDistance / segments);
            if (result == SegmentResult.STOPPED) {
                if (trajectoryMotion != null) laser.getTrajectoryRuntime().rollbackStep();
                return;
            }
            if (result == SegmentResult.INTERRUPTED) {
                if (trajectoryMotion != null) laser.getTrajectoryRuntime().rollbackStep();
                laser.tickCount++;
                return;
            }
            if (!laser.direction.equals(segmentDirection)) {
                if (trajectoryMotion != null) laser.getTrajectoryRuntime().rollbackStep();
                laser.tickCount++;
                return;
            }
        }
        laser.direction = finalDirection;
        if (trajectoryMotion != null) {
            laser.getTrajectoryRuntime().advance(trajectoryMotion.progressRate());
            laser.getTrajectoryRuntime().commitStep();
        }
        laser.tickCount++;
    }

    private SegmentResult traceLaserSegment(LaserProjectile laser, ServerLevel level, Vec3 start, Vec3 end,
                                            double segmentLength, double chargedDistance) {
        BlockHitResult blockHit = findBlockCollision(level, start, end, laser.width, laser.owner);
        if (blockHit != null) {
            boolean firstContact = laser.markBlockHit(blockHit.getBlockPos());
            if (firstContact) {
                // Spectral lasers do not stop on blocks, but every new block contact still
                // publishes the same block event and split boundary as a normal laser.
                laser.setLastBlockHit(blockHit);
                laser.recordSplitTrigger(SplitTriggerType.BLOCK);
                // Capture the actual contact center before the deferred split is created.
                // The split reference context must describe the block boundary, not the
                // beginning of this laser segment (especially for spectral lasers).
                laser.setPosition(blockHit.getLocation());
                SplitExecutor.execute(laser, SplitTriggerType.BLOCK);
                IsaacAttackHitBlockEvent event =
                    new IsaacAttackHitBlockEvent(
                        laser, laser.owner, getId(), laser.getTriggers(), blockHit);
                MinecraftForge.EVENT_BUS.post(event);

                if (!laser.spectral) {
                    double hitParameter =
                        ProjectileCollisionHelper.pathParameter(start, end, blockHit.getLocation());
                    laser.traveled += segmentLength * hitParameter;
                    if (!event.isCanceled()) {
                        laser.traveled = laser.range;
                        return SegmentResult.STOPPED;
                    }
                    if (laser.position.equals(blockHit.getLocation())) {
                        Vec3 outgoing = laser.direction;
                        if (segmentLength * (1.0D - hitParameter) > 1.0E-5
                            && outgoing.lengthSqr() > 1.0E-8D) {
                            laser.position = laser.position.add(outgoing.normalize().scale(1.0E-4D));
                        }
                    }
                    return SegmentResult.INTERRUPTED;
                }
            } else if (!laser.spectral) {
                // A non-spectral laser cannot normally revisit a block, but a bounced or
                // redirected path must still stop without publishing a duplicate event.
                laser.position = blockHit.getLocation();
                laser.traveled = laser.range;
                return SegmentResult.STOPPED;
            }
        }

        handleEntityCollision(laser, level, start, end, laser.width, laser.getTriggers());
        laser.position = end;
        laser.traveled += chargedDistance;
        return SegmentResult.COMPLETE;
    }

    private enum SegmentResult { COMPLETE, INTERRUPTED, STOPPED }

    // ================== Collision & Damage ==================
    @Nullable
    private BlockHitResult findBlockCollision(
        ServerLevel level, Vec3 start, Vec3 end, double width, Entity source) {
        AABB swept = createCollisionBox(start, width).expandTowards(end.subtract(start));
        BlockPos min = BlockPos.containing(swept.minX, swept.minY, swept.minZ);
        BlockPos max = BlockPos.containing(swept.maxX, swept.maxY, swept.maxZ);
        BlockHitResult nearest = null;
        double nearestParameter = Double.POSITIVE_INFINITY;
        Vec3 half = new Vec3(width * 0.5, width * 0.5, width * 0.5);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            var shape = level.getBlockState(pos).getCollisionShape(level, pos);
            for (AABB part : shape.toAabbs()) {
                AABB expanded = part.move(pos).inflate(half.x, half.y, half.z);
                Vec3 hit = expanded.clip(start, end).orElse(null);
                if (hit == null) continue;
                double parameter = ProjectileCollisionHelper.pathParameter(start, end, hit);
                if (parameter < nearestParameter) {
                    nearestParameter = parameter;
                    Vec3 motion = end.subtract(start);
                    double dx = Math.min(Math.abs(hit.x - expanded.minX), Math.abs(hit.x - expanded.maxX));
                    double dy = Math.min(Math.abs(hit.y - expanded.minY), Math.abs(hit.y - expanded.maxY));
                    double dz = Math.min(Math.abs(hit.z - expanded.minZ), Math.abs(hit.z - expanded.maxZ));
                    Direction direction =
                        dx <= dy && dx <= dz
                            ? (motion.x > 0 ? Direction.WEST : Direction.EAST)
                            : dy <= dz
                                ? (motion.y > 0 ? Direction.DOWN : Direction.UP)
                                : (motion.z > 0 ? Direction.NORTH : Direction.SOUTH);
                    nearest = new BlockHitResult(hit, direction, pos.immutable(), false);
                }
            }
        }
        return nearest;
    }

    protected void handleEntityCollision(
        LaserProjectile laser,
        ServerLevel level,
        Vec3 start,
        Vec3 end,
        double width,
        CompositeTrigger triggers) {
        AABB box = createCollisionBox(start, width).expandTowards(end.subtract(start));
        List<LivingEntity> entities =
            level.getEntitiesOfClass(
                LivingEntity.class,
                box,
                e ->
                    e != laser.owner
                        && e.isAlive()
                        && !EntityHelper.isFriendly(e, laser.owner)
                        && !laser.damagedEntities.contains(e.getUUID()));

        Map<LivingEntity, Vec3> hitPositions = new java.util.HashMap<>();
        Vec3 half = new Vec3(width * 0.5, width * 0.5, width * 0.5);
        entities.removeIf(
            target -> {
                Optional<Vec3> hit =
                    ProjectileCollisionHelper.clipExpandedTarget(
                        start, end, target.getBoundingBox(), half);
                if (hit.isEmpty()) return true;
                hitPositions.put(target, hit.get());
                return false;
            });
        entities.sort(
            java.util.Comparator.comparingDouble(
                target ->
                    ProjectileCollisionHelper.pathParameter(start, end, hitPositions.get(target))));
        for (LivingEntity target : entities) {
            EntityHitResult hitResult = new EntityHitResult(target, hitPositions.get(target));

            IsaacAttackBeforeHitEntityEvent beforeHit =
                new IsaacAttackBeforeHitEntityEvent(
                    laser, laser.owner, getId(), triggers, hitResult, laser.damage);

            if (!MinecraftForge.EVENT_BUS.post(beforeHit)
                && target.isAlive()
                && !EntityHelper.isFriendly(target, laser.owner)
                && containsInclusive(target.getBoundingBox().inflate(width * 0.5D), hitPositions.get(target))) {
                double actualDamage = beforeHit.getDamage();
                if (!applyDamage(laser.owner, target, (float) actualDamage)) {
                    continue;
                }
                laser.damagedEntities.add(target.getUUID());
                laser.recordSplitTrigger(SplitTriggerType.ENTITY);
                SplitExecutor.executeAt(laser, SplitTriggerType.ENTITY,
                    hitPositions.get(target), laser.direction);
                laser.homingTarget = null; // 清空当前追踪目标，开始追踪下一个目标

                IsaacAttackAfterHitEvent afterHit =
                    new IsaacAttackAfterHitEvent(
                        laser,
                        laser.owner,
                        ModAttackTypes.LASER.getId(),
                        triggers,
                        hitResult,
                        actualDamage,
                        target.getHealth());
                MinecraftForge.EVENT_BUS.post(afterHit);
            }
        }
    }

    protected boolean applyDamage(LivingEntity source, LivingEntity target, float damage) {
        return target.hurt(getDamageSource(source), damage);
    }

    protected DamageSource getDamageSource(LivingEntity source) {
        ServerLevel level = getLaserLevel(source);
        if (level == null) return source.damageSources().generic();
        var damageTypeHolder =
            level
                .registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModDamageType.LASER);
        return new DamageSource(damageTypeHolder, source, source);
    }

    /** Resolves the world for collision, damage and visuals; persistent beams may pin it at release. */
    protected @Nullable ServerLevel getLaserLevel(@Nullable LivingEntity owner) {
        return owner != null && owner.level() instanceof ServerLevel level ? level : null;
    }

    // ================== Utils ==================
    private Vec3 turnTowards(Vec3 current, Vec3 target, double maxAngleRad) {
        double dot = current.dot(target);
        dot = Math.max(-1.0, Math.min(1.0, dot));

        double angle = Math.acos(dot);
        if (angle <= maxAngleRad) return target;

        Vec3 axis = current.cross(target);
        if (axis.lengthSqr() < 1.0E-12D) {
            axis = current.cross(new Vec3(0.0D, 1.0D, 0.0D));
            if (axis.lengthSqr() < 1.0E-12D) {
                axis = current.cross(new Vec3(1.0D, 0.0D, 0.0D));
            }
        }
        return GeometryHelper.rotateAroundAxis(current, axis.normalize(), maxAngleRad);
    }

    /** Keeps the visible curve continuous; collision chords approximate it within the beam width. */
    private record HomingArc(Vec3 start, Vec3 forward, Vec3 side, double angle, double length) {
        @Nullable
        static HomingArc between(Vec3 start, Vec3 fromDirection, Vec3 toDirection, double length) {
            double dot = Math.max(-1.0D, Math.min(1.0D, fromDirection.dot(toDirection)));
            double angle = Math.acos(dot);
            if (angle < 1.0E-5D) return null;
            Vec3 axis = fromDirection.cross(toDirection);
            if (axis.lengthSqr() < 1.0E-12D) return null;
            return new HomingArc(start, fromDirection, axis.normalize().cross(fromDirection), angle, length);
        }

        Vec3 positionAt(double fraction) {
            double radians = angle * fraction;
            return start.add(forward.scale(length * Math.sin(radians) / angle))
                .add(side.scale(length * (1.0D - Math.cos(radians)) / angle));
        }

        int collisionSegments() {
            return Math.max(1, (int) Math.ceil(angle / MAX_HOMING_COLLISION_ANGLE));
        }
    }

    private void queueStraightVisual(
        LivingEntity owner, Vec3 start, Vec3 end, double width, ResourceLocation colorRl) {
        VisualBatchState batch = activeVisualBatch.get();
        if (batch == null) {
            batch = new VisualBatchState();
            activeVisualBatch.set(batch);
            try {
                batch.add(getLaserLevel(owner), start, end, width, resolveLaserColor(colorRl));
            } finally {
                batch.flush();
                activeVisualBatch.remove();
            }
            return;
        }
        batch.add(getLaserLevel(owner), start, end, width, resolveLaserColor(colorRl));
    }

    private int resolveLaserColor(ResourceLocation colorRl) {
        IForgeRegistry<BulletColor> registry =
            RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);
        BulletColor color = colorRl == null || registry == null
            ? ModBulletColors.BASE.get() : registry.getValue(colorRl);
        color = color == null ? ModBulletColors.BASE.get() : color;
        Vector3f rgb = color == ModBulletColors.BASE.get()
            ? new Vector3f(1.0F, 0.0F, 0.0F)
            : BulletColor.getVec3fColorById(color.color());
        return channel(rgb.x) << 16 | channel(rgb.y) << 8 | channel(rgb.z);
    }

    private static int channel(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255.0F)));
    }

    private static final class VisualBatchState {
        private static final double VISUAL_TRACKING_RADIUS = 32.0D;
        private final Map<ServerLevel, List<LaserBeamBatchS2CPacket.Beam>> entries =
            new IdentityHashMap<>();

        void add(@Nullable ServerLevel level, Vec3 start, Vec3 end, double width, int color) {
            if (level == null) return;
            entries.computeIfAbsent(level, ignored -> new ArrayList<>())
                .add(new LaserBeamBatchS2CPacket.Beam(start, end, (float) width, color));
        }

        void flush() {
            Map<ServerPlayer, List<LaserBeamBatchS2CPacket.Beam>> recipients =
                new IdentityHashMap<>();
            for (Map.Entry<ServerLevel, List<LaserBeamBatchS2CPacket.Beam>> entry : entries.entrySet()) {
                List<LaserBeamBatchS2CPacket.Beam> beams = entry.getValue();
                if (beams.isEmpty()) continue;
                ServerLevel level = entry.getKey();
                for (ServerPlayer player : level.players()) {
                    List<LaserBeamBatchS2CPacket.Beam> visible = new ArrayList<>();
                    for (LaserBeamBatchS2CPacket.Beam beam : beams) {
                        if (distanceToSegmentSqr(player.position(), beam.start(), beam.end())
                            <= VISUAL_TRACKING_RADIUS * VISUAL_TRACKING_RADIUS) {
                            visible.add(beam);
                        }
                    }
                    if (!visible.isEmpty()) {
                        recipients.computeIfAbsent(player, ignored -> new ArrayList<>()).addAll(visible);
                    }
                }
            }
            for (Map.Entry<ServerPlayer, List<LaserBeamBatchS2CPacket.Beam>> entry : recipients.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    ModMessages.sentToPlayer(new LaserBeamBatchS2CPacket(entry.getValue()), entry.getKey());
                }
            }
            entries.clear();
        }

        private static double distanceToSegmentSqr(Vec3 point, Vec3 start, Vec3 end) {
            Vec3 delta = end.subtract(start);
            double lengthSqr = delta.lengthSqr();
            if (lengthSqr <= 1.0E-12D) return point.distanceToSqr(start);
            double fraction = point.subtract(start).dot(delta) / lengthSqr;
            fraction = Math.max(0.0D, Math.min(1.0D, fraction));
            return point.distanceToSqr(start.add(delta.scale(fraction)));
        }
    }

    private void spawnInterpolatedParticles(
        ServerLevel level, Vec3 from, Vec3 to, double width, ResourceLocation colorRl,
        @Nullable HomingArc homingArc) {
        IForgeRegistry<BulletColor> registry =
            RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);

        BulletColor c = registry != null ? registry.getValue(colorRl) : ModBulletColors.BASE.get();
        c = c == null ? ModBulletColors.BASE.get() : c;

        Vector3f color = BulletColor.getVec3fColorById(c.color());
        if (c == ModBulletColors.BASE.get()) color = new Vector3f(1f, 0f, 0f);

        Vec3 delta = to.subtract(from);
        double distance = homingArc == null ? delta.length() : homingArc.length();
        int steps = Math.max(1, (int) Math.ceil(distance / 0.2));

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            Vec3 pos = homingArc == null ? from.add(delta.scale(t)) : homingArc.positionAt(t);
            float particleSize = (float) Math.max(0.01D, Math.min(4.0D, width));
            DustParticleOptions dust = new DustParticleOptions(color, particleSize);
            level.sendParticles(dust, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    private AABB createCollisionBox(Vec3 pos, double width) {
        return new AABB(
            pos.subtract(width / 2, width / 2, width / 2), pos.add(width / 2, width / 2, width / 2));
    }

    protected double getWidth(AttackContext context) {
        return laserWidth(context.getBulletScale(), BASE_LASER_WIDTH);
    }

    protected static double laserWidth(double bulletScale, double baseWidth) {
        if (!Double.isFinite(bulletScale) || bulletScale <= 0.0D) return baseWidth;
        double growth = Math.min(3.0D, Math.sqrt(bulletScale));
        return baseWidth * growth;
    }
}

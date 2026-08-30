package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.helper.GeometryHelper;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.AttackPatternContext;
import net.luojiuoscar.isaac_disaster.registries.attack_pattern.impl.LaserAttackPattern;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.BulletSourceType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.attack_type.util.DamagedEntities;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitExecutor;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitSequence;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerCounts;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.luojiuoscar.isaac_disaster.registries.trajectory.IAttackTrajectory;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModAttackTrajectories;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.luojiuoscar.isaac_disaster.helper.ProjectileCollisionHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LaserAttack extends AttackType {
    private static final LaserAttackPattern PATTERN = new LaserAttackPattern();

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

    @Override
    public void makeSound(LivingEntity entity) {
        entity.level().playSound(
                null,
                entity.blockPosition(),
                ModSounds.LASER_SHOT.get(),
                SoundSource.PLAYERS,
                0.6f,
                1.0f
        );
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
        private Vec3 prevShooterPos = null;
        public boolean isCurrentlyHoming;
        public int tickCount;
        public LivingEntity homingTarget;
        public double yRotAngle;
        public double xRotAngle;
        private final AttackContext attackContext;
        private final CompositeTrigger triggers;
        private final SplitSequence splitSequence;
        private int attackSequenceIndex;
        private final double range;
        private final SplitTriggerCounts splitTriggerCounts = new SplitTriggerCounts();
        private BlockPos lastSplitBlockPosition;
        private BlockHitResult lastBlockHit;

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
            GeometryHelper.Rotation rotation = GeometryHelper.rotationFromMainAxis(attackContext.getMainAxis());
            this.yRotAngle = owner.getYRot() - rotation.yRot();
            this.xRotAngle = owner.getXRot() - rotation.xRot();
            this.attackContext = attackContext.copy();
            this.triggers = attackContext.copyTrigger();
            this.splitSequence = attackContext.copySplitSequence();
            this.attackSequenceIndex = 0;
            this.range = attackContext.getBulletRange();
            this.lastBlockHit = null;

            this.prevShooterPos = getShooterWaistPosition();
        }

        public void setWidth(double width) { this.width = width; }
        public void setStep(double step) { this.step = step; }
        public void setHoming(boolean homing) { this.homing = homing; }
        public void setSpectral(boolean spectral) { this.spectral = spectral; }
        public void setAttackSequenceIndex(int attackSequenceIndex) { this.attackSequenceIndex = attackSequenceIndex; }
        @Override
        public void setLastBlockHit(@Nullable BlockHitResult lastBlockHit) { this.lastBlockHit = lastBlockHit; }

        @Override
        public AttackContext getAttackContext() { return attackContext.copy(); }

        @Override
        public BulletSourceType getSourceType() {
            return attackSequenceIndex > 0 ? BulletSourceType.BRIMSTONE : BulletSourceType.LASER;
        }

        public int getAttackSequenceIndex() { return attackSequenceIndex; }
        @Nullable
        @Override
        public BlockHitResult getLastBlockHit() { return lastBlockHit; }

        @Override
        public SplitSequence getSplitSequence() { return splitSequence; }

        @Override
        public SplitTriggerCounts getSplitTriggerCounts() { return splitTriggerCounts.copy(); }

        @Override
        public void recordSplitTrigger(SplitTriggerType type) {
            splitTriggerCounts.increment(type);
        }

        /** Records a block split position unless it is inside the previous hit's 3x3x3 area. */
        public boolean markBlockSplitPosition(BlockPos position) {
            BlockPos current = Objects.requireNonNull(position, "position");
            if (lastSplitBlockPosition != null
                    && Math.abs(lastSplitBlockPosition.getX() - current.getX()) <= 1
                    && Math.abs(lastSplitBlockPosition.getY() - current.getY()) <= 1
                    && Math.abs(lastSplitBlockPosition.getZ() - current.getZ()) <= 1) {
                return false;
            }

            lastSplitBlockPosition = current.immutable();
            return true;
        }

        @Override
        public float getDamage() { return this.damage; }

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
        public Vec3 getPrevShooterPos() {
            if (shooter != null){
                prevShooterPos = getShooterWaistPosition();
            }

            return prevShooterPos;
        }

        private Vec3 getShooterWaistPosition() {
            AABB box = shooter.getBoundingBox();
            return new Vec3(
                    (box.minX + box.maxX) * 0.5,
                    box.minY + box.getYsize() * 0.6,
                    (box.minZ + box.maxZ) * 0.5
            );
        }

        @Override
        public double getStartYRot() {
            return this.yRotAngle;
        }

        @Override
        public double getStartXRot() {
            return this.xRotAngle;
        }

        @Override
        public boolean noGravity(){
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
        public Map<ResourceLocation, Integer> getTrajectories() {
            return attackContext.getTrajectories();
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
       for (AttackContext ctx : ctxList){
           shoot(ctx);
       }
    }

    // ================== shotLaser ==================
    @Override
    public void shoot(AttackContext ctx) {
        shootSingleLaser(ctx, 0);
    }

    protected void shootSingleLaser(AttackContext ctx, int attackSequenceIndex) {
        LivingEntity entity = ctx.getOwner();
        if (!(entity.level() instanceof ServerLevel level)) return;

        Vec3 direction = ctx.getMainAxis();
        double width = getWidth(ctx);

        LaserProjectile laser = new LaserProjectile(ctx);
        laser.direction = direction;
        laser.setWidth(width);
        laser.setStep(Math.max(0.5, width * 2));
        laser.setHoming(isHoming(entity));
        laser.setSpectral(isSpectral(entity));
        laser.setAttackSequenceIndex(attackSequenceIndex);

        while (laser.traveled < laser.range) {
            stepLaser(laser, level, ctx);
        }

    }

    // ================== stepLaser ==================
    protected void stepLaser(LaserProjectile laser, ServerLevel level, AttackContext context) {
        // --------- Homing ---------
        laser.isCurrentlyHoming = false;
        if (laser.homing) {
            // 低频搜索目标（每 10 tick）
            if (laser.tickCount % 10 == 0 || laser.homingTarget == null || !laser.homingTarget.isAlive()) {
                laser.homingTarget = EntityHelper.findNearestTrackingTarget(
                        level,
                        laser.owner,
                        laser.position,
                        8.0,
                        e -> !laser.damagedEntities.contains(e.getUUID())
                );
            }

            // 高频平滑转向
            if (laser.homingTarget != null && laser.homingTarget.isAlive()) {
                Vec3 toTarget = laser.homingTarget.getEyePosition().subtract(laser.position).normalize();
                laser.direction = turnTowards(laser.direction, toTarget, 0.25);
                laser.isCurrentlyHoming = true;
            }
        }

        // --------- Trajectories ---------
        Vec3 totalPositionOffset = Vec3.ZERO;
        Vec3 totalVelocityOffset = Vec3.ZERO;
        IForgeRegistry<IAttackTrajectory> trajectoryIForgeRegistry =
                RegistryManager.ACTIVE.getRegistry(ModAttackTrajectories.ATTACK_TRAJECTORY_KEY);

        if (!laser.isCurrentlyHoming && trajectoryIForgeRegistry != null) {
            for (Map.Entry<ResourceLocation, Integer> entry : context.getTrajectories().entrySet()) {
                ResourceLocation trajId = entry.getKey();
                int amplifier = entry.getValue() - 1;

                IAttackTrajectory traj = trajectoryIForgeRegistry.getValue(trajId);
                if (traj == null) continue;

                TrajectoryContext ctx = new TrajectoryContext(laser, laser.step, amplifier,
                        laser.getPrevShooterPos());

                var result = traj.getResult(ctx);
                totalPositionOffset = totalPositionOffset.add(result.positionOffset());
                totalVelocityOffset = totalVelocityOffset.add(result.velocityOffset());

                // ---- 应用旋转到方向 ----
                // yRot 绕世界上方向旋转
                Vec3 up = new Vec3(0, 1, 0);
                laser.direction = GeometryHelper.rotateAroundAxis(laser.direction, up, result.yRot());

                // xRot 绕局部右方向旋转（如果 xRot 不为0）
                Vec3 right = laser.direction.cross(up).normalize();
                laser.direction = GeometryHelper.rotateAroundAxis(laser.direction, right, result.xRot());
            }
        }

        // --------- 更新方向 ---------
        laser.direction = laser.direction.add(totalVelocityOffset);
        if (laser.direction.lengthSqr() > 1e-6) laser.direction = laser.direction.normalize();

        Vec3 start = laser.position;
        Vec3 displacement = laser.direction.scale(laser.step).add(totalPositionOffset);
        Vec3 nextPos = start.add(displacement);
        spawnInterpolatedParticles(level, start, nextPos, laser.width, context.getColorRl());

        if (!laser.spectral) {
            BlockHitResult blockHit = findBlockCollision(level, start, nextPos, laser.width, laser.owner);
            if (blockHit != null) {
                double hitParameter = ProjectileCollisionHelper.pathParameter(start, nextPos, blockHit.getLocation());
                laser.position = blockHit.getLocation();
                laser.setLastBlockHit(blockHit);
                if (laser.markBlockSplitPosition(blockHit.getBlockPos())) {
                    laser.recordSplitTrigger(SplitTriggerType.BLOCK);
                    SplitExecutor.execute(laser, SplitTriggerType.BLOCK);
                }
                IsaacAttackHitBlockEvent event = new IsaacAttackHitBlockEvent(
                        laser, laser.owner, getId(), laser.getTriggers(), blockHit);
                MinecraftForge.EVENT_BUS.post(event);
                laser.traveled += laser.step * hitParameter;
                if (!event.isCanceled()) {
                    laser.traveled = laser.range;
                    return;
                }
                if (laser.step * (1.0D - hitParameter) > 1.0E-5 && laser.direction.lengthSqr() > 1.0E-8) {
                    laser.position = laser.position.add(laser.direction.normalize().scale(1.0E-4));
                }
                laser.tickCount++;
                return;
            }
        }

        handleEntityCollision(laser, level, start, nextPos, laser.width, laser.getTriggers());
        laser.position = nextPos;
        laser.traveled += laser.step;
        laser.tickCount++;
    }

    // ================== Collision & Damage ==================
    @Nullable
    private BlockHitResult findBlockCollision(ServerLevel level, Vec3 start, Vec3 end, double width, Entity source) {
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
                    Direction direction = dx <= dy && dx <= dz
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

    protected void handleEntityCollision(LaserProjectile laser, ServerLevel level, Vec3 start, Vec3 end, double width,
                                         CompositeTrigger triggers) {
        AABB box = createCollisionBox(start, width).expandTowards(end.subtract(start));
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != laser.owner
                        && e.isAlive()
                        && !EntityHelper.isFriendly(e, laser.owner)
                        && !laser.damagedEntities.contains(e.getUUID())
        );

        Map<LivingEntity, Vec3> hitPositions = new java.util.HashMap<>();
        Vec3 half = new Vec3(width * 0.5, width * 0.5, width * 0.5);
        entities.removeIf(target -> {
            Optional<Vec3> hit = ProjectileCollisionHelper.clipExpandedTarget(start, end, target.getBoundingBox(), half);
            if (hit.isEmpty()) return true;
            hitPositions.put(target, hit.get());
            return false;
        });
        entities.sort(java.util.Comparator.comparingDouble(target ->
                ProjectileCollisionHelper.pathParameter(start, end, hitPositions.get(target))));
        for (LivingEntity target : entities) {
            EntityHitResult hitResult = new EntityHitResult(target,
                    hitPositions.get(target));

            IsaacAttackBeforeHitEntityEvent beforeHit = new IsaacAttackBeforeHitEntityEvent(
                    laser, laser.owner, getId(), triggers, hitResult, laser.damage
            );

            if (!MinecraftForge.EVENT_BUS.post(beforeHit)) {
                double actualDamage = beforeHit.getDamage();
                if (!applyDamage(laser.owner, target, (float) actualDamage)) {
                    continue;
                }
                laser.damagedEntities.add(target.getUUID());
                SplitExecutor.executeEntityHit(laser);
                laser.homingTarget = null; // 清空当前追踪目标，开始追踪下一个目标

                IsaacAttackAfterHitEvent afterHit = new IsaacAttackAfterHitEvent(
                        laser, laser.owner, ModAttackTypes.LASER.getId(), triggers, hitResult, actualDamage, target.getHealth()
                );
                MinecraftForge.EVENT_BUS.post(afterHit);
            }
        }
    }

    protected boolean applyDamage(LivingEntity source, LivingEntity target, float damage) {
        return target.hurt(getDamageSource(source), damage);
    }

    protected DamageSource getDamageSource(LivingEntity source){
        if (!(source.level() instanceof ServerLevel level)) return source.damageSources().generic();
        var damageTypeHolder = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModDamageType.LASER);
        return new DamageSource(damageTypeHolder, source, source);
    }

    // ================== Utils ==================
    private Vec3 getDirectionFromRotation(float xRot, float yRot) {
        float f = (float) Math.cos(-yRot * ((float) Math.PI / 180F) - (float) Math.PI);
        float f1 = (float) Math.sin(-yRot * ((float) Math.PI / 180F) - (float) Math.PI);
        float f2 = (float) -Math.cos(-xRot * ((float) Math.PI / 180F));
        float f3 = (float) Math.sin(-xRot * ((float) Math.PI / 180F));
        return new Vec3((f1 * f2), f3, (f * f2)).normalize();
    }

    private Vec3 turnTowards(Vec3 current, Vec3 target, double maxAngleRad) {
        double dot = current.dot(target);
        dot = Math.max(-1.0, Math.min(1.0, dot));

        double angle = Math.acos(dot);
        if (angle < 1e-5) return target;

        double rotateAngle = Math.min(maxAngleRad, angle);

        // 旋转轴 = current × target
        Vec3 axis = current.cross(target);

        if (axis.lengthSqr() < 1e-6) {
            return target; // 共线情况
        }

        axis = axis.normalize();

        return GeometryHelper.rotateAroundAxis(current, axis, rotateAngle);
    }

    private void spawnInterpolatedParticles(ServerLevel level, Vec3 from, Vec3 to, double width, ResourceLocation colorRl) {
        IForgeRegistry<BulletColor> registry = RegistryManager.ACTIVE.getRegistry(ModBulletColors.BULLET_COLOR_KEY);

        BulletColor c = registry != null ? registry.getValue(colorRl) : ModBulletColors.BASE.get();
        c = c == null ? ModBulletColors.BASE.get() : c;

        Vector3f color = BulletColor.getVec3fColorById(c.color());
        if (c == ModBulletColors.BASE.get()) color = new Vector3f(1f, 0f, 0f);

        Vec3 delta = to.subtract(from);
        double distance = delta.length();
        int steps = (int) Math.ceil(distance / 0.2);

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            Vec3 pos = from.add(delta.scale(t));
            float particleSize = (float) Math.max(0.01D, Math.min(4.0D, width));
            DustParticleOptions dust = new DustParticleOptions(color, particleSize);
            level.sendParticles(dust, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    private AABB createCollisionBox(Vec3 pos, double width) {
        return new AABB(
                pos.subtract(width / 2, width / 2, width / 2),
                pos.add(width / 2, width / 2, width / 2)
        );
    }

    protected double getWidth(AttackContext context) {
        return laserWidth(context.getBulletScale(), 0.25D);
    }

    protected static double laserWidth(double bulletScale, double baseWidth) {
        if (!Double.isFinite(bulletScale) || bulletScale <= 0.0D) return baseWidth;
        double growth = Math.min(3.0D, Math.sqrt(bulletScale));
        return baseWidth * growth;
    }
}

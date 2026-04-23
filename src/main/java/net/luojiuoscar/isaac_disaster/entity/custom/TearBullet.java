package net.luojiuoscar.isaac_disaster.entity.custom;

import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletTickEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.TearBulletEndOfLifeEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.CompositeTrigger;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.util.DamagedEntities;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.luojiuoscar.isaac_disaster.registries.trajectory.IAttackTrajectory;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModAttackTrajectory;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TearBullet extends Entity implements IBulletObject {

    // ======== 基础属性 ========
    protected int lifeTick;
    protected int totalLifeTick;
    protected float damage;
    protected UUID ownerUUID;
    protected LivingEntity cachedOwner;
    protected Entity shooter;
    protected double yRotAngle;
    protected double xRotAngle;

    private float traveled = 0f;
    private Vec3 velocity = Vec3.ZERO;
    private boolean isCurrentlySteering = false;
    private Vec3 prevShooterPos = null;

    // ======== 特性 ========
    public boolean isSpectral = false;
    public boolean isPiercing = false;
    public boolean isHoming = false;
    public boolean isControllable = false;

    protected double homingRange = 4.0;
    protected double homingSpeed = 1.0;
    protected double homingSteer = 0.6;

    protected double controlRange = 64.0;
    protected double controlSteer = 0.8;

    // ======== 状态 ========
    protected final DamagedEntities damagedEntities = new DamagedEntities();
    protected ResourceLocation colorRl = ModBulletColor.BASE.getId();
    protected final CompositeTrigger trigger = new CompositeTrigger();
    protected Vec3 extraPositionOffset = Vec3.ZERO;

    // ======== 客户端同步属性 ========
    protected static final EntityDataAccessor<Float> SCALE =
            SynchedEntityData.defineId(TearBullet.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(TearBullet.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> ALPHA =
            SynchedEntityData.defineId(TearBullet.class, EntityDataSerializers.FLOAT);

    protected static final EntityDataAccessor<String> TRAJECTORIES =
            SynchedEntityData.defineId(TearBullet.class, EntityDataSerializers.STRING);


    // ======== 构造函数 ========
    public TearBullet(EntityType<? extends TearBullet> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.damage = 1.0f;
        this.setColor(0xFFFFFF);
        this.setAlpha(1.0f);
        this.setScale(1.0f);
        setVelocity(Vec3.ZERO);
    }

    public TearBullet(Level level,
                      LivingEntity owner, Entity shooter,
                      int lifeTick, double bulletSpeed, float scale, float damage,
                      float xRot, float yRot, Vec3 pos) {
        this(ModEntities.TEAR_BULLET.get(), level, owner, shooter, lifeTick, bulletSpeed, scale, damage, xRot, yRot, pos);
    }

    protected TearBullet(EntityType<? extends TearBullet> type,
                         Level level,
                         LivingEntity owner,
                         Entity shooter,
                         int lifeTick,
                         double bulletSpeed,
                         float scale,
                         float damage,
                         float xRot,
                         float yRot,
                         Vec3 pos) {

        this(type, level);

        this.ownerUUID = shooter.getUUID();
        this.cachedOwner = owner;
        this.shooter = shooter;
        this.lifeTick = lifeTick;
        this.totalLifeTick = lifeTick;
        this.damage = damage;
        this.yRotAngle = shooter.getYRot() - yRot;
        this.xRotAngle = shooter.getXRot() - xRot;

        setScale(scale);

        moveTo(pos.x, pos.y, pos.z, yRot, xRot);

        Vec3 look = Vec3.directionFromRotation(xRot, yRot);
        setVelocity(look.scale(bulletSpeed));
    }

    // ======== 核心逻辑 ========
    @Override
    public void tick() {
        super.tick();

        move(MoverType.SELF, getDeltaMovement());

        if (!level().isClientSide){
            MinecraftForge.EVENT_BUS.post(new BulletTickEvent(this));

            Vec3 positionOffset = extraPositionOffset; // 额外位置偏移
            double traveled = getTraveled();

            // ================== 跟踪/控制 ==================
            if (tickCount % 4 == 0) setIsCurrentlySteering(handleSteering());

            // ================== 轨迹偏移 ==================
            Vec3 baseDir = getVelocity().normalize(); // 当前方向
            double speed = getVelocity().length();    // 当前速度大小
            IForgeRegistry<IAttackTrajectory> trajectoryIForgeRegistry =
                    RegistryManager.ACTIVE.getRegistry(ModAttackTrajectory.ATTACK_TRAJECTORY_KEY);

            if (!isCurrentlySteering() && trajectoryIForgeRegistry != null) { // 非跟踪时
                for (Map.Entry<ResourceLocation, Integer> entry : getTrajectories().entrySet()) {
                    ResourceLocation trajId = entry.getKey();
                    int amplifier = entry.getValue() - 1;

                    IAttackTrajectory traj = trajectoryIForgeRegistry.getValue(trajId);
                    if (traj == null) continue;

                    TrajectoryContext ctx = new TrajectoryContext(
                            this, speed, amplifier, getPrevShooterPos());

                    var result = traj.getResult(ctx);

                    // ---- 应用旋转到方向 ----
                    Vec3 up = new Vec3(0, 1, 0);
                    baseDir = AttackType.rotateAroundAxis(baseDir, up, result.yRot());
                    Vec3 right = baseDir.cross(up).normalize();
                    baseDir = AttackType.rotateAroundAxis(baseDir, right, result.xRot());

                    // ---- 将旋转后的方向应用到速度 ----
                    Vec3 newVelocity = baseDir.scale(speed).add(result.velocityOffset());
                    setVelocity(newVelocity);

                    // ---- 累积位置偏移 ----
                    positionOffset = positionOffset.add(result.positionOffset());
                }
            }

            // 最终位移 = velocity + 额外位置偏移
            Vec3 velocityMove = getVelocity(); // 当前速度向量
            Vec3 finalMove = velocityMove.add(positionOffset);
            setDeltaMovement(finalMove);

            // ================== 碰撞检测 ==================
            if (--lifeTick <= 0) {
                if (!MinecraftForge.EVENT_BUS.post(new TearBulletEndOfLifeEvent(this))) discard();
                return;
            }

            Vec3 start = position();
            Vec3 end = start.add(finalMove);

            if (handleBlockCollision(start, end)) return;
            handleEntityCollision(start, end, finalMove);

            setTraveled((float)(traveled + velocityMove.length()));

            updateDirection();
            extraPositionOffset = Vec3.ZERO;
        }
    }

    protected void updateDirection(){
        Vec3 vel = getVelocity();
        if (vel.lengthSqr() > 1.0e-6) {
            double yaw = Math.atan2(vel.z, vel.x) * Mth.RAD_TO_DEG - 90.0;
            double pitch = -Math.atan2(vel.y, Math.sqrt(vel.x * vel.x + vel.z * vel.z)) * Mth.RAD_TO_DEG;

            setYRot((float) yaw);
            setXRot((float) pitch);

            yRotO = getYRot();
            xRotO = getXRot();
        }
    }

    // ======== 碰撞与追踪 ========
    protected boolean handleBlockCollision(Vec3 start, Vec3 end) {
        if (isSpectral) return false;

        BlockHitResult blockHit = level().clip(
                new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)
        );

        if (blockHit.getType() != HitResult.Type.BLOCK) return false;

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level().getBlockState(pos);
        VoxelShape shape = state.getCollisionShape(level(), pos);

        if (shape.isEmpty() || shape.bounds().getSize() < 0.01) return false;

        IsaacAttackHitBlockEvent event =
                new IsaacAttackHitBlockEvent(this, getOwner(), ModAttackType.BULLET.getId(), trigger, blockHit);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            discard();
        }

        return true;
    }

    protected boolean handleSteering() {
        LivingEntity owner = getOwner();
        LivingEntity target = isHoming ? getTrackingTarget() : null;

        if (target == null && isControllable && owner != null) {
            Vec3 targetPos = owner.getEyePosition().add(Vec3.directionFromRotation(owner.getXRot(), owner.getYRot()).scale(getControlRange()));
            steerTowards(targetPos, getControlSteer(), true);
            return true;
        }

        if (target != null) {
            steerTowards(target, getHomingSteer());
            if (getVelocity().length() < getHomingSpeed()) setVelocity(getVelocity().normalize().scale(getHomingSpeed()));
            return true;
        }
        return false;
    }

    protected void handleEntityCollision(Vec3 start, Vec3 end, Vec3 motion) {
        AABB box = getAABB(motion);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level(), this, start, end, box,
                e -> e.isAlive() && e != this && e != getOwner());

        if (entityHit == null) return;

        Entity target = entityHit.getEntity();
        if (!(target instanceof LivingEntity living)) return;

        if (EntityHelper.isFriendly(living, getOwner()) || living == getOwner() || damagedEntities.contains(target.getUUID()))
            return;

        IsaacAttackBeforeHitEntityEvent beforeEvent = new IsaacAttackBeforeHitEntityEvent(
                this, getOwner(), ModAttackType.BULLET.getId(), trigger, entityHit, damage);
        if (MinecraftForge.EVENT_BUS.post(beforeEvent)) return;

        double damageValue = beforeEvent.getDamage();

        boolean success = makeDamage(living, (float) damageValue);
        if (!success){
            return;
        }

        IsaacAttackAfterHitEvent afterEvent = new IsaacAttackAfterHitEvent(
                this, getOwner(), ModAttackType.BULLET.getId(), trigger, entityHit, damageValue, living.getHealth());
        if (MinecraftForge.EVENT_BUS.post(afterEvent)) return;


        if (!isPiercing) {
            discard();
        }
    }

    protected AABB getAABB(Vec3 motion){
        return getBoundingBox().expandTowards(motion).inflate(getScale() * 0.5);
    }

    public LivingEntity getTrackingTarget() {
        Vec3 bulletPos = this.position();
        Vec3 forwardPos = bulletPos.add(getVelocity().normalize().scale(3.0));

        return EntityHelper.findNearestTrackingTarget(
                level(),
                getOwner(),
                forwardPos,
                getHomingRange(),
                e -> !damagedEntities.contains(e.getUUID())
        );
    }

    // ======== 追踪/转向 ========
    public void steerTowards(LivingEntity target, double steerStrength) {
        if (target == null) return;
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        steerTowards(targetPos, steerStrength, true);
    }

    public void steerTowards(Vec3 targetPos, double steerStrength, boolean normalizeSpeed) {
        Vec3 toTarget = targetPos.subtract(position());
        if (toTarget.lengthSqr() < 1e-6) return;

        double distance = toTarget.length();
        double adaptiveSteer = steerStrength * Math.min(distance, 1.0);
        Vec3 desiredVel = toTarget.normalize();
        Vec3 newVel = getVelocity().normalize().add(desiredVel.subtract(getVelocity().normalize()).scale(adaptiveSteer));

        if (normalizeSpeed) newVel = newVel.normalize().scale(getVelocity().length());
        setVelocity(newVel);
    }

    // ======== DamageSource ========
    protected boolean makeDamage(LivingEntity victim, float damage){
        victim.invulnerableTime = 0;
        victim.hurt(getDamageSource(), damage);
        damagedEntities.add(victim.getUUID());
        return true;
    }

    protected DamageSource getDamageSource() {
        if (!(level() instanceof ServerLevel serverLevel)) return this.damageSources().generic();

        var damageTypeHolder = serverLevel.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModDamageType.TEAR);

        return new DamageSource(damageTypeHolder, this, getOwner());
    }

    // ======== Owner ========
    @Nullable
    @Override
    public LivingEntity getOwner() {
        if (cachedOwner == null && ownerUUID != null && level() instanceof ServerLevel server)
            cachedOwner = (LivingEntity) server.getEntity(ownerUUID);
        return cachedOwner;
    }

    @Nullable
    @Override
    public Object getShooter() {
        return this.shooter;
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
    public boolean noGravity() {
        return false;
    }

    @Override
    public boolean isHoming() {
        return this.isHoming;
    }

    @Override
    public boolean isSpectral() {
        return this.isSpectral;
    }

    @Override
    public boolean isControllable() {
        return this.isControllable;
    }

    @Override
    public boolean isPiercing() {
        return this.isPiercing;
    }

    @Override
    public ResourceLocation getColorId() {
        return colorRl;
    }

    public void setOwner(LivingEntity entity) {
        this.cachedOwner = entity;
        this.ownerUUID = entity == null ? null : entity.getUUID();
    }

    // ======== 同步数据 ========
    @Override
    protected void defineSynchedData() {
        entityData.define(SCALE, 1.0F);
        entityData.define(COLOR, 0xFFFFFF);
        entityData.define(ALPHA, 1.0F);
        entityData.define(TRAJECTORIES, "");
    }

    // ======== NBT读写(不保存子弹) ========
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    // ======== 客户端 ========
    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() { return false; }
    @Override
    public boolean isPushable() { return false; }
    @Override
    public boolean isAttackable() { return false; }
    @Override
    public boolean canBeCollidedWith() { return false; }

    // ======== Getter/Setter ========
    public void setSpectral(boolean b) { this.isSpectral = b; }
    public void setPiercing(boolean b) { this.isPiercing = b; }
    public void setHoming(boolean b) { this.isHoming = b; }
    public void setControllable(boolean b) { this.isControllable = b; }

    public float getScale() { return this.entityData.get(SCALE); }
    public int getColor() { return this.entityData.get(COLOR); }
    public float getAlpha() { return this.entityData.get(ALPHA); }
    @Override
    public double getTraveled() { return this.traveled; }

    @Override
    public Vec3 getPosition() {
        return this.position();
    }

    public double getHomingRange() {
        return homingRange;
    }

    public double getHomingSpeed() {
        return homingSpeed;
    }

    public double getHomingSteer() {
        return homingSteer;
    }

    public double getControlRange() {
        return controlRange;
    }

    public double getControlSteer() {
        return controlSteer;
    }

    public boolean isCurrentlySteering() { return this.isCurrentlySteering; }

    public void setScale(float scale) {
        this.entityData.set(SCALE, scale);
        this.setBoundingBox(new AABB(getX() - scale * 0.25, getY() - scale * 0.25, getZ() - scale * 0.25,
                getX() + scale * 0.25, getY() + scale * 0.25, getZ() + scale * 0.25));
    }

    public void setBulletColor(ResourceLocation id){
        IForgeRegistry<BulletColor> registry = RegistryManager.ACTIVE.getRegistry(ModBulletColor.BULLET_COLOR_KEY);

        BulletColor c = registry != null ? registry.getValue(id) : ModBulletColor.BASE.get();
        c = c == null ? ModBulletColor.BASE.get() : c;

        setColor(c.color());
        setAlpha(c.alpha());
        colorRl = id;
    }

    public void setColor(int rgb) { this.entityData.set(COLOR, rgb); }
    public void setAlpha(float alpha) { this.entityData.set(ALPHA, Mth.clamp(alpha, 0.0F, 1.0F)); }
    public void setTraveled(float traveled) { this.traveled = traveled; }
    public void setIsCurrentlySteering(boolean b) { this.isCurrentlySteering = b; }

    public void setDamage(float damage) { this.damage = damage; }
    @Override
    public float getDamage() { return damage; }

    @Override
    public Vec3 getVelocity() {
        return this.velocity;
    }

    public void setVelocity(Vec3 vel) {
        this.velocity = vel;
    }

    @Override
    public Vec3 getPrevShooterPos() {
        if (shooter != null){
            setPrevShooterPos(shooter.position()
                    .add(0, shooter.getBbHeight() * 0.6, 0));
        }

        return this.prevShooterPos;
    }

    public void setPrevShooterPos(Vec3 pos) {
        this.prevShooterPos = pos;
    }

    public void setTrajectories(Map<ResourceLocation, Integer> map) {
        // 格式 "namespace:path:value,namespace:path:value"
        String s = map.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        entityData.set(TRAJECTORIES, s);
    }

    @Override
    public Map<ResourceLocation, Integer> getTrajectories() {
        String s = entityData.get(TRAJECTORIES);
        Map<ResourceLocation, Integer> map = new HashMap<>();
        if (!s.isEmpty()) {
            for (String triplet : s.split(",")) {
                String[] parts = triplet.split(":");
                if (parts.length == 3) {
                    try {
                        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
                        int value = Integer.parseInt(parts[2]);
                        map.put(rl, value);
                    } catch (Exception ignored) {}
                }
            }
        }
        return map;
    }

    @Override
    public CompositeTrigger getTriggers() {
        return trigger;
    }

    @Override
    public DamagedEntities getDamagedEntities() { return damagedEntities; }

    public void setExtraPositionOffset(Vec3 extraPositionOffset) {
        this.extraPositionOffset = extraPositionOffset;
    }

    public Vec3 getExtraPositionOffset() {
        return extraPositionOffset;
    }
}

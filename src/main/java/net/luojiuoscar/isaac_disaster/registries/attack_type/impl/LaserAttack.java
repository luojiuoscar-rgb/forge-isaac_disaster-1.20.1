package net.luojiuoscar.isaac_disaster.registries.attack_type.impl;

import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.IBulletObject;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackType;
import net.luojiuoscar.isaac_disaster.registries.attack_type.util.DamagedEntities;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColor;
import net.luojiuoscar.isaac_disaster.registries.trajectory.IAttackTrajectory;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModAttackTrajectory;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectoryContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.SimpleTrigger;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;

public class LaserAttack extends AttackType {

    public LaserAttack(double priority) {
        super(priority);
    }

    @Override
    public ResourceLocation getId() {
        return ModAttackType.LASER.getId();
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

        public LaserProjectile(LivingEntity owner, Entity shooter,
                               Vec3 startPos, Vec3 direction,
                               double step, double width,
                               float damage,
                               boolean homing, boolean spectral, double yRotAngle, double xRotAngle,
                               AttackContext attackContext) {
            this.owner = owner;
            this.shooter = shooter;
            this.position = startPos;
            this.direction = direction;
            this.step = step;
            this.width = width;
            this.damage = damage;
            this.homing = homing;
            this.spectral = spectral;
            this.traveled = 0;
            this.isCurrentlyHoming = false;
            this.tickCount = 0;
            this.homingTarget = null;
            this.yRotAngle = yRotAngle;
            this.xRotAngle = xRotAngle;
            this.attackContext = attackContext;

            if (shooter != null){
                prevShooterPos = shooter.position();
            }
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
                prevShooterPos = shooter.position();
            }

            return prevShooterPos;
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
            return attackContext.colorRl;
        }

        @Override
        public Map<ResourceLocation, Integer> getTrajectories() {
            return attackContext.trajectories;
        }

        @Override
        public List<SimpleTrigger> getTriggers() {
            return attackContext.getTriggers();
        }

        @Override
        public DamagedEntities getDamagedEntities() {
            return damagedEntities;
        }
    }

    // ================== handleAttack ==================
    @Override
    public List<AttackContext> getAttackContexts(ServerPlayer player, int bulletCount) {
        AttackContext ctx = getOneAttackContext(player, player);

        List<AttackContext> contexts = new ArrayList<>();
        if (ctx == null) return contexts;

        float angleInterval = 8;
        float curAngle = -angleInterval * (bulletCount - 1) / 2.0f;

        for (int i = 0; i < bulletCount; i++) {
            AttackContext c = ctx.copy();
            c.setYRotOffset(curAngle);
            contexts.add(c);

            curAngle += angleInterval;
        }

        return contexts;
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
        LivingEntity entity = ctx.getOwner();
        if (!(entity.level() instanceof ServerLevel level)) return;

        Vec3 direction = getDirectionFromRotation(ctx.getXRot(), ctx.getYRot()).normalize();

        LaserProjectile laser = new LaserProjectile(
                ctx.getOwner(),
                ctx.getShooter(),
                ctx.getPos(),
                direction,
                Math.max(0.5, getWidth(entity) * 2),
                getWidth(entity),
                getDamage(entity),
                isHoming(entity),
                isSpectral(entity),
                entity.getYRot() - ctx.getYRot(),
                entity.getXRot() - ctx.getXRot(),
                ctx
        );

        while (laser.traveled < getRange(entity)) {
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
                laser.direction = smoothTurn(laser.direction, toTarget, 0.15);
                laser.isCurrentlyHoming = true;
            }
        }

        // --------- Trajectories ---------
        Vec3 totalPositionOffset = Vec3.ZERO;
        Vec3 totalVelocityOffset = Vec3.ZERO;
        IForgeRegistry<IAttackTrajectory> trajectoryIForgeRegistry =
                RegistryManager.ACTIVE.getRegistry(ModAttackTrajectory.ATTACK_TRAJECTORY_KEY);

        if (!laser.isCurrentlyHoming && trajectoryIForgeRegistry != null) {
            for (Map.Entry<ResourceLocation, Integer> entry : context.trajectories.entrySet()) {
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
                laser.direction = AttackType.rotateAroundAxis(laser.direction, up, result.yRot());

                // xRot 绕局部右方向旋转（如果 xRot 不为0）
                Vec3 right = laser.direction.cross(up).normalize();
                laser.direction = AttackType.rotateAroundAxis(laser.direction, right, result.xRot());
            }
        }

        // --------- 更新方向 ---------
        laser.direction = laser.direction.add(totalVelocityOffset);
        if (laser.direction.lengthSqr() > 1e-6) laser.direction = laser.direction.normalize();

        // --------- 计算下一帧位置 ---------
        Vec3 nextPos = laser.position.add(laser.direction.scale(laser.step)).add(totalPositionOffset);

        // --------- 粒子 ---------
        spawnInterpolatedParticles(level, laser.position, nextPos, laser.width, context.colorRl);

        // --------- Block Collision ---------
        AABB box = createCollisionBox(nextPos, laser.width);
        if (handleBlockCollision(laser, level, context.getTriggers()) && !laser.spectral) {
            laser.traveled = getRange(laser.owner);
            return;
        }

        // --------- Entity Collision ---------
        handleEntityCollision(laser, level, box, context.getTriggers());

        // -------- 重新计算nextPos以防pos被修改后行为出错 --------
        nextPos = laser.position.add(laser.direction.scale(laser.step)).add(totalPositionOffset);

        // --------- 更新位置和行进距离 ---------
        laser.position = nextPos;
        laser.traveled += laser.step;
        laser.tickCount++;
    }

    // ================== Collision & Damage ==================
    protected boolean handleBlockCollision(LaserProjectile laser, ServerLevel level, List<SimpleTrigger> triggers) {
        BlockHitResult blockHit = level.clip(new ClipContext(
                laser.position,
                laser.position.add(laser.direction.scale(laser.step)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                laser.owner
        ));

        if (blockHit.getType() == BlockHitResult.Type.BLOCK) {
            IsaacAttackHitBlockEvent blockEvent =
                    new IsaacAttackHitBlockEvent(laser, laser.owner, getId(), triggers, blockHit);
            MinecraftForge.EVENT_BUS.post(blockEvent);

            return !blockEvent.isCanceled();
        }
        return false;
    }

    protected void handleEntityCollision(LaserProjectile laser, ServerLevel level, AABB box,
                                         List<SimpleTrigger> triggers) {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != laser.owner && e.isAlive() && !laser.damagedEntities.contains(e.getUUID())
        );

        for (LivingEntity target : entities) {
            EntityHitResult hitResult = new EntityHitResult(target);

            IsaacAttackBeforeHitEntityEvent beforeHit = new IsaacAttackBeforeHitEntityEvent(
                    laser, laser.owner, getId(), triggers, hitResult, laser.damage
            );

            if (!MinecraftForge.EVENT_BUS.post(beforeHit)) {
                double actualDamage = beforeHit.getDamage();
                laser.damagedEntities.add(makeDamage(laser.owner, target,(float) actualDamage).getUUID());

                IsaacAttackAfterHitEvent afterHit = new IsaacAttackAfterHitEvent(
                        laser, laser.owner, ModAttackType.LASER.getId(), triggers, hitResult, actualDamage, target.getHealth()
                );
                MinecraftForge.EVENT_BUS.post(afterHit);
            }
        }
    }

    protected LivingEntity makeDamage(LivingEntity source, LivingEntity target, float damage) {
        target.invulnerableTime = 0;
        target.hurt(getDamageSource(source), damage);
        return target;
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

    private Vec3 smoothTurn(Vec3 currentDir, Vec3 targetDir, double factor) {
        Vec3 newDir = currentDir.add(targetDir.subtract(currentDir).scale(factor));
        return newDir.normalize();
    }

    private void spawnInterpolatedParticles(ServerLevel level, Vec3 from, Vec3 to, double width, ResourceLocation colorRl) {
        IForgeRegistry<BulletColor> registry = RegistryManager.ACTIVE.getRegistry(ModBulletColor.BULLET_COLOR_KEY);

        BulletColor c = registry != null ? registry.getValue(colorRl) : ModBulletColor.BASE.get();
        c = c == null ? ModBulletColor.BASE.get() : c;

        Vector3f color = BulletColor.getVec3fColorById(c.color());
        if (c == ModBulletColor.BASE.get()) color = new Vector3f(1f, 0f, 0f);

        Vec3 delta = to.subtract(from);
        double distance = delta.length();
        int steps = (int) Math.ceil(distance / 0.2);

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            Vec3 pos = from.add(delta.scale(t));
            DustParticleOptions dust = new DustParticleOptions(color, (float) width * 1.5f);
            level.sendParticles(dust, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    private AABB createCollisionBox(Vec3 pos, double width) {
        return new AABB(
                pos.subtract(width / 2, width / 2, width / 2),
                pos.add(width / 2, width / 2, width / 2)
        );
    }

    protected double getWidth(LivingEntity living) {
        return getBulletScale(living) * 0.25;
    }
}

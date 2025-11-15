package net.luojiuoscar.isaac_disaster.manager.attack.types;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackAfterHitEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackBeforeHitEntityEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.attack.IAttackType;
import net.luojiuoscar.isaac_disaster.manager.attack.managers.AttackTrajectory;
import net.luojiuoscar.isaac_disaster.manager.attack.managers.AttackType;
import net.luojiuoscar.isaac_disaster.manager.attack.managers.BulletColor;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LaserAttack implements IAttackType {

    @Override
    public int getId() {
        return AttackType.LASER.getId();
    }

    @Override
    public double getPriority() {
        return AttackType.LASER.getPriority();
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
    public static class LaserProjectile {
        public Vec3 position;
        public Vec3 direction;
        public double traveled;
        public double step;
        public double width;
        public float damage;
        public boolean homing;
        public boolean spectral;
        public Set<LivingEntity> hitEntities = new HashSet<>();
        public LivingEntity shooter;

        public LaserProjectile(LivingEntity shooter, Vec3 startPos, Vec3 direction, double step, double width,
                               float damage, boolean homing, boolean spectral) {
            this.shooter = shooter;
            this.position = startPos;
            this.direction = direction;
            this.step = step;
            this.width = width;
            this.damage = damage;
            this.homing = homing;
            this.spectral = spectral;
            this.traveled = 0;
        }
    }

    // ================== handleAttack ==================
    @Override
    public void handleAttack(LivingEntity entity, AttackContext context) {
        int count = entity instanceof Player player ? getBulletCount(player) : 1;
        float angleInterval = 6;
        float curAngle = -angleInterval * (count - 1) / 2.0f;

        for (int i = 0; i < count; i++) {
            float baseYRot = entity.getYRot() + curAngle;
            float baseXRot = entity.getXRot();
            shotLaser(entity, context, baseXRot, baseYRot);
            curAngle += angleInterval;
        }
    }

    // ================== shotLaser ==================
    private void shotLaser(LivingEntity entity, AttackContext context, float xRot, float yRot) {
        if (!(entity.level() instanceof ServerLevel level)) return;

        Vec3 startPos = entity.getEyePosition().subtract(0, entity.getBbHeight() * 0.1, 0);
        Vec3 direction = getDirectionFromRotation(xRot, yRot).normalize();

        LaserProjectile laser = new LaserProjectile(
                entity,
                startPos,
                direction,
                Math.max(0.5, getWidth(entity) * 2),
                getWidth(entity),
                getDamage(entity),
                isHoming(entity),
                isSpectral(entity)
        );

        while (laser.traveled < getRange(entity)) {
            stepLaser(laser, level, context);
        }
    }

    // ================== stepLaser ==================
    private void stepLaser(LaserProjectile laser, ServerLevel level, AttackContext context) {
        // --------- Homing ---------
        if (laser.homing) {
            LivingEntity target = EntityHelper.findNearestTrackingTarget(
                    level,
                    laser.shooter,
                    laser.position,
                    8.0,
                    e -> !laser.hitEntities.contains(e)
            );
            if (target != null) {
                Vec3 toTarget = target.getEyePosition().subtract(laser.position).normalize();
                laser.direction = smoothTurn(laser.direction, toTarget, 0.15);
            }
        }

        // --------- Trajectories ---------
        Vec3 positionOffset = Vec3.ZERO;
        Vec3 velocityOffset = Vec3.ZERO;

        for (Map.Entry<Integer, Integer> entry : context.trajectories.entrySet()) {
            int trajId = entry.getKey();
            int amplifier = entry.getValue() - 1;

            AttackTrajectory traj = AttackTrajectory.byId(trajId);
            AttackTrajectory.TrajectoryContext ctx =
                    new AttackTrajectory.TrajectoryContext(
                            laser.direction,
                            laser.traveled,
                            laser.step,
                            laser.shooter,
                            laser.position,
                            amplifier
                    );

            var result = traj.getResult(ctx);
            positionOffset = positionOffset.add(result.positionOffset());
            velocityOffset = velocityOffset.add(result.velocityOffset());
            if (result.speedDelta() > 0) laser.step = result.speedDelta();
        }

        laser.direction = laser.direction.add(velocityOffset);
        if (laser.direction.lengthSqr() > 1e-6) laser.direction = laser.direction.normalize();

        // --------- 计算下一帧位置 ---------
        Vec3 prePos = laser.position;
        Vec3 nextPos = laser.position.add(laser.direction.scale(laser.step)).add(positionOffset);

        // --------- 粒子 ---------
        spawnInterpolatedParticles(level, laser.position, nextPos, laser.width, context.colorId);

        // --------- Block Collision ---------
        AABB box = createCollisionBox(nextPos, laser.width);
        if (!laser.spectral) {
            if (handleBlockCollision(laser, level, context.hitEffects)) {
                laser.traveled = getRange(laser.shooter);
                return;
            }
        }

        // --------- Entity Collision ---------
        handleEntityCollision(laser, level, box, context.hitEffects);

        // --------- 更新位置和行进距离 ---------
        if (prePos == laser.position){
            laser.position = nextPos;
        }
        laser.traveled += laser.step + positionOffset.length();
    }

    // ================== Collision & Damage ==================
    private boolean handleBlockCollision(LaserProjectile laser, ServerLevel level, Set<Integer> hitEffects) {
        BlockHitResult blockHit = level.clip(new ClipContext(
                laser.position,
                laser.position.add(laser.direction.scale(laser.step)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                laser.shooter
        ));

        if (blockHit.getType() == BlockHitResult.Type.BLOCK) {
            IsaacAttackHitBlockEvent blockEvent = new IsaacAttackHitBlockEvent(laser, laser.shooter, getId(), hitEffects, blockHit);
            MinecraftForge.EVENT_BUS.post(blockEvent);

            return !blockEvent.isCanceled();
        }
        return false;
    }

    private void handleEntityCollision(LaserProjectile laser, ServerLevel level, AABB box, Set<Integer> hitEffects) {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != laser.shooter && e.isAlive() && !laser.hitEntities.contains(e)
        );

        for (LivingEntity target : entities) {
            EntityHitResult hitResult = new EntityHitResult(target);

            IsaacAttackBeforeHitEntityEvent beforeHit = new IsaacAttackBeforeHitEntityEvent(
                    laser, laser.shooter, getId(), hitEffects, hitResult, laser.damage
            );

            if (!MinecraftForge.EVENT_BUS.post(beforeHit)) {
                double actualDamage = beforeHit.getDamage();
                laser.hitEntities.add(makeDamage(laser.shooter, target,(float) actualDamage));

                IsaacAttackAfterHitEvent afterHit = new IsaacAttackAfterHitEvent(
                        laser, laser.shooter, getId(), hitEffects, hitResult, actualDamage, target.getHealth()
                );
                MinecraftForge.EVENT_BUS.post(afterHit);
            }
        }
    }

    private LivingEntity makeDamage(LivingEntity source, LivingEntity target, float damage) {
        target.invulnerableTime = 0;
        target.hurt(getDamageSource(source), damage);
        return target;
    }

    private DamageSource getDamageSource(LivingEntity source){
        if (!(source.level() instanceof ServerLevel level)) return source.damageSources().generic();
        var damageTypeHolder = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ResourceKey.create(
                        Registries.DAMAGE_TYPE,
                        ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "laser")
                ));
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

    private void spawnInterpolatedParticles(ServerLevel level, Vec3 from, Vec3 to, double width, int colorId) {
        Vector3f color = BulletColor.getVec3fColorById(colorId);
        if (colorId == BulletColor.BASE.getId()) color = new Vector3f(1f, 0f, 0f);

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

    public double getWidth(LivingEntity living) {
        return getBulletScale(living) * 0.25;
    }
}

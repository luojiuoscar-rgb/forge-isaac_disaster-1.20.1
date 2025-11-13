package net.luojiuoscar.isaac_disaster.manager.attack.types;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.event.custom.attack.*;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.manager.attack.IAttackType;
import net.luojiuoscar.isaac_disaster.manager.id.AttackTypeId;
import net.luojiuoscar.isaac_disaster.manager.id.BulletColorId;
import net.luojiuoscar.isaac_disaster.sound.ModSounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LaserAttack implements IAttackType {

    @Override
    public int getId() {
        return AttackTypeId.LASER.getId();
    }

    @Override
    public double getPriority() {
        return AttackTypeId.LASER.getPriority();
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

    @Override
    public void handleAttack(LivingEntity entity, int colorId, Set<Integer> hitEffects) {
        if (!(entity.level() instanceof ServerLevel level)) return;

        Vec3 startPos = entity.getEyePosition().subtract(0, entity.getBbHeight() * 0.1, 0);
        Vec3 direction = entity.getLookAngle().normalize();

        double range = getRange(entity);
        double width = getWidth(entity);
        float damage = getDamage(entity);

        Set<LivingEntity> hitEntities = new HashSet<>();
        Vec3 currentPos = startPos;
        double traveled = 0;
        double step = 0.5;

        boolean homing = isHoming(entity);

        Vec3 lastPos = currentPos;

        while (traveled < range) {
            if (homing) {
                LivingEntity target = EntityHelper.findNearestTrackingTarget(
                        level,
                        entity,
                        currentPos,
                        8.0,
                        e -> !hitEntities.contains(e)
                );

                if (target != null) {
                    Vec3 toTarget = target.getEyePosition().subtract(currentPos).normalize();
                    direction = smoothTurn(direction, toTarget, 0.15); // 0.15 = 平滑系数
                }
            }

            AABB box = createCollisionBox(currentPos, width);

            if (!isSpectral(entity)) {
                if (handleBlockCollision(entity, level, currentPos, direction, step, hitEffects)) break;
            }

            handleEntityCollision(entity, level, box, hitEntities, damage, hitEffects);

            // 插入粒子，使激光更连续
            spawnInterpolatedParticles(level, lastPos, currentPos, width, colorId);

            lastPos = currentPos;
            currentPos = currentPos.add(direction.scale(step));
            traveled += step;
        }
    }

    // ================= 平滑转向 =================
    private Vec3 smoothTurn(Vec3 currentDir, Vec3 targetDir, double factor) {
        // factor: 0~1，越小越平滑
        Vec3 newDir = currentDir.add(targetDir.subtract(currentDir).scale(factor));
        return newDir.normalize();
    }

    // ================= 粒子插值 =================
    private void spawnInterpolatedParticles(ServerLevel level, Vec3 from, Vec3 to, double width, int colorId) {
        Vector3f color = BulletColorId.getVec3fColorById(colorId);
        if (colorId == BulletColorId.BASE.getId()) color = new Vector3f(1f, 0f, 0f);

        Vec3 delta = to.subtract(from);
        double distance = delta.length();
        int steps = (int) Math.ceil(distance / 0.2); // 每0.2个单位插一个粒子

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            Vec3 pos = from.add(delta.scale(t));
            DustParticleOptions dust = new DustParticleOptions(color, (float) width * 1.5f);
            level.sendParticles(dust, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    // ================= 私有辅助方法 =================

    private AABB createCollisionBox(Vec3 pos, double width) {
        return new AABB(
                pos.subtract(width / 2, width / 2, width / 2),
                pos.add(width / 2, width / 2, width / 2)
        );
    }

    private boolean handleBlockCollision(LivingEntity source, ServerLevel level, Vec3 currentPos, Vec3 direction, double step, Set<Integer> hitEffects) {
        BlockHitResult blockHit = level.clip(new ClipContext(
                currentPos,
                currentPos.add(direction.scale(step)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                source
        ));

        if (blockHit.getType() == BlockHitResult.Type.BLOCK) {
            IsaacAttackHitBlockEvent blockEvent = new IsaacAttackHitBlockEvent(source, getId(), hitEffects, blockHit);
            MinecraftForge.EVENT_BUS.post(blockEvent);
            return !blockEvent.isCanceled();
        }
        return false;
    }

    private void handleEntityCollision(LivingEntity source, ServerLevel level, AABB box, Set<LivingEntity> hitEntities, float damage, Set<Integer> hitEffects) {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != source && e.isAlive() && !hitEntities.contains(e)
        );

        for (LivingEntity target : entities) {
            EntityHitResult hitResult = new EntityHitResult(target);

            IsaacAttackBeforeHitEntityEvent beforeHit = new IsaacAttackBeforeHitEntityEvent(
                    source, getId(), hitEffects, hitResult, damage
            );

            if (!MinecraftForge.EVENT_BUS.post(beforeHit)) {
                double actualDamage = beforeHit.getDamage();
                target.invulnerableTime = 0;
                target.hurt(makeDamageSource(source), (float) actualDamage);
                hitEntities.add(target);

                IsaacAttackAfterHitEvent afterHit = new IsaacAttackAfterHitEvent(
                        source, getId(), hitEffects, hitResult, actualDamage, target.getHealth()
                );
                MinecraftForge.EVENT_BUS.post(afterHit);
            }
        }
    }

    // ================= 参数接口 =================

    public double getRange(LivingEntity living) {
        return Math.min(64, living.getAttributeValue(ModAttributes.BULLET_RANGE.get()));
    }

    public double getWidth(LivingEntity living) {
        double scale = living.getAttributeValue(ModAttributes.BULLET_SCALE.get());
        double damage = living.getAttributeValue(Attributes.ATTACK_DAMAGE);
        return Math.log10(1 + damage) + scale / 2;
    }

    public float getDamage(LivingEntity living) {
        return (float) living.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    // ================= DamageSource =================

    private DamageSource makeDamageSource(LivingEntity source) {
        if (!(source.level() instanceof ServerLevel level)) return source.damageSources().generic();

        var damageTypeHolder = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ResourceKey.create(
                        Registries.DAMAGE_TYPE,
                        ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "laser")
                ));

        return new DamageSource(damageTypeHolder, source, source);
    }
}

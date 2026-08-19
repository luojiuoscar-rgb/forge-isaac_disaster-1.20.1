package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.fireball.TimedFireball;
import net.luojiuoscar.isaac_disaster.entity.tnt.BombData;
import net.luojiuoscar.isaac_disaster.entity.tnt.GigaBomb;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class EntityHelper {


    /**
     * Spawns an Isaac bomb from one of the built-in bomb profiles.
     *
     * <p>The profile sets the default block radius, rendered size, center damage, damage radius,
     * block destruction behavior, and fuse. The explicit fuse argument is applied last because many
     * effects need instant or delayed variants of the same bomb profile.</p>
     */
    public static IsaacBomb spawnBomb(Vec3 position, LivingEntity owner,
                                      Level level, Vec3 velocity, BombData data, int fuse) {
        return spawnBomb(position, owner, level, velocity, data, fuse, true);
    }

    /**
     * Spawns an Isaac bomb from a profile and marks whether it is the original bomb for chained
     * bomb effects such as Bomber Boy or Scatter Bomb.
     */
    public static IsaacBomb spawnBomb(Vec3 position, LivingEntity owner, Level level, Vec3 velocity,
                                      BombData data, int fuse, boolean isOriginal) {
        if (level.isClientSide()) return null;

        IsaacBomb bomb = ModEntities.ISAAC_BOMB.get().create(level);
        if (bomb == null) return null;

        bomb.applyProfile(data);
        bomb.moveTo(position.x, position.y, position.z, 0, 0);
        bomb.setOwner(owner);
        bomb.setFuse(fuse);
        bomb.setOriginal(isOriginal);
        bomb.setDeltaMovement(velocity);

        level.addFreshEntity(bomb);
        return bomb;
    }

    public static IsaacBomb spawnBomb(Vec3 position, LivingEntity owner,
                                      Level level, Vec3 velocity,
                                      int fuse, int power, float scale, boolean isOriginal) {
        IsaacBomb bomb = spawnBomb(position, owner, level, velocity, BombData.fromPower(power), fuse, isOriginal);
        if (bomb == null) return null;

        bomb.setPower(power);
        bomb.setScale(scale);
        return bomb;
    }


    public static void spawnGigaBomb(Vec3 position, LivingEntity entity, Vec3 tntVelocity, int fuse, Level level){
        if (level.isClientSide) return;

        IsaacBomb tnt = ModEntities.GIGA_BOMB.get().create(entity.level());
        if (tnt == null) return;

        tnt.moveTo(position.x, position.y, position.z, 0, 0);
        tnt.setOwner(entity);
        tnt.setFuse(fuse);
        tnt.setPower(10);
        tnt.setScale(2.5f);
        tnt.setOriginal(true);
        tnt.setDeltaMovement(tntVelocity);

        level.addFreshEntity(tnt);
    }
    public static void throwGigaBomb(LivingEntity entity, int fuse){
        // 获取玩家朝向向量
        Vec3 lookVec = entity.getLookAngle();
        // 获取玩家当前速度
        Vec3 playerVelocity = entity.getDeltaMovement();

        // 计算TNT生成位置（玩家眼睛位置略微偏移）
        Vec3 spawnPos = entity.getEyePosition()
                .add(lookVec.x * 0.5, lookVec.y * 0.5, lookVec.z * 0.5);

        // 计算TNT初速度：结合玩家朝向和玩家自身速度
        double throwStrength = 1.3; // 投掷力度
        double velocityInheritance = 1.0; // 继承玩家速度的比例

        Vec3 tntVelocity = new Vec3(
                lookVec.x * throwStrength + playerVelocity.x * velocityInheritance,
                lookVec.y * throwStrength + playerVelocity.y * velocityInheritance + 0.25, // 略微向上
                lookVec.z * throwStrength + playerVelocity.z * velocityInheritance
        );

        spawnGigaBomb(spawnPos, entity, tntVelocity, fuse, entity.level());
    }

    public static void throwBomb(LivingEntity entity, int fuse, int power) {
        throwBomb(entity, fuse, BombData.fromPower(power), power);
    }

    public static void throwBomb(LivingEntity entity, int fuse, int power, float scale) {
        throwBomb(entity, fuse, BombData.fromPower(power), power, scale);
    }

    /**
     * Throws a bomb from a built-in profile while preserving the existing Isaac bomb throw velocity.
     */
    public static void throwBomb(LivingEntity entity, int fuse, BombData profile) {
        throwBomb(entity, fuse, profile, profile.power(), profile.size());
    }

    /**
     * Throws a profile-based bomb and then overrides its explosion radius.
     */
    public static void throwBomb(LivingEntity entity, int fuse, BombData profile, int power) {
        throwBomb(entity, fuse, profile, power, profile.size());
    }

    private static void throwBomb(LivingEntity entity, int fuse, BombData profile, int power, float scale) {
        Vec3 look = entity.getLookAngle();
        Vec3 playerVel = entity.getDeltaMovement();

        // 炸弹生成点：眼睛位置略前
        Vec3 spawnPos = entity.getEyePosition().add(look.scale(0.5));

        // 炸弹初始速度：方向 * 投掷力度
        double throwStrength = 1.3;
        double inherit = 1.0;
        Vec3 velocity = look.scale(throwStrength)
                .add(playerVel.x * inherit,
                        0,
                        playerVel.z * inherit)
                .add(0, 0.25, 0);

        // 若为玩家丢出，且处在飞行状态，则会继承一部分速度
        if (entity instanceof Player player){
            boolean flying = player.getAbilities().flying;
            velocity = look.scale(throwStrength).add(0, flying ? 0 : playerVel.y * inherit, 0);
        }


        IsaacBomb bomb = spawnBomb(spawnPos, entity, entity.level(), velocity, profile, fuse, true);
        if (bomb == null) return;

        bomb.setPower(power);
        bomb.setScale(scale);
    }


    public static void bomberBoy(LivingEntity entity, IsaacBomb source, Vec3 center, Level level) {
        if (!isValidOrigin(source)) return;

        int power = source.getPower();
        float offset = power + 1f;

        Vec3[] offsets = new Vec3[]{
                new Vec3(offset, 0, 0),
                new Vec3(-offset, 0, 0),
                new Vec3(0, 0, offset),
                new Vec3(0, 0, -offset)
        };

        for (Vec3 delta : offsets) {
            spawnBomb(center.add(delta), entity, level, Vec3.ZERO, 0, power, source.getScale(), false);
        }
    }

    /**
     * 炸弹碎裂效果（分裂炸弹）
     */
    public static void scatterBomb(LivingEntity entity, IsaacBomb source, Vec3 center, Level level) {
        if (!isValidOrigin(source)) return;

        int power = source.getPower() - 3;
        float scale = (power == BombData.SMALL.power()) ? BombData.SMALL.size() : BombData.NORMAL.size();

        for (int i = 0; i < 4; i++) {
            Vec3 randomVel = new Vec3(
                    Math.random() * 0.6 - 0.3,
                    Math.random() * 0.4,
                    Math.random() * 0.6 - 0.3
            );
            spawnBomb(center, entity, level, randomVel, 30, power, scale,
                    power != BombData.SMALL.power());
        }
    }

    private static boolean isValidOrigin(IsaacBomb bomb) {
        return bomb != null && bomb.isOriginal() && !(bomb instanceof GigaBomb);
    }

    public static void HotBomb(LivingEntity entity, IsaacBomb tnt, Vec3 pos, Level level){
        if(!isValidOrigin(tnt)) return;

        int power = 0;
        if(tnt.getPower() > 4){
            power = 3;
        }else if (tnt.getPower() > 1){
            power = 5;
        }

        for (int i = 0; i < power; i++) {  // 火球数量可调
            double vx = (level.random.nextDouble() - 0.5) * 0.5;
            double vy = (level.random.nextDouble() - 0.5) * 0.5;
            double vz = (level.random.nextDouble() - 0.5) * 0.5;

            TimedFireball fireball = new TimedFireball(level, entity, vx, vy, vz, power);

            fireball.setPos(pos);
            fireball.setDeltaMovement(new Vec3(vx, vy, vz)); // 设速度
            level.addFreshEntity(fireball);
        }
    }

    public static boolean isFriendly(LivingEntity a, LivingEntity b) {
        if (a == null || b == null) return false;
        if (a == b) return true; // 自己和自己

        if (a instanceof Player playerA && b instanceof Player playerB) {
            if (playerA.level().getServer() != null && !playerA.level().getServer().isPvpAllowed()) return true;
            if (!playerA.canHarmPlayer(playerB) || !playerB.canHarmPlayer(playerA)) return true;
        }

        // Team 友好
        if (a.isAlliedTo(b)) return true; // 内含玩家玩家、玩家实体、实体实体的 team 检测

        // 驯服生物：检查是否有同一主人
        LivingEntity ownerA = getOwner(a);
        LivingEntity ownerB = getOwner(b);

        boolean aHasOwner = ownerA != null;
        boolean bHasOwner = ownerB != null;

        // 如果都是驯服生物，并且是同一个主人
        if (aHasOwner && bHasOwner && ownerA.equals(ownerB)) return true;

        // 生物A 是生物B 驯服的
        if (aHasOwner && ownerA.equals(b)) return true;

        // 生物B 是生物A 驯服的
        if (bHasOwner && ownerB.equals(a)) return true;

        return false;
    }

    private static LivingEntity getOwner(LivingEntity entity) {
        if (entity instanceof TamableAnimal tamable) {
            return tamable.getOwner();
        }
        return null;
    }


    /**
     * 叠加药水效果
     * 其他参数均以最后一个执行的函数为准
     * @param stackDuration 是否叠加时间
     * @param stackAmplifier 是否叠加药水等级
     */
    public static void applyOrStackEffect(LivingEntity entity, MobEffect effect, int duration, int amplifier, boolean stackDuration, boolean stackAmplifier){
        applyOrStackEffect(entity, effect, duration, amplifier, false, false, true, stackDuration, stackAmplifier);
    }
    public static void applyOrStackEffect(LivingEntity entity, MobEffect effect, int duration, int amplifier, boolean isAmbient, boolean isVisible, boolean showIcon, boolean stackDuration, boolean stackAmplifier){
        MobEffectInstance mobEffectInstance = entity.getEffect(effect);
        if (stackAmplifier && mobEffectInstance != null){
            amplifier += mobEffectInstance.getAmplifier() + 1;
        }
        if (stackDuration && mobEffectInstance != null){
            duration += mobEffectInstance.getDuration();
        }

        entity.addEffect(new MobEffectInstance(effect, duration, amplifier, isAmbient, isVisible, showIcon));
    }

    public static void setFireAtEntity(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return;

        BlockPos pos = entity.blockPosition();
        if (level.isEmptyBlock(pos)) {
            level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 11);
        }
    }

    @Nullable
    public static LivingEntity findNearestTrackingTarget(Level level,
                                                         LivingEntity owner,
                                                         Vec3 center,
                                                         double range,
                                                         @Nullable Predicate<LivingEntity> filter) {
        // 搜索范围
        AABB searchBox = new AABB(
                center.x - range, center.y - range, center.z - range,
                center.x + range, center.y + range, center.z + range
        );

        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class,
                searchBox,
                e -> e.isAlive() && e != owner && !e.isInvulnerable() &&
                        (filter == null || filter.test(e))
        );

        LivingEntity hostileAggro = null;
        LivingEntity hostilePassive = null;
        LivingEntity neutral = null;
        LivingEntity playerTarget = null;

        for (LivingEntity e : nearby) {

            if (isFriendly(e, owner)) continue;

            // 优先考虑 Enemy
            if (e instanceof Enemy) {
                boolean hasAggro = false;

                if (e instanceof Monster m) {
                    hasAggro = m.getTarget() == owner;

                    if (m instanceof NeutralMob neutralMob && owner instanceof Player player)
                        if (neutralMob.isAngryAt(player)) hasAggro = true;
                }

                if (hasAggro) {
                    if (hostileAggro == null || e.distanceToSqr(center.x, center.y, center.z) < hostileAggro.distanceToSqr(center.x, center.y, center.z))
                        hostileAggro = e;
                } else if (hostilePassive == null || e.distanceToSqr(center.x, center.y, center.z) < hostilePassive.distanceToSqr(center.x, center.y, center.z))
                    hostilePassive = e;

            }
            // 中立生物
            else if (e instanceof Mob) {
                if (neutral == null || e.distanceToSqr(center.x, center.y, center.z) < neutral.distanceToSqr(center.x, center.y, center.z))
                    neutral = e;
            }
            // 玩家
            else if (e instanceof Player otherPlayer && owner instanceof Player ownerPlayer) {
                if (!ownerPlayer.isAlliedTo(otherPlayer) &&
                        ((ServerLevel) ownerPlayer.level()).getServer().isPvpAllowed() &&
                        ownerPlayer.canHarmPlayer(otherPlayer)) {

                    if (playerTarget == null || e.distanceToSqr(center.x, center.y, center.z) < playerTarget.distanceToSqr(center.x, center.y, center.z))
                        playerTarget = e;
                }
            }
        }

        if (hostileAggro != null) return hostileAggro;
        if (hostilePassive != null) return hostilePassive;
        if (neutral != null) return neutral;
        return playerTarget;
    }

    /** 传送到附近随机安全位置 */
    public static boolean teleportToRandomLocation(LivingEntity entity, double radius) {
        if (entity == null || entity.level().isClientSide()
                || !(entity.level() instanceof ServerLevel level)
                || !Double.isFinite(radius) || radius <= 0.0) {
            return false;
        }

        int minY = level.getMinBuildHeight();
        int maxY = minY + level.getLogicalHeight() - 1;
        int verticalRadius = (int) Math.min(Integer.MAX_VALUE, Math.floor(radius));
        int verticalSpan = Math.max(1,
                (int) Math.min(Integer.MAX_VALUE, Math.floor(radius * 2.0)));

        for (int attempt = 0; attempt < 16; attempt++) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * radius * 2.0D;
            double y = entity.getY() + entity.getRandom().nextInt(verticalSpan) - verticalRadius;
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * radius * 2.0D;
            y = Math.max(minY, Math.min(maxY, y));

            entity.stopRiding();
            if (entity.randomTeleport(x, y, z, true)) {
                return true;
            }
        }

        return false;
    }

}

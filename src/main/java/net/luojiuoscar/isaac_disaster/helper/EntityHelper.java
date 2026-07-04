package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.fireball.TimedFireball;
import net.luojiuoscar.isaac_disaster.entity.tnt.BombData;
import net.luojiuoscar.isaac_disaster.entity.tnt.GigaBomb;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
            else if (e instanceof PathfinderMob) {
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

    public static void teleportToRandomLocation(Entity entity, double radius) {
        if (entity.level().isClientSide()) return;

        Level world = entity.level();
        Vec3 currentPos = entity.position();
        int minY = world.getMinBuildHeight();
        int maxY = world.getMaxBuildHeight();

        RandomSource random = world.getRandom();
        float height = entity.getBbHeight();

        // 最多尝试5次
        for (int retry = 0; retry < 5; retry++) {
            // 生成随机X和Z坐标（在半径范围内）
            int randomX = (int) (currentPos.x + (random.nextDouble() * radius * 2 - radius));
            int randomZ = (int) (currentPos.z + (random.nextDouble() * radius * 2 - radius));
            // 生成初始Y坐标（在世界高度范围内）
            int initialY = (int) (currentPos.y + (random.nextDouble() * radius * 2 - radius));
            initialY = Mth.clamp(initialY, minY, maxY);

            // 从初始Y位置向上寻找安全位置
            BlockPos safePos = findSafePosition(world, randomX, randomZ, initialY, minY, maxY, height);

            if (safePos != null) {
                // 找到安全位置，执行传送（Y坐标稍微向上偏移，避免卡进方块）
                entity.teleportTo(
                        safePos.getX() + 0.5, // 方块中心X
                        safePos.getY() + 0.1, // 稍微高于方块
                        safePos.getZ() + 0.5  // 方块中心Z
                );
                return;
            }
        }
    }

    private static BlockPos findSafePosition(
            Level world,
            int x, int z,
            int startY,
            int minY, int maxY,
            double entityHeight
    ) {
        // 向下搜索
        for (int y = startY; y >= minY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isSafeToStand(world, pos, entityHeight)) {
                return pos;
            }

            y -= skipDownUnsafe(world, pos, entityHeight);
        }

        // 向上搜索
        for (int y = startY + 1; y <= maxY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (isSafeToStand(world, pos, entityHeight)) {
                return pos;
            }
        }

        return null;
    }

    private static int skipDownUnsafe(Level world, BlockPos pos, double entityHeight) {
        int height = (int) Math.ceil(entityHeight);

        // 如果脚下都没支撑，不能跳过（可能下一格才有地面）
        BlockPos below = pos.below();
        if (world.getBlockState(below).getCollisionShape(world, below).isEmpty()) {
            return 0;
        }

        // 如果身体被方块阻挡，可以安全跳过 height - 1 格
        for (int i = 0; i < height; i++) {
            BlockPos bodyPos = pos.above(i);
            if (!world.getBlockState(bodyPos).getCollisionShape(world, bodyPos).isEmpty()) {
                return height - 1;
            }
        }

        return 0;
    }

    private static boolean isSafeToStand(Level world, BlockPos pos, double playerHeight) {
        // 检查脚下是否有支撑（有碰撞箱）
        BlockPos standPos = pos.below();
        BlockState groundState = world.getBlockState(standPos);
        if (groundState.getCollisionShape(world, standPos).isEmpty()) {
            return false; // 脚下无支撑，不安全
        }

        // 计算需要检查的总高度（向上取整，确保覆盖完整碰撞箱）
        int checkHeight = (int) Math.ceil(playerHeight);

        // 检查玩家身体占据的每一格空间是否有碰撞箱
        for (int y = 0; y < checkHeight; y++) {
            BlockPos bodyPos = pos.offset(0, y, 0); // 当前检查的身体位置
            BlockState bodyState = world.getBlockState(bodyPos);

            // 如果身体位置存在碰撞箱，则不安全
            if (!bodyState.getCollisionShape(world, bodyPos).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}

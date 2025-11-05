package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.fireball.TimedFireball;
import net.luojiuoscar.isaac_disaster.entity.tnt.GigaBomb;
import net.luojiuoscar.isaac_disaster.entity.tnt.IsaacBomb;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class EntityHelper {


    /**
     * type 0: small bomb
     * type 1: medium bomb
     * type 2: large bomb
     */
    public static IsaacBomb spawnBomb(Vec3 position, LivingEntity owner, Level level, Vec3 velocity, int type) {
        return switch (type) {
            case 1 -> spawnBomb(position, owner, level, velocity, 80, 4, 0.98f);
            case 2 -> spawnBomb(position, owner, level, velocity, 80, 7, 1.4f);
            default -> spawnBomb(position, owner, level, velocity, 80, 1, 0.4f);
        };
    }


    public static IsaacBomb spawnBomb(Vec3 position, LivingEntity owner, Level level, Vec3 velocity, int fuse, int power, float scale) {
        return spawnBomb(position, owner, level, velocity, fuse, power, scale, true);
    }

    public static IsaacBomb spawnBomb(Vec3 position, LivingEntity owner, Level level, Vec3 velocity,
                                      int fuse, int power, float scale, boolean isOriginal) {
        if (level.isClientSide()) return null;

        IsaacBomb bomb = ModEntities.ISAAC_BOMB.get().create(level);
        if (bomb == null) return null;

        bomb.moveTo(position.x, position.y, position.z, 0, 0);
        bomb.setOwner(owner);
        bomb.setFuse(fuse);
        bomb.setPower(power);
        bomb.setScale(scale);
        bomb.setOriginal(isOriginal);
        bomb.setDeltaMovement(velocity);

        level.addFreshEntity(bomb);
        return bomb;
    }


    public static void spawnGigaBomb(Vec3 position, Player player, Vec3 tntVelocity, int fuse, Level level){
        if (level.isClientSide) return;

        IsaacBomb tnt = ModEntities.GIGA_BOMB.get().create(player.level());
        if (tnt == null) return;

        tnt.moveTo(position.x, position.y, position.z, 0, 0);
        tnt.setOwner(player);
        tnt.setFuse(fuse);
        tnt.setPower(10);
        tnt.setScale(2.5f);
        tnt.setOriginal(true);
        tnt.setDeltaMovement(tntVelocity);

        level.addFreshEntity(tnt);
    }
    public static void throwGigaBomb(Player player, int fuse){
        // 获取玩家朝向向量
        Vec3 lookVec = player.getLookAngle();
        // 获取玩家当前速度
        Vec3 playerVelocity = player.getDeltaMovement();

        // 计算TNT生成位置（玩家眼睛位置略微偏移）
        Vec3 spawnPos = player.getEyePosition()
                .add(lookVec.x * 0.5, lookVec.y * 0.5, lookVec.z * 0.5);

        // 计算TNT初速度：结合玩家朝向和玩家自身速度
        double throwStrength = 1.3; // 投掷力度
        double velocityInheritance = 1.0; // 继承玩家速度的比例

        Vec3 tntVelocity = new Vec3(
                lookVec.x * throwStrength + playerVelocity.x * velocityInheritance,
                lookVec.y * throwStrength + playerVelocity.y * velocityInheritance + 0.25, // 略微向上
                lookVec.z * throwStrength + playerVelocity.z * velocityInheritance
        );

        spawnGigaBomb(spawnPos, player, tntVelocity, fuse, player.level());
    }

    public static void throwBomb(Player player, int fuse, int power) {
        throwBomb(player, fuse, power, 0.98f);
    }

    public static void throwBomb(Player player, int fuse, int power, float scale) {
        Vec3 look = player.getLookAngle();
        Vec3 playerVel = player.getDeltaMovement();

        // 炸弹生成点：眼睛位置略前
        Vec3 spawnPos = player.getEyePosition().add(look.scale(0.5));

        // 炸弹初始速度：方向 * 投掷力度 + 玩家速度
        double throwStrength = 1.3;
        double inherit = 1.0;

        Vec3 velocity = look.scale(throwStrength)
                .add(playerVel.scale(inherit))
                .add(0, 0.25, 0); // 略微向上

        spawnBomb(spawnPos, player, player.level(), velocity, fuse, power, scale);
    }

    /**
     * 炸弹四向扩散（炸弹人）
     */
    public static void bomberBoy(Player player, IsaacBomb source, Vec3 center, Level level) {
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
            spawnBomb(center.add(delta), player, level, Vec3.ZERO, 0, power, source.getScale(), false);
        }
    }

    /**
     * 炸弹碎裂效果（分裂炸弹）
     */
    public static void scatterBomb(Player player, IsaacBomb source, Vec3 center, Level level) {
        if (!isValidOrigin(source)) return;

        int power = source.getPower() - 3;
        float scale = (power == 1) ? 0.4f : 0.98f;

        for (int i = 0; i < 4; i++) {
            Vec3 randomVel = new Vec3(
                    Math.random() * 0.6 - 0.3,
                    Math.random() * 0.4,
                    Math.random() * 0.6 - 0.3
            );
            spawnBomb(center, player, level, randomVel, 30, power, scale, power != 1);
        }
    }

    private static boolean isValidOrigin(IsaacBomb bomb) {
        return bomb != null && bomb.isOriginal() && !(bomb instanceof GigaBomb);
    }

    public static void HotBomb(Player player, IsaacBomb tnt, Vec3 pos, Level level){
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

            TimedFireball fireball = new TimedFireball(level, player, vx, vy, vz, power);

            fireball.setPos(pos);
            fireball.setDeltaMovement(new Vec3(vx, vy, vz)); // 设速度
            level.addFreshEntity(fireball);
        }
    }

    public static boolean isFriendlyToPlayer(LivingEntity entity, LivingEntity player) {
        if (player == null || entity == null) return false;
        if (entity == player) return true; // 跳过本身

        // 被同一玩家驯服的生物
        if (entity instanceof TamableAnimal tamable) {
            LivingEntity tamer = tamable.getOwner();
            if (tamer != null && tamer.equals(player)) return true;
        }

        // 玩家之间是队友
        if (entity instanceof Player p && player instanceof Player o && o.isAlliedTo(p)) return true;

        return false;
    }

    /**
     * 对于*可叠加*相关的药水效果
     * 其他参数均以最后一个执行的函数为准
     *
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

    /**
     * 对于*层数*相关的药水效果
     */
    public static void addAmplifier(LivingEntity entity, MobEffect effect){
        addAmplifier(entity, effect, 1);
    }
    public static void addAmplifier(LivingEntity entity, MobEffect effect, int count){
        int amplifier = entity.getEffect(effect) == null ? -1 : entity.getEffect(effect).getAmplifier();
        amplifier += count;

        entity.removeEffect(effect);
        if (amplifier < 0) return;

        MobEffectInstance newEffect = new MobEffectInstance(
                effect,
                -1,
                amplifier,
                false,
                false,
                true
        );
        entity.addEffect(newEffect);
    }

    public static void setFireAtEntity(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return;

        BlockPos pos = entity.blockPosition();
        if (level.isEmptyBlock(pos)) {
            level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 11);
        }
    }

}

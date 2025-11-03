package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class LevelHelper {
    public static List<LivingEntity> selectBySphere(Level level, double x, double y, double z, double radius) {
        List<Entity> entities = selectBySphere(level, x, y, z, radius, e -> e instanceof LivingEntity);
        return entities.stream().map(e -> (LivingEntity) e).toList();
    }
    public static List<LivingEntity> selectBySquare(Level level, double x, double y, double z, double radius) {
        List<Entity> entities = selectBySquare(level, x, y, z, radius, e -> e instanceof LivingEntity);
        return entities.stream().map(e -> (LivingEntity) e).toList();
    }
    public static List<Entity> selectBySphere(Level level, double x, double y, double z, double radius,
                                              @Nullable Predicate<Entity> filter) {
        List<Entity> targetEntities = new ArrayList<>();

        AABB area = new AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        );

        List<Entity> entities = level.getEntitiesOfClass(Entity.class, area);

        for (Entity target : entities) {
            double dx = target.getX() - x;
            double dy = target.getY() - y;
            double dz = target.getZ() - z;
            double distanceSq = dx * dx + dy * dy + dz * dz;

            if (distanceSq > radius * radius) continue;
            if (filter != null && !filter.test(target)) continue;
            targetEntities.add(target);
        }

        return targetEntities;
    }
    public static List<Entity> selectBySquare(Level level, double x, double y, double z, double radius,
                                              @Nullable Predicate<Entity> filter) {
        AABB area = new AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        );

        List<Entity> entities = level.getEntitiesOfClass(Entity.class, area);
        if (filter == null) return entities;
        return entities.stream().filter(filter).toList();
    }

    public static LivingEntity findNearestLivingEntity(Level level, double x, double y, double z, double radius, Predicate<LivingEntity> filter) {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius),
                filter);

        LivingEntity nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            double dx = entity.getX() - x;
            double dy = entity.getY() - y;
            double dz = entity.getZ() - z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = entity;
            }
        }
        return nearest;
    }


    public static <T extends Entity> T findNearestOfType(Level level, double x, double y, double z, double radius, Class<T> type){
        return findNearestOfType(level, x, y, z, radius, type, null);
    }
    public static <T extends Entity> T findNearestOfType(Level level, double x, double y, double z, double radius, Class<T> type, @Nullable Predicate<Entity> filter) {
        List<T> entities = level.getEntitiesOfClass(type,
                new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius));
        if (filter != null){
            entities = entities.stream().filter(filter).toList();
        }

        T nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (T entity : entities) {
            if (entity.isRemoved() || !entity.isAlive()) continue;
            double dx = entity.getX() - x;
            double dy = entity.getY() - y;
            double dz = entity.getZ() - z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = entity;
            }
        }
        return nearest;
    }



    public static void pushEntities(Level world, Vec3 centerPos, double radius, double pushStrength, double yStrength) {
        // 获取范围内的所有生物
        List<LivingEntity> targetEntities = selectBySphere(world, centerPos.x, centerPos.y, centerPos.z, radius);

        // 推开它们
        for (LivingEntity entity : targetEntities) {
            pushEntity(world, entity, centerPos, pushStrength, yStrength);
        }
    }
    public static void pushEntity(Level world, LivingEntity entity, Vec3 centerPos, double pushStrength, double yStrength) {
        if (entity == null) return;  // 检查生物是否有效

        // 计算推开方向（从生物位置到中心坐标的反方向）
        Vec3 direction = entity.position().subtract(centerPos).normalize();
        Vec3 push = direction.scale(pushStrength); // 推开力度

        // 应用推力到生物
        entity.push(push.x, push.y + yStrength, push.z);
    }


    /**
     * 在世界中生成粒子（类似于 /particle 命令）
     *
     * @param level    世界（必须是 ServerLevel）
     * @param particle 粒子类型（如 ParticleTypes.FLAME）
     * @param pos      生成位置
     * @param dx       扩散范围X
     * @param dy       扩散范围Y
     * @param dz       扩散范围Z
     * @param speed    粒子速度
     * @param count    数量
     * @param force    是否强制显示（相当于 /particle 的 "force"）
     * @param player   指定玩家（null 则对所有玩家可见）
     */
    public static void spawnParticle(ServerLevel level, ParticleOptions particle, Vec3 pos,
                             double dx, double dy, double dz, double speed,
                             int count, boolean force, ServerPlayer player) {
        if (level == null) return;

        if (player != null) {
            // 仅对指定玩家可见
            level.sendParticles(player, particle, force, pos.x, pos.y, pos.z, count, dx, dy, dz, speed);
        } else {
            // 对所有玩家可见
            level.sendParticles(particle, pos.x, pos.y, pos.z, count, dx, dy, dz, speed);
        }
    }
    public static void spawnParticle(ServerLevel level, ParticleOptions particle, BlockPos pos) {
        spawnParticle(level, particle, Vec3.atCenterOf(pos), 0, 0, 0, 0, 1, false, null);
    }
    public static void spawnParticle(ServerLevel level, ParticleOptions particle, Vec3 pos, double dx, double dy, double dz, int count) {
        spawnParticle(level, particle, pos, dx, dy, dz, 0, count, false, null);
    }

    public static void spawnMoney(ServerLevel level, Vec3 pos, int amount){
        int value = Config.COIN_TIER_1_VALUE.get();
        String moneyId = Config.COIN_TIER_1_ID.get();

        if (value <= 0) value = 1;
        // 计算需要给予的数量
        int count = (int) Math.ceil((double) amount / value);

        ResourceLocation rl = ResourceLocation.tryParse(moneyId);
        if (rl == null) return;
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        // 检查是否有效
        if (item == null || item == Items.AIR) return;
        ItemStack stack = new ItemStack(item, count);
        ItemEntity itemEntity = new ItemEntity(level, pos.x, pos.y, pos.z, stack);
        itemEntity.setPickUpDelay(20);
        level.addFreshEntity(itemEntity);
    }

    public static boolean isCoin(Item item){
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) return false;

        return id.toString().equals(Config.COIN_TIER_1_ID.get())
                || id.toString().equals(Config.COIN_TIER_2_ID.get())
                || id.toString().equals(Config.COIN_TIER_3_ID.get());
    }


}


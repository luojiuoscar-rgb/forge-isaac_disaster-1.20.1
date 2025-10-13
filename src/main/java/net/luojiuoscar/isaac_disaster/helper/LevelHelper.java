package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class LevelHelper {
    public static void spawnLootAtPos(ServerLevel level, Vec3 pos, ResourceLocation tableId) {
        LootTable table = level.getServer().getLootData().getLootTable(tableId);

        // LootParams 中至少需要 Origin
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, pos)
                .create(LootContextParamSets.EMPTY);

        // 获取掉落结果
        List<ItemStack> generated = table.getRandomItems(params);

        // 生成到世界中
        for (ItemStack stack : generated) {
            level.addFreshEntity(new ItemEntity(level, pos.x, pos.y, pos.z, stack));
        }
    }

    public static List<LivingEntity> selectBySphere(Level level, double x, double y, double z, double radius){
        List<LivingEntity> targetEntities = new ArrayList<>();

        // 计算搜索范围
        AABB area = new AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        );

        // 搜索范围内所有生物实体
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

        // 遍历并记录
        for (LivingEntity target : entities) {
            // 精确球形检测
            double dx = target.getX() - x;
            double dy = target.getY() - y;
            double dz = target.getZ() - z;
            double distanceSq = dx * dx + dy * dy + dz * dz;

            if (distanceSq > radius * radius) continue; // 不在球体内则跳过 (距离不开方)
            targetEntities.add(target);
        }
        return targetEntities;
    }
    public static List<LivingEntity> selectBySquare(Level level, double x, double y, double z, double radius){
        // 计算搜索范围
        AABB area = new AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        );
        return level.getEntitiesOfClass(LivingEntity.class, area);
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
    public static <T extends LivingEntity> T findNearestOfType(Level level, double x, double y, double z, double radius, Class<T> type) {
        List<T> entities = level.getEntitiesOfClass(type,
                new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius));

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
}

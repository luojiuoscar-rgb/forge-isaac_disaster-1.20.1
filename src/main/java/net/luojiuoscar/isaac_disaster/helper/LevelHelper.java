package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.Config;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
        spawnParticle(level, particle, net.minecraft.world.phys.Vec3.atCenterOf(pos), 0, 0, 0, 0, 1, false, null);
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

    public static List<ItemStack> getMoney(int amount) {
        List<ItemStack> result = new ArrayList<>();

        // 从 Config 获取金币信息
        int[] values = {
                Config.COIN_TIER_3_VALUE.get(),
                Config.COIN_TIER_2_VALUE.get(),
                Config.COIN_TIER_1_VALUE.get()
        };
        String[] ids = {
                Config.COIN_TIER_3_ID.get(),
                Config.COIN_TIER_2_ID.get(),
                Config.COIN_TIER_1_ID.get()
        };

        // 遍历金币类型，从高到低兑换
        for (int i = 0; i < values.length; i++) {
            int value = values[i];
            if (value <= 0) continue;

            int count = amount / value;
            amount %= value;

            if (count <= 0) continue;

            ResourceLocation rl = ResourceLocation.tryParse(ids[i]);
            if (rl == null) continue;

            Item item = ForgeRegistries.ITEMS.getValue(rl);
            if (item == null || item == Items.AIR) continue;

            while (count > 0) {
                int stackCount = Math.min(count, 64);
                result.add(new ItemStack(item, stackCount));
                count -= stackCount;
            }
        }

        // 如果还有剩余的 amount（无法被任何币种整除）
        if (amount > 0) {
            // 默认用最小币种补足
            ResourceLocation rl = ResourceLocation.tryParse(Config.COIN_TIER_1_ID.get());
            if (rl != null) {
                Item item = ForgeRegistries.ITEMS.getValue(rl);
                if (item != null && item != Items.AIR) {
                    int value = Config.COIN_TIER_1_VALUE.get();
                    int count = (int) Math.ceil((double) amount / value);
                    while (count > 0) {
                        int stackCount = Math.min(count, 64);
                        result.add(new ItemStack(item, stackCount));
                        count -= stackCount;
                    }
                }
            }
        }

        return result;
    }

    public static void spawnRandomStructure(ServerLevel level, BlockPos pos) {
        CompletableFuture.runAsync(() -> {
            try {
                RegistryAccess registryAccess = level.registryAccess();
                var structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);

                List<ResourceLocation> structureIds = structureRegistry.keySet().stream().toList();
                if (structureIds.isEmpty()) return;

                RandomSource random = level.getRandom();
                ResourceLocation randomId = structureIds.get(random.nextInt(structureIds.size()));
                Optional<Structure> optional = structureRegistry.getOptional(randomId);
                if (optional.isEmpty()) return;

                Structure structure = optional.get();

                ChunkPos chunkPos = new ChunkPos(pos);
                ChunkGenerator generator = level.getChunkSource().getGenerator();
                StructureManager structureManager = level.structureManager();
                StructureTemplateManager templateManager = level.getStructureManager();

                // 准备结构生成
                StructureStart start = structure.generate(
                        registryAccess,
                        generator,
                        generator.getBiomeSource(),
                        level.getChunkSource().randomState(),
                        templateManager,
                        level.getSeed(),
                        chunkPos,
                        0,
                        level,
                        biomeHolder -> true
                );

                if (!start.isValid()) {
                    IsaacDisaster.LOGGER.warn("Failed to prepare structure: {}", randomId);
                    return;
                }

                // 回到主线程执行方块放置
                level.getServer().execute(() -> {
                    try {
                        start.placeInChunk(
                                level,
                                structureManager,
                                generator,
                                level.getRandom(),
                                start.getBoundingBox(),
                                chunkPos
                        );
                    } catch (Exception e) {
                        IsaacDisaster.LOGGER.error("Error placing structure {}: {}", randomId, e);
                    }
                });

            } catch (Exception e) {
                IsaacDisaster.LOGGER.error("Error generating structure async: ", e);
            }
        });
    }

    public static void explodeCustom(LivingEntity source, Vec3 pos, float power,
                                     float damage, boolean ignoreFriendly, boolean hasFire){
        if (!(source.level() instanceof ServerLevel level)) return;
        Explosion explosion = new Explosion(
                level,
                source,                  // 爆炸来源实体
                null,                    // DamageSource
                null,                    // 伤害计算器使用自定义的
                pos.x, pos.y, pos.z,
                power,
                hasFire,
                Explosion.BlockInteraction.DESTROY // 破坏方块
        );
        explosion.explode();
        explosion.finalizeExplosion(true);

        level.playSound(
                null,
                pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                0.8F,
                (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F
        );

        for (int i = 0; i < 10; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * power * 2;
            double offsetY = (level.random.nextDouble() - 0.5) * power * 2;
            double offsetZ = (level.random.nextDouble() - 0.5) * power * 2;

            level.sendParticles(
                    ParticleTypes.EXPLOSION,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );
        }



        List<LivingEntity> livingEntities = LevelHelper.selectBySquare(source.level(), pos.x, pos.y, pos.z,
                power + 2);

        for (LivingEntity entity : livingEntities){
            if (ignoreFriendly && EntityHelper.isFriendly(entity, source)) continue;
            entity.hurt(source.damageSources().explosion(source, null), damage);
        }
    }
}


package net.luojiuoscar.isaac_disaster.helper;

import net.luojiuoscar.isaac_disaster.loot.LootGenerationContext;
import net.luojiuoscar.isaac_disaster.loot.LootGenerationMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * LootHelper - 用于在指定位置生成或提取掉落物。
 */
public class LootHelper {

    public static void spawnLootAtPos(LivingEntity source, Vec3 pos, ResourceLocation tableId) {
        spawnLootAtPos(source, pos, tableId, LootContextParamSets.EMPTY);
    }
    public static void spawnLootAtPos(LivingEntity source, Vec3 pos, ResourceLocation tableId, LootContextParamSet paramSet) {
        if (!(source.level() instanceof ServerLevel level)) return;
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, pos)
                .withParameter(LootContextParams.THIS_ENTITY, source)
                .create(paramSet);
        spawnLootAtPos(level, pos, tableId, params);
    }
    public static void spawnLootAtPos(LivingEntity source, Vec3 pos, ResourceLocation tableId, int count) {
        if (!(source.level() instanceof ServerLevel level)) return;
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, pos)
                .withParameter(LootContextParams.THIS_ENTITY, source)
                .create(LootContextParamSets.EMPTY);
        for (int i = 0; i < count; i++) {
            spawnLootAtPos(level, pos, tableId, params);
        }
    }
    public static void spawnLootAtPos(ServerLevel level, Vec3 pos, ResourceLocation tableId, LootParams params) {
        spawnLootAtPos(level, pos, tableId, params, LootGenerationMode.SPAWN_DROP);
    }

    /**
     * Spawns loot items at a position while marking why this table roll is happening.
     */
    public static void spawnLootAtPos(ServerLevel level, Vec3 pos, ResourceLocation tableId,
                                      LootParams params, LootGenerationMode mode) {
        List<ItemStack> generated = getLoot(level, tableId, params, mode);
        for (ItemStack stack : generated) {
            level.addFreshEntity(new ItemEntity(level, pos.x, pos.y, pos.z, stack));
        }
    }
    public static void spawnItemViaLoot(LivingEntity source, Vec3 pos, Item item, int count) {
        if (source == null || !(source.level() instanceof ServerLevel level)) return;

        // 创建一个自定义 LootTable
        LootPool.Builder poolBuilder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(count)) // 掉落次数 = count
                .add(LootItem.lootTableItem(item));

        LootTable lootTable = LootTable.lootTable()
                .withPool(poolBuilder)
                .build();

        // 构建 LootContext
        LootParams.Builder paramsBuilder = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, pos)
                .withParameter(LootContextParams.THIS_ENTITY, source)
                .withLuck(getLuck(source));
        LootParams params = paramsBuilder.create(LootContextParamSets.EMPTY);

        List<ItemStack> generatedLoot = LootGenerationContext.supply(
                LootGenerationMode.SPAWN_DROP,
                () -> lootTable.getRandomItems(params));

        // 掉落
        for (ItemStack stack : generatedLoot) {
            ItemEntity entity = new ItemEntity(level, pos.x, pos.y, pos.z, stack);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
    }



    /**
     * 从自定义 LootParams 中获取战利品列表
     */
    public static List<ItemStack> getLoot(ServerLevel level, ResourceLocation tableId, LootParams params) {
        return getLoot(level, tableId, params, LootGenerationMode.SPAWN_DROP);
    }

    /**
     * Rolls a loot table while marking the current generation mode.
     */
    public static List<ItemStack> getLoot(ServerLevel level, ResourceLocation tableId,
                                          LootParams params, LootGenerationMode mode) {
        LootTable table = level.getServer().getLootData().getLootTable(tableId);
        return LootGenerationContext.supply(mode, () -> new ArrayList<>(table.getRandomItems(params)));
    }

    /**
     * 直接根据参数返回战利品
     */
    public static List<ItemStack> generateLoot(ServerLevel level, ResourceLocation tableId, LootParams.Builder builder, LootContextParamSet paramSet) {
        LootParams params = builder.create(paramSet);
        return getLoot(level, tableId, params);
    }

    /**
     * Rolls a loot table from a builder while marking the current generation mode.
     */
    public static List<ItemStack> generateLoot(ServerLevel level, ResourceLocation tableId,
                                               LootParams.Builder builder, LootContextParamSet paramSet,
                                               LootGenerationMode mode) {
        LootParams params = builder.create(paramSet);
        return getLoot(level, tableId, params, mode);
    }

    private static float getLuck(LivingEntity entity){
        AttributeInstance instance = entity.getAttribute(Attributes.LUCK);
        return instance == null ? 0.0f : (float) instance.getValue();
    }
}

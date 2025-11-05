package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
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
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, pos)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, source)
                .create(paramSet);
        spawnLootAtPos(level, pos, tableId, params);
    }
    public static void spawnLootAtPos(LivingEntity source, Vec3 pos, ResourceLocation tableId, int count) {
        if (!(source.level() instanceof ServerLevel level)) return;
        LootParams params = new LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, pos)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, source)
                .create(LootContextParamSets.EMPTY);
        for (int i = 0; i < count; i++) {
            spawnLootAtPos(level, pos, tableId, params);
        }
    }
    public static void spawnLootAtPos(ServerLevel level, Vec3 pos, ResourceLocation tableId, LootParams params) {
        List<ItemStack> generated = getLoot(level, tableId, params);
        for (ItemStack stack : generated) {
            level.addFreshEntity(new ItemEntity(level, pos.x, pos.y, pos.z, stack));
        }
    }

    /**
     * 从自定义 LootParams 中获取战利品列表
     */
    public static List<ItemStack> getLoot(ServerLevel level, ResourceLocation tableId, LootParams params) {
        LootTable table = level.getServer().getLootData().getLootTable(tableId);
        return new ArrayList<>(table.getRandomItems(params));
    }

    /**
     * 直接根据参数返回战利品
     */
    public static List<ItemStack> generateLoot(ServerLevel level, ResourceLocation tableId, LootParams.Builder builder, LootContextParamSet paramSet) {
        LootParams params = builder.create(paramSet);
        return getLoot(level, tableId, params);
    }
}

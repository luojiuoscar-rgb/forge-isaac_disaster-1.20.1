package net.luojiuoscar.isaac_disaster.helper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

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
}

package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luojiuoscar.isaac_disaster.event.custom.misc.GeneralLootModifyEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * 效果是翻倍loot数量但是不叠加。
 */
public class MomsKey implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.get(ContextKeys.EVENT) instanceof GeneralLootModifyEvent event)) return false;
        var objectArrayList = event.getObjectArrayList();
        var lootContext = event.getLootContext();

        if (objectArrayList.isEmpty()) return true;

        Vec3 pos = lootContext.getParamOrNull(LootContextParams.ORIGIN);
        if (pos == null) return true;

        // 通过构造不同的ParamSet来避免循环
        ServerLevel level = lootContext.getLevel();
        if (!(level.getBlockEntity(BlockPos.containing(pos)) instanceof RandomizableContainerBlockEntity)) return true;
        if (!(lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player)) return true;

        ResourceLocation lootId = lootContext.getQueriedLootTableId();
        // get lootManager
        var server = player.server;
        var lootManager = server.getLootData();
        var lootTable = lootManager.getLootTable(lootId);

        LootParams.Builder paramsBuilder = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withLuck((float) getLuck(player));
        LootParams params = paramsBuilder.create(LootContextParamSets.EMPTY);

        ObjectArrayList<ItemStack> newLoot = lootTable.getRandomItems(params);

        newLoot.addAll(objectArrayList);

        event.setObjectArrayList(newLoot);
        return true;
    }
}

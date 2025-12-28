package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Set;

public class CurseOfTheTower implements ITriggerModule {
    private static final String DATA_NAME = "CurseOfTheTower";

    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(TriggerCategory.ON_HURT_NEGATIVE);
    }

    @Override
    public void onHurtNegative(LivingHurtEvent event, int stacks, TriggerModuleQueue queue) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (PlayerHelper.hasItem(ItemId.BLACK_CANDLE.getId(), player)) return;

        // 如果为true（禁用状态）则取消释放；同时启用受伤效果（防止退出游戏丢失schedule后永久禁用）
        var map = PlayerHelper.getExtraData(player);
        if (map.getOrDefault(DATA_NAME, 0.0) == 0) {
            map.put(DATA_NAME, 1.0);
            return;
        }
        // 生成随机炸弹
        PlayerHelper.spawnRandomBombsNearby(player, StatManager.getNearbyRange() * 0.5, 6);

        map.put(DATA_NAME, 1.0);
        // 计划重启
        ScheduledFuncHelper.schedule("curse_of_the_tower_cool_down", 120, 0, false,
                () -> map.put(DATA_NAME, 0.0));
    }
}

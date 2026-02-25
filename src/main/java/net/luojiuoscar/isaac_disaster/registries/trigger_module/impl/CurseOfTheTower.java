package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Set;

public class CurseOfTheTower implements ITriggerModule {
    private static final ResourceLocation DATA_KEY =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "curse_of_the_tower");
    private static final ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "curse_of_the_tower_schedule");



    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of(TriggerCategory.ON_HURT_NEGATIVE);
    }

    @Override
    public void onHurtNegative(LivingHurtEvent event, int stacks, TriggerModuleQueue queue) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (PlayerHelper.hasItem(ItemId.BLACK_CANDLE.getId(), player)) return;

        player.getCapability(ExtraDataProvider.EXTRA_DATA_CAP).ifPresent(
                extraData -> {
                    double cd = extraData.getDouble(DATA_KEY) == null ? 0 : extraData.getDouble(DATA_KEY);

                    if (cd > 0){
                        ScheduledFuncHelper.scheduleForPlayer(player.getUUID(),
                                SCHEDULE_TYPE, 20, 20, (int) cd, true, () -> {
                                    extraData.setDouble(DATA_KEY, extraData.getDouble(DATA_KEY) - 1);
                                });
                        return;
                    }

                    // 生成随机炸弹
                    PlayerHelper.spawnRandomBombsNearby(player, StatManager.getNearbyRange() * 0.5, 6);

                    extraData.setDouble(DATA_KEY, 6);
                    ScheduledFuncHelper.scheduleForPlayer(player.getUUID(),
                            SCHEDULE_TYPE, 20,20, 6, true, () -> {
                        extraData.setDouble(DATA_KEY, extraData.getDouble(DATA_KEY) - 1);
                    });
                }
        );
    }
}

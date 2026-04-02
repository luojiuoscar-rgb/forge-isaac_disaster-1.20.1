package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.ExtraDataProvider;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.helper.ScheduledFuncHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.AbilityEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class CurseOfTheTower implements IAbilityEffect {
    private static final ResourceLocation DATA_KEY =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "curse_of_the_tower");
    private static final ResourceLocation SCHEDULE_TYPE =
            ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "curse_of_the_tower_schedule");


    @Override
    public boolean applyEffect(AbilityEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        if (PlayerHelper.hasItem(ItemId.BLACK_CANDLE.getId(), player)) return true;

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

        return true;
    }
}

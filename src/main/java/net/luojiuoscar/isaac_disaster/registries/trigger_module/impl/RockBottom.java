package net.luojiuoscar.isaac_disaster.registries.trigger_module.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.event.custom.misc.UpdateStatusDisplayValueEvent;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ITriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerCategory;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.TriggerModuleQueue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public class RockBottom implements ITriggerModule {
    @Override
    public Set<TriggerCategory> getTriggerType() {
        return Set.of();
    }

    private static final String DATA_NAME = "RockBottom";

    private static final List<UUID> UUIDS = List.of(
            StatManager.MOVEMENT_SPEED.getUUID(),
            StatManager.TEARS.getUUID(),
            StatManager.TEARS_CORRECTION.getUUID(),
            StatManager.DAMAGE.getUUID(),
            StatManager.RANGE.getUUID(),
            StatManager.BULLET_SPEED.getUUID(),
            StatManager.LUCK.getUUID()
    );

    @Override
    public void onAdded(LivingEntity entity, TriggerModuleQueue queue) {
        if (!(entity instanceof ServerPlayer player)) return;

        var map = PlayerHelper.getExtraData(player);

        IsaacDisaster.LOGGER.info("ADDED Rock Bottom!");
        // 保存当前值
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {
                    for (UUID uuid : UUIDS){
                        String name = DATA_NAME + "_" + StatManager.fromUUID(uuid).getKey();
                        var statInst = playerStatModifier.getStatInstance(uuid);
                        if (statInst == null) continue;

                        map.put(name, statInst.getDisplayValue());

                        IsaacDisaster.LOGGER.info("Putted data: {} -> {}", uuid, statInst.getDisplayValue());
                    }
                }
        );
    }

    @Override
    public void onRemove(LivingEntity entity, TriggerModuleQueue queue) {
        if (!(entity instanceof ServerPlayer player)) return;

        // 所有石头底座模块被移除时，移除保存的最高值
        if (!queue.contains(ModTriggerModule.ROCK_BOTTOM.getId())){
            var map = PlayerHelper.getExtraData(player);

            for (UUID uuid : UUIDS){
                String name = DATA_NAME + "_" + StatManager.fromUUID(uuid).getKey();
                map.remove(name);
                StatManager.fromUUID(uuid).refresh(player); // 刷新数据

                IsaacDisaster.LOGGER.info("Removed and refreshed data: {}", name);
            }
        }
    }

    public void onTriggered(UpdateStatusDisplayValueEvent event){
        ServerPlayer player = event.getPlayer();
        Map<UUID, Double> values = new HashMap<>();
        var map = PlayerHelper.getExtraData(player);

        // 获取到所有的status instances的值
        player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).ifPresent(
                playerStatModifier -> {
                    for (UUID uuid : UUIDS){
                        var statInst = playerStatModifier.getStatInstance(uuid);
                        if (statInst == null) continue;

                        values.put(uuid, statInst.getDisplayValue());
                        IsaacDisaster.LOGGER.info("On triggered, recorded value: {}",
                                statInst.getDisplayValue());
                    }
                }
        );

        // 根据status instance计算最大值
        player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                playerAbility -> {
                    for (UUID uuid : UUIDS){
                        if (!values.containsKey(uuid)) continue;

                        String name = DATA_NAME +"_" + StatManager.fromUUID(uuid).getKey();
                        double currVal = values.get(uuid);
                        double highest = map.getOrDefault(name, currVal);

                        if (!map.containsKey(name) || currVal > highest){
                            map.put(name, currVal); // 大于或记录不存在时，记录最高值
                            IsaacDisaster.LOGGER.info("Larger, record: {}", currVal);

                        }else if (currVal < highest){
                            event.getInst().setDisplayValue(highest); // 小于时，重置数值
                            IsaacDisaster.LOGGER.info("Smaller, set: {}", highest);
                        }

                    }
                }
        );


    }
}

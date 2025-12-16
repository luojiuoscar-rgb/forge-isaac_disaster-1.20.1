package net.luojiuoscar.isaac_disaster.registries.ability.set.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class Adult extends SetAbility {
    public Adult(int id) {
        super(Component.translatable("set.isaac_disaster.adult"), id, 0);
    }

    @Override
    public void onFirstObtain(Player player){
        StatManager.healHealth(player, 1); // 首次获取时恢复一定血量
    }

    @Override
    public void onObtainEffect(Player player) {
        StatManager.MAX_HEALTH.apply(player, 1);
    }

    @Override
    public void onRemoveEffect(Player player) {
        StatManager.MAX_HEALTH.apply(player, -1);
    }

    @Override
    public List<Component> getExtraDesc() {
        return List.of(
                Component.translatable("set.isaac_disaster.adult").append(": ")
                        .append(StatManager.MAX_HEALTH.description(1)),
                Component.translatable("set.isaac_disaster.adult.lore.1")
        );
    }
}

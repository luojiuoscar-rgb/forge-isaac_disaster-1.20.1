package net.luojiuoscar.isaac_disaster.registries.ability.set.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.familiar.ModFamiliarEntities;
import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class Mom extends SetAbility {
    public Mom(int id) {
        super(Component.translatable("set.isaac_disaster.mom"), id, 3);
    }

    @Override
    public void onFirstObtain(Player player) {

    }

    @Override
    public void onObtainEffect(Player player) {
        StatManager.addFamiliar(player, ModFamiliarEntities.MOM_KNIFE.getId(), 1);
    }

    @Override
    public void onRemoveEffect(Player player) {
        StatManager.addFamiliar(player, ModFamiliarEntities.MOM_KNIFE.getId(), -1);
    }

    @Override
    public List<Component> getExtraDesc() {
        return List.of(
                Component.translatable("set.isaac_disaster.mom").append(": ")
                        .append(Component.translatable("set.isaac_disaster.mom.lore.1"))
        );
    }

}

package net.luojiuoscar.isaac_disaster.registries.ability.set.impl;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.set.SetAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class Spun extends SetAbility {
    public Spun(int id) {
        super(Component.translatable("set.isaac_disaster.spun"), id, 3);
    }

    @Override
    public void onFirstObtain(Player player){
        LootHelper.spawnLootAtPos(player, player.blockPosition().getCenter(), ModLootTables.RANDOM_PILLS);
    }

    @Override
    public void onObtainEffect(Player player) {
        StatManager.DAMAGE.apply(player, 2);
        StatManager.MOVEMENT_SPEED.apply(player, 0.75);
    }

    @Override
    public void onRemoveEffect(Player player) {
        StatManager.DAMAGE.apply(player, -2);
        StatManager.MOVEMENT_SPEED.apply(player, -0.75);
    }

    @Override
    public List<Component> getExtraDesc() {
        return List.of(
                Component.translatable("set.isaac_disaster.spun").append(": ")
                        .append(StatManager.DAMAGE.description(2)),
                StatManager.ATTACK_SPEED.description(0.75)
        );
    }
}

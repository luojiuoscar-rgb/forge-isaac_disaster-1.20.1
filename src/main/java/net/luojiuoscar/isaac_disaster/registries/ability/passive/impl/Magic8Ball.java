package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Magic8Ball extends PassiveAbility {
    public Magic8Ball(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        LootHelper.spawnLootAtPos(player, player.position(), ModLootTables.RANDOM_CARDS);
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.BULLET_SPEED.apply(player, 0.8);
        StatManager.ATTACK_KNOCKBACK.apply(player, 0.8);
        StatManager.ENTITY_REACH.apply(player, 0.5);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.BULLET_SPEED.apply(player, -0.8);
        StatManager.ATTACK_KNOCKBACK.apply(player, -0.8);
        StatManager.ENTITY_REACH.apply(player, -0.5);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.BULLET_SPEED.description(.8),
                StatManager.ATTACK_KNOCKBACK.description(.8),
                StatManager.ENTITY_REACH.description(.5),
                Component.translatable("item.isaac_disaster.action.give_card", 1)
        );
    }
}

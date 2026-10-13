package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ABarOfSoap extends PassiveAbility {
    public ABarOfSoap(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.TEARS.apply(player, 0.7);
        StatManager.BULLET_SPEED.apply(player, 1);
        StatManager.BLOCK_BREAKING.apply(player, 0.5);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.TEARS.apply(player, -0.7);
        StatManager.BULLET_SPEED.apply(player, -1);
        StatManager.BLOCK_BREAKING.apply(player, -0.5);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                StatManager.TEARS.description(0.7),
                StatManager.BULLET_SPEED.description(1),
                StatManager.BLOCK_BREAKING.description(0.5)
        );
    }
}

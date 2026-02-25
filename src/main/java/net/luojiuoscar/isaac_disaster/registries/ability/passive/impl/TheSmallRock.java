package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TheSmallRock extends PassiveAbility {
    public TheSmallRock(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 1);
        StatManager.MOVEMENT_SPEED.apply(player, -1);
        StatManager.TEARS.apply(player, 0.25);
        StatManager.BLOCK_BREAKING.apply(player, 0.25);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, -1);
        StatManager.MOVEMENT_SPEED.apply(player, 1);
        StatManager.TEARS.apply(player, -0.25);
        StatManager.BLOCK_BREAKING.apply(player, -0.25);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.DAMAGE.description(1),
                StatManager.MOVEMENT_SPEED.description(1),
                StatManager.TEARS.description(0.25),
                StatManager.BLOCK_BREAKING.description(0.25)
        );
    }

}

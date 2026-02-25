package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TheHalo extends PassiveAbility {
    public TheHalo(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.healHealth(player, 1);
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, 1);
        StatManager.MOVEMENT_SPEED.apply(player, 1.5);
        StatManager.TEARS.apply(player, 0.25);
        StatManager.DAMAGE.apply(player, 0.5);
        StatManager.RANGE.apply(player, 1);

    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, -1);
        StatManager.MOVEMENT_SPEED.apply(player, -1.5);
        StatManager.TEARS.apply(player, -0.25);
        StatManager.DAMAGE.apply(player, -0.5);
        StatManager.RANGE.apply(player, -1);

    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.MAX_HEALTH.description(1),
                StatManager.MOVEMENT_SPEED.description(1.5),
                StatManager.TEARS.description(0.25),
                StatManager.DAMAGE.description(0.5),
                StatManager.RANGE.description(1),
                StatManager.healHealthDescription(1)
        );
    }

}

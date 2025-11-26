package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CricketsHead extends PassiveAbility {
    public CricketsHead(int id, int level) {
        super(id, level);
    }
    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 0.5);
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, 0.5);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 0.5);
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, -0.5);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.DAMAGE.description(0.5),
                StatManager.DAMAGE_MULTIPLY_BASE.description(0.5)
        );
    }
}

package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Polyphemus extends PassiveAbility {
    public Polyphemus(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, 0.8);
        StatManager.DAMAGE.apply(player, 4);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE_MULTIPLY_BASE.apply(player, -0.8);
        StatManager.DAMAGE.apply(player, -4);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                StatManager.DAMAGE_MULTIPLY_BASE.description(0.8),
                StatManager.DAMAGE.description(4),
                Component.translatable("item.isaac_disaster.polyphemus.lore.1"),
                Component.translatable("item.isaac_disaster.polyphemus.lore.2")
        );
    }
}

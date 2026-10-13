package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BlackLotus extends PassiveAbility {
    public BlackLotus(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.healHealth(player, 2);
        PlayerHelper.giveItem(player, ModItems.SOUL_HEART.get(), 1);
        PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
        StatManager.MAX_HEALTH.apply(player, 1.0);
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                StatManager.MAX_HEALTH.description(1.0),
                StatManager.healHealthDescription(2),
                Component.translatable("item.isaac_disaster.action.give_soul_heart", 1),
                Component.translatable("item.isaac_disaster.action.give_black_heart", 1)
        );
    }
}

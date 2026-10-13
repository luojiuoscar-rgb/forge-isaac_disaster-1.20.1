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

public final class Capricorn extends PassiveAbility {


    public Capricorn(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.MAX_HEALTH.apply(player, 1);
        StatManager.healHealth(player, 1);
        PlayerHelper.giveMoney(player, 1);
        PlayerHelper.giveItem(player, ModItems.BOMB.get(), 1);
        PlayerHelper.giveItem(player, ModItems.KEY.get(), 1);
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 0.5);
        StatManager.MOVEMENT_SPEED.apply(player, 0.5);
        StatManager.FIRE_RATE.apply(player, 0.5);
        StatManager.RANGE.apply(player, 0.5);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, -0.5);
        StatManager.MOVEMENT_SPEED.apply(player, -0.5);
        StatManager.FIRE_RATE.apply(player, -0.5);
        StatManager.RANGE.apply(player, -0.5);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                StatManager.MAX_HEALTH.description(1),
                StatManager.healHealthDescription(1),
                StatManager.DAMAGE.description(0.5),
                StatManager.MOVEMENT_SPEED.description(0.5),
                StatManager.FIRE_RATE.description(0.5),
                StatManager.RANGE.description(0.5),
                Component.translatable("item.isaac_disaster.action.give_money", 1),
                Component.translatable("item.isaac_disaster.action.give_bomb", 1),
                Component.translatable("item.isaac_disaster.action.give_key", 1)
        );
    }
}

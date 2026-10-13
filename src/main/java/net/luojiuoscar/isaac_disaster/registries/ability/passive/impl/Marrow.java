package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class Marrow extends PassiveAbility {

    public Marrow(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        PlayerHelper.giveItem(player, ModItems.BONE_HEART.get(), 1);
        PlayerHelper.giveItem(player, ModItems.RED_HEART.get(), 3);
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
                Component.translatable("item.isaac_disaster.action.give_bone_heart", 1),
                Component.translatable("item.isaac_disaster.action.give_red_heart", 3)
        );
    }
}

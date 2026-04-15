package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BobbyBomb extends PassiveAbility {

    public BobbyBomb(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        PlayerHelper.giveItem(player, ModItems.BOMB.get(), 5);

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.action.give_bomb", 5),
                Component.translatable("item.isaac_disaster.bobby_bomb.lore.1")
        );
    }
}

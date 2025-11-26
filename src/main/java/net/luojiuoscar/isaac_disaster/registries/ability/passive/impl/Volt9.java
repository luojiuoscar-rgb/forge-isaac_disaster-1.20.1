package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Volt9 extends PassiveAbility {

    public Volt9(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        // 满电
        PlayerHelper.chargeAll((ServerPlayer) player, null);
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
                Component.translatable("item.isaac_disaster.volt_9.lore.1"),
                Component.translatable("item.isaac_disaster.action.full_charge")
        );
    }
}

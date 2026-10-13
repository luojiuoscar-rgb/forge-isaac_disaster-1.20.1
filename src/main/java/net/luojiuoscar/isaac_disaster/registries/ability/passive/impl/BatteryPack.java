package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BatteryPack extends PassiveAbility {



    public BatteryPack(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        PlayerHelper.chargeAll(player, null);
        LootHelper.spawnLootAtPos(player, player.position(), ModLootTables.RANDOM_BATTERIES, player.getRandom().nextInt(2, 5));
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
                Component.translatable("item.isaac_disaster.battery_pack.lore.1"),
                Component.translatable("item.isaac_disaster.battery_pack.lore.2", 2, 4)
        );
    }
}

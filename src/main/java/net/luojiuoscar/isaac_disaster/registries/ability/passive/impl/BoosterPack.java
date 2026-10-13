package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BoosterPack extends PassiveAbility {


    public BoosterPack(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        LootHelper.spawnLootAtPos(player, player.position(), ModLootTables.BOOSTER_PACK_SPECIAL_SLOT);
        LootHelper.spawnLootAtPos(player, player.position(), ModLootTables.BOOSTER_PACK_NORMAL_SLOT, 4);
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
                Component.translatable("item.isaac_disaster.action.give_card", 5)
        );
    }
}

package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FalsePhd extends PassiveAbility {
    public FalsePhd(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        PlayerHelper.giveItem(player, ModItems.BLACK_HEART.get(), 1);
        LootHelper.spawnLootAtPos(player, player.blockPosition().getCenter(), ModLootTables.RANDOM_PILLS);
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.PILL_QUALITY.apply(player, -1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.PILL_QUALITY.apply(player, 1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                Component.translatable("item.isaac_disaster.false_phd.lore.1"),
                Component.translatable("item.isaac_disaster.false_phd.lore.2"),
                Component.translatable("item.isaac_disaster.false_phd.lore.3"),
                Component.translatable("item.isaac_disaster.action.give_pill", 1),
                Component.translatable("item.isaac_disaster.action.give_black_heart", 1)

        );
    }
}

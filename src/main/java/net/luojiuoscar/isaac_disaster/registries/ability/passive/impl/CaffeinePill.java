package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.helper.LootHelper;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.ModLootTables;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CaffeinePill extends PassiveAbility {
    public CaffeinePill(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        if (player.level() instanceof ServerLevel level){
            LootHelper.spawnLootAtPos(player, player.blockPosition().getCenter(), ModLootTables.RANDOM_PILLS);
        }
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.MOVEMENT_SPEED.apply(player, 1.5);
        StatManager.SCALE.apply(player, -1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.MOVEMENT_SPEED.apply(player, -1.5);
        StatManager.SCALE.apply(player, 1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        return List.of(
                StatManager.MOVEMENT_SPEED.description(1.5),
                Component.translatable("attribute.isaac_disaster.scale_down"),
                Component.translatable("item.isaac_disaster.action.give_pill", 1)
        );
    }
}

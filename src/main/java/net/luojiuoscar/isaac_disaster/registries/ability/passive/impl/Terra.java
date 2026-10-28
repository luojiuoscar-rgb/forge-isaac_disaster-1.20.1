package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Terra extends PassiveAbility {
    public Terra(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 1);
        StatManager.BLOCK_BREAKING.apply(player, 1.5);
        StatManager.addTriggerModule(player, ModTriggerModules.TERRA.getId(), 1);

    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, -1);
        StatManager.BLOCK_BREAKING.apply(player, -1.5);
        StatManager.addTriggerModule(player, ModTriggerModules.TERRA.getId(), -1);

    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        if (ClientDataManager.getInstance().getItemCount(ModPassiveItems.FIRE_MIND.getId()) <= 0) return List.of();
        return DescriptionHelper.getSynergyDesc(Component.translatable("item.isaac_disaster.fire_mind"), List.of(
                Component.translatable("item.isaac_disaster.terra.synergy.fire_mind.1")));
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                StatManager.DAMAGE.description(1),
                StatManager.BLOCK_BREAKING.description(1.5),
                Component.translatable("item.isaac_disaster.terra.lore.1")
        );
    }

}

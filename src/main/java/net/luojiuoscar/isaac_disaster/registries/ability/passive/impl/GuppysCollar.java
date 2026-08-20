package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModule;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GuppysCollar extends PassiveAbility {
    public GuppysCollar(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModule.GUPPYS_COLLAR.getId(), 1);
        StatManager.modifySetWithId(player, ModSetAbility.CAT.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModule.GUPPYS_COLLAR.getId(), -1);
        StatManager.modifySetWithId(player, ModSetAbility.CAT.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.guppys_collar.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, Player player) {
        return ModSetAbility.CAT.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, Player player) {
        return ModSetAbility.CAT.get().getExtraDesc();
    }
}

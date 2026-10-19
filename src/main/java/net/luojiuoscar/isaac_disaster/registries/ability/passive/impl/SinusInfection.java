package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import java.util.List;
import net.luojiuoscar.isaac_disaster.manager.EffectManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class SinusInfection extends PassiveAbility {
    public SinusInfection(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {}

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.SINUS_INFECTION.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.SINUS_INFECTION.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.sinus_infection.lore.1"),
                Component.translatable("item.isaac_disaster.sinus_infection.lore.2"),
                Component.translatable("item.isaac_disaster.sinus_infection.lore.3"),
                Component.translatable("item.isaac_disaster.sinus_infection.lore.4"));
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, Player player) {
        return List.of(EffectManager.DEADLY_POISON.getExplainDesc());
    }
}

package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MonstrosLung extends PassiveAbility {
    public MonstrosLung(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {}

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addAttackType(player, ModAttackTypes.MONSTROS_LUNG.getId(), 1);
        StatManager.addTriggerModule(player, ModTriggerModules.BRIMSTONE_MONSTROS_LUNG.getId(), 1);
        StatManager.addBulletColor(player, ModBulletColors.BLOOD_TEAR.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addAttackType(player, ModAttackTypes.MONSTROS_LUNG.getId(), -1);
        StatManager.addTriggerModule(player, ModTriggerModules.BRIMSTONE_MONSTROS_LUNG.getId(), -1);
        StatManager.addBulletColor(player, ModBulletColors.BLOOD_TEAR.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.monstros_lung.lore.1"),
                Component.translatable("item.isaac_disaster.monstros_lung.lore.2")
        );
    }
}

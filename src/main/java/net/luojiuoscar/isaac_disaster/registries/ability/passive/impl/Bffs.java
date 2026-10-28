package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.ModFamiliarModifiers;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class Bffs extends PassiveAbility {
    public Bffs(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) { }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addFamiliarModifier(player, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
        StatManager.addFamiliarModifier(player, ModFamiliarModifiers.BFFS_VISUAL_SCALE.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.removeFamiliarModifier(player, ModFamiliarModifiers.BFFS_ENHANCEMENT.getId(), 1);
        StatManager.removeFamiliarModifier(player, ModFamiliarModifiers.BFFS_VISUAL_SCALE.getId(), 1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.bffs.lore.1"),
                Component.translatable("item.isaac_disaster.bffs.lore.2"));
    }
}

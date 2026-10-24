package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MomsKnife extends PassiveAbility {
    public MomsKnife(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {}

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addAttackType(player, ModAttackTypes.MOMS_KNIFE.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addAttackType(player, ModAttackTypes.MOMS_KNIFE.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.moms_knife.lore.1"),
                Component.translatable("item.isaac_disaster.moms_knife.lore.2")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, Player player) {
        if (ClientDataManager.getInstance().getItemCount(ModPassiveItems.BRIMSTONE.getId()) <= 0) return List.of();
        return DescriptionHelper.getSynergyDesc(Component.translatable("item.isaac_disaster.brimstone"), List.of(
                Component.translatable("item.isaac_disaster.moms_knife.synergy.brimstone.1")
        ));
    }
}

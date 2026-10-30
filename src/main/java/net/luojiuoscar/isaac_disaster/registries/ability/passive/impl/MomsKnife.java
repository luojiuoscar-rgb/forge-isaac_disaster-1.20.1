package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        StatManager.modifySetWithId(player, ModSetAbilities.MOM.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addAttackType(player, ModAttackTypes.MOMS_KNIFE.getId(), -1);
        StatManager.modifySetWithId(player, ModSetAbilities.MOM.getId(), -1);
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
        List<Component> description = new ArrayList<>(ModSetAbilities.MOM.get().getSynergyDesc());
        if (ClientDataManager.getInstance().getItemCount(ModPassiveItems.C_SECTION.getId()) > 0) return description;
        if (ClientDataManager.getInstance().getItemCount(ModPassiveItems.TECHNOLOGY.getId()) > 0) {
            description.addAll(DescriptionHelper.getSynergyDesc(Component.translatable("item.isaac_disaster.technology"), List.of(
                    Component.translatable("item.isaac_disaster.moms_knife.synergy.technology.1"),
                    Component.translatable("item.isaac_disaster.moms_knife.synergy.technology.2")
            )));
        }
        if (ClientDataManager.getInstance().getItemCount(ModPassiveItems.BRIMSTONE.getId()) > 0) {
            description.addAll(DescriptionHelper.getSynergyDesc(Component.translatable("item.isaac_disaster.brimstone"), List.of(
                    Component.translatable("item.isaac_disaster.moms_knife.synergy.brimstone.1"),
                    Component.translatable("item.isaac_disaster.moms_knife.synergy.brimstone.2")
            )));
        }
        if (ClientDataManager.getInstance().getItemCount(ModPassiveItems.LOKIS_HORNS.getId()) > 0) {
            description.addAll(DescriptionHelper.getSynergyDesc(Component.translatable("item.isaac_disaster.lokis_horns"), List.of(
                    Component.translatable("item.isaac_disaster.moms_knife.synergy.lokis_horns.1"),
                    Component.translatable("item.isaac_disaster.moms_knife.synergy.lokis_horns.2")
            )));
        }
        if (ClientDataManager.getInstance().getSetCountFromId(ModSetAbilities.BOOK.get().getId())
                >= ModSetAbilities.BOOK.get().getRequirementCount()) {
            description.addAll(DescriptionHelper.getSynergyDesc(Component.translatable("set.isaac_disaster.book"), List.of(
                    Component.translatable("item.isaac_disaster.moms_knife.synergy.book.1")
            )));
        }
        if (ClientDataManager.getInstance().getItemCount(ModPassiveItems.MONSTROS_LUNG.getId()) > 0) {
            description.addAll(DescriptionHelper.getSynergyDesc(Component.translatable("item.isaac_disaster.monstros_lung"), List.of(
                    Component.translatable("item.isaac_disaster.moms_knife.synergy.monstros_lung.1")
            )));
        }
        if (ClientDataManager.getInstance().getItemCount(ModPassiveItems.TINY_PLANET.getId()) > 0) {
            description.addAll(DescriptionHelper.getSynergyDesc(Component.translatable("item.isaac_disaster.tiny_planet"), List.of(
                    Component.translatable("item.isaac_disaster.moms_knife.synergy.tiny_planet.1")
            )));
        }
        return description;
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, Player player) {
        return ModSetAbilities.MOM.get().getExtraDesc();
    }
}

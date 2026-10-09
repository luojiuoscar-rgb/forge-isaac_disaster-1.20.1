package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.helper.PlayerHelper;
import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Revelation extends PassiveAbility {
    public Revelation(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
        PlayerHelper.giveItem(player, ModItems.SOUL_HEART.get(), 2);
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.FLY_TIME.apply(player, 1);
        StatManager.modifySetWithId(player, ModSetAbilities.SERAPHIM.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.FLY_TIME.apply(player, -1);
        StatManager.modifySetWithId(player, ModSetAbilities.SERAPHIM.getId(), -1);
        ModAttackTypes.REVELATION.get().onItemRemoved(player);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.revelation.lore.1"),
                Component.translatable("item.isaac_disaster.revelation.lore.2"),
                Component.translatable("item.isaac_disaster.revelation.lore.3"),
                StatManager.FLY_TIME.description(1),
                Component.translatable("item.isaac_disaster.action.give_soul_heart", 2)
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, Player player) {
        return ModSetAbilities.SERAPHIM.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, Player player) {
        return ModSetAbilities.SERAPHIM.get().getExtraDesc();
    }
}

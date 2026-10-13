package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.recursive_module.ModRecursiveModules;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TwoSpooky extends PassiveAbility {
    public TwoSpooky(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.TEARS.apply(player, 0.7);
        StatManager.BULLET_SPEED.apply(player, 1);
        StatManager.addRecursiveModule(player, ModRecursiveModules.SPOOKY_2.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.TEARS.apply(player, -0.7);
        StatManager.BULLET_SPEED.apply(player, -1);
        StatManager.addRecursiveModule(player, ModRecursiveModules.SPOOKY_2.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                StatManager.TEARS.description(0.7),
                StatManager.BULLET_SPEED.description(1),
                Component.translatable("item.isaac_disaster.two_spooky.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        if (ClientDataManager.getInstance().getItemCount(ModPassiveItems.VENUS.getId()) == 0) return List.of();
        return List.of(DescriptionHelper.getSynergyDesc(
                Component.translatable("item.isaac_disaster.venus"),
                Component.translatable("item.isaac_disaster.two_spooky.synergy.venus.1")
        ));
    }
}

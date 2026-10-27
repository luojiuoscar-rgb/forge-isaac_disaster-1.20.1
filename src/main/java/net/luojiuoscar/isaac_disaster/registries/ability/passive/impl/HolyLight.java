package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.helper.DescriptionHelper;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.luojiuoscar.isaac_disaster.registries.trigger_module.impl.normal.HolyLight.chance;

public final class HolyLight extends PassiveAbility {
    public HolyLight(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {}

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.HOLY_LIGHT.getId(), 1);
        StatManager.modifySetWithId(player, ModSetAbilities.SERAPHIM.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addTriggerModule(player, ModTriggerModules.HOLY_LIGHT.getId(), -1);
        StatManager.modifySetWithId(player, ModSetAbilities.SERAPHIM.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.holy_light.lore.1",
                        DescriptionHelper.dynamicNumber((player == null ? chance(0) : chance(player)) * 100)),
                Component.translatable("item.isaac_disaster.holy_light.lore.2"),
                Component.translatable("item.isaac_disaster.holy_light.lore.3")
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

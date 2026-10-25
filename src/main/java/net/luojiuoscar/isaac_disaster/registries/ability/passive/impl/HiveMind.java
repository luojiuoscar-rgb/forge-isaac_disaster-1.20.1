package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.familiar_modifier.ModFamiliarModifiers;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;

public final class HiveMind extends PassiveAbility {
    public HiveMind(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) { }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addFamiliarModifier(player, ModFamiliarModifiers.HIVE_MIND_ENHANCEMENT.getId(), 1);
        StatManager.addFamiliarModifier(player, ModFamiliarModifiers.HIVE_MIND_VISUAL_SCALE.getId(), 1);
        StatManager.modifySetWithId(player, ModSetAbilities.BEELZEBUB.getId(), 1);
        StatManager.modifySetWithId(player, ModSetAbilities.SPIDER_BABY.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.removeFamiliarModifier(player, ModFamiliarModifiers.HIVE_MIND_ENHANCEMENT.getId(), 1);
        StatManager.removeFamiliarModifier(player, ModFamiliarModifiers.HIVE_MIND_VISUAL_SCALE.getId(), 1);
        StatManager.modifySetWithId(player, ModSetAbilities.BEELZEBUB.getId(), -1);
        StatManager.modifySetWithId(player, ModSetAbilities.SPIDER_BABY.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.hive_mind.lore.1")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack, @Nullable Player player) {
        List<Component> result = new ArrayList<>(ModSetAbilities.BEELZEBUB.get().getSynergyDesc());
        result.addAll(ModSetAbilities.SPIDER_BABY.get().getSynergyDesc());
        return result;
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack, @Nullable Player player) {
        List<Component> result = new ArrayList<>();
        for (var set : List.of(ModSetAbilities.BEELZEBUB.get(), ModSetAbilities.SPIDER_BABY.get())) {
            for (var line : set.getExtraDesc()) result.add(set.getName().copy().append(": ").append(line));
        }
        return result;
    }
}

package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MutantSpider extends PassiveAbility {
    public static final class Multipliers {
        public static final MultiplierEntry FIRE_RATE = StatManager.createMultiplierEntry(
                "mutant_spider", MultiplierEntry.FIRING_PENALTY_ID, ModAttributes.FIRE_RATE.get(), -0.5D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public MutantSpider(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addMultiplier(player, Multipliers.FIRE_RATE, 1);
        StatManager.addTriggerModule(player, ModTriggerModules.FIRING_MODIFIER.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.removeMultiplier(player, Multipliers.FIRE_RATE, 1);
        StatManager.addTriggerModule(player, ModTriggerModules.FIRING_MODIFIER.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.mutant_spider.lore.1"),
                Component.translatable("attribute.isaac_disaster.double_shot_delay")
        );
    }
}

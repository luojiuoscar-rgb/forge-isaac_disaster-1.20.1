package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;


import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.item.ModPassiveItems;
import net.luojiuoscar.isaac_disaster.manager.MultishotCountManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierEntry;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierRules;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PerfectVision extends PassiveAbility {
    public static final class Multipliers {
        public static final MultiplierEntry DAMAGE = StatManager.createMultiplierEntry(
                "perfect_vision", "damage", Attributes.ATTACK_DAMAGE, -0.2,
                AttributeModifier.Operation.MULTIPLY_BASE);
    }

    public static final class Rules {
        public static final MultiplierRules.RuleEntry FIRE_RATE = new MultiplierRules.RuleEntry(
                ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "multiplier_rule/perfect_vision_suppresses_firing_penalty"),
                Multipliers.DAMAGE.sourceId(), MultiplierEntry.FIRING_PENALTY_ID);
    }

    public PerfectVision(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addMultiplierRule(player, Rules.FIRE_RATE, 1);

        StatManager.addMultiplier(player, Multipliers.DAMAGE, 1);
        MultishotCountManager.update(player, ModPassiveItems.PERFECT_VISION.getId());
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.removeMultiplierRule(player, Rules.FIRE_RATE.id(), 1);
        StatManager.removeMultiplier(player, Multipliers.DAMAGE, 1);
        MultishotCountManager.update(player);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.perfect_vision.lore.1"),
                Component.translatable("item.isaac_disaster.action.damage_multiplier", "-20")
        );
    }
}

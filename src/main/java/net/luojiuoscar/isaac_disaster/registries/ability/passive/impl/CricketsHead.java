package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
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

public class CricketsHead extends PassiveAbility {
    public static final class Multipliers {
        public static final MultiplierEntry DAMAGE = StatManager.createMultiplierEntry(
                "crickets_head", "damage", Attributes.ATTACK_DAMAGE, 0.5, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    public CricketsHead(int id, int level) {
        super(id, level);
    }

    public static final class Rules {
        public static final MultiplierRules.RuleEntry SUPPRESS_MAGIC_MUSHROOM_DAMAGE = new MultiplierRules.RuleEntry(
                ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "multiplier_rule/crickets_head_suppresses_magic_mushroom_damage"),
                Multipliers.DAMAGE.sourceId(), MagicMushroom.Multipliers.DAMAGE.id());
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 0.5);
        StatManager.addMultiplier(player, Multipliers.DAMAGE, 1);
        StatManager.addMultiplierRule(player, Rules.SUPPRESS_MAGIC_MUSHROOM_DAMAGE, 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, -0.5);
        StatManager.removeMultiplier(player, Multipliers.DAMAGE.id(), 1);
        StatManager.removeMultiplierRule(player, Rules.SUPPRESS_MAGIC_MUSHROOM_DAMAGE.id(), 1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                StatManager.DAMAGE.description(0.5),
                Component.translatable("item.isaac_disaster.action.damage_multiplier", "+50")
        );
    }
}

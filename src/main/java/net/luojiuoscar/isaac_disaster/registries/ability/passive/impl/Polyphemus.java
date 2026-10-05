package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierEntry;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Polyphemus extends PassiveAbility {
    public static final class Multipliers {
        public static final MultiplierEntry DAMAGE = StatManager.createMultiplierEntry(
                "polyphemus", "damage", Attributes.ATTACK_DAMAGE, 0.8, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    public Polyphemus(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.addMultiplier(player, Multipliers.DAMAGE, 1);
        StatManager.DAMAGE.apply(player, 4);
        StatManager.addTriggerModule(player, ModTriggerModules.FIRING_MODIFIER.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.removeMultiplier(player, Multipliers.DAMAGE.id(), 1);
        StatManager.DAMAGE.apply(player, -4);
        StatManager.addTriggerModule(player, ModTriggerModules.FIRING_MODIFIER.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.action.damage_multiplier", "+80"),
                StatManager.DAMAGE.description(4),
                Component.translatable("item.isaac_disaster.polyphemus.lore.1"),
                Component.translatable("item.isaac_disaster.polyphemus.lore.2")
        );
    }
}

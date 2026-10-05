package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierEntry;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Passive ability for Cricket's Body. */
public final class CricketsBody extends PassiveAbility {
    public static final class Multipliers {
        public static final MultiplierEntry RANGE = StatManager.createMultiplierEntry(
                "crickets_body", "range", ModAttributes.BULLET_RANGE.get(), -0.2,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public CricketsBody(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.BULLET_SCALE.apply(player, 1.5);
        StatManager.addTriggerModule(player, ModTriggerModules.CRICKETS_BODY.getId(), 1);
        StatManager.TEARS_CORRECTION.apply(player, 0.5);
        StatManager.addMultiplier(player, Multipliers.RANGE, 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.BULLET_SCALE.apply(player, -1.5);
        StatManager.addTriggerModule(player, ModTriggerModules.CRICKETS_BODY.getId(), -1);
        StatManager.TEARS_CORRECTION.apply(player, -0.5);
        StatManager.removeMultiplier(player, Multipliers.RANGE.id(), 1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                StatManager.BULLET_SCALE.description(1.5),
                StatManager.TEARS_CORRECTION.description(0.5),
                Component.translatable("item.isaac_disaster.crickets_body.lore.1"),
                Component.translatable("item.isaac_disaster.crickets_body.lore.2"),
                Component.translatable("item.isaac_disaster.action.range_multiplier", "-20")
        );
    }
}

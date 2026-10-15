package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class NumberOne extends PassiveAbility {
    public static final class Multipliers {
        public static final MultiplierEntry RANGE = StatManager.createMultiplierEntry(
                "number_one", "range", ModAttributes.BULLET_RANGE.get(), -0.2,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public NumberOne(int id, int level) { super(id, level); }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) { }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.TEARS.apply(player, 2);
        StatManager.RANGE.apply(player, -1);
        StatManager.addMultiplier(player, Multipliers.RANGE, 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.TEARS.apply(player, -2);
        StatManager.RANGE.apply(player, 1);
        StatManager.removeMultiplier(player, Multipliers.RANGE, 1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                StatManager.TEARS.description(2),
                StatManager.RANGE.description(-1),
                Component.translatable("item.isaac_disaster.action.range_multiplier", "-20")
        );
    }
}

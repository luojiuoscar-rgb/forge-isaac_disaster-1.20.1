package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierEntry;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class EvesMascara extends PassiveAbility {

    public static final class Multipliers {
        public static final MultiplierEntry DAMAGE = StatManager.createMultiplierEntry(
                "eves_mascara", "damage", Attributes.ATTACK_DAMAGE,
                1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        public static final MultiplierEntry FIRE_RATE = StatManager.createMultiplierEntry(
                "eves_mascara", "fire_rate", ModAttributes.FIRE_RATE.get(),
                -0.34, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public EvesMascara(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.BULLET_SPEED.apply(player, -2.5);
        StatManager.addMultiplier(player, Multipliers.DAMAGE, 1);
        StatManager.addMultiplier(player, Multipliers.FIRE_RATE, 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.BULLET_SPEED.apply(player, 2.5);
        StatManager.removeMultiplier(player, Multipliers.DAMAGE, 1);
        StatManager.removeMultiplier(player, Multipliers.FIRE_RATE, 1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, @Nullable Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.action.damage_multiplier", "+100"),
                Component.translatable("item.isaac_disaster.action.fire_rate_multiplier", "-34"),
                StatManager.BULLET_SPEED.description(-2.5)
        );
    }
}

package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierEntry;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MyReflection extends PassiveAbility {
    public static final class Multipliers {
        public static final MultiplierEntry RANGE = StatManager.createMultiplierEntry(
                "my_reflection", "range", ModAttributes.BULLET_RANGE.get(), 1.0,
                AttributeModifier.Operation.MULTIPLY_BASE);
        public static final MultiplierEntry BULLET_SPEED = StatManager.createMultiplierEntry(
                "my_reflection", "bullet_speed", ModAttributes.BULLET_SPEED.get(), 0.6,
                AttributeModifier.Operation.MULTIPLY_BASE);
    }

    public MyReflection(int id, int level) {
        super(id, level);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.RANGE.apply(player, 1.5);
        StatManager.DAMAGE.apply(player, 1.5);
        StatManager.LUCK.apply(player, -1);
        StatManager.addTriggerModule(player, ModTriggerModules.MY_REFLECTION.getId(), 1);

        StatManager.addMultiplier(player, Multipliers.RANGE, 1);
        StatManager.addMultiplier(player, Multipliers.BULLET_SPEED, 1);

    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.removeMultiplier(player, Multipliers.RANGE.id(), 1);
        StatManager.removeMultiplier(player, Multipliers.BULLET_SPEED.id(), 1);

        StatManager.RANGE.apply(player, -1.5);
        StatManager.DAMAGE.apply(player, -1.5);
        StatManager.LUCK.apply(player, 1);
        StatManager.addTriggerModule(player, ModTriggerModules.MY_REFLECTION.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                Component.translatable("item.isaac_disaster.my_reflection.lore.1"),
                Component.translatable("item.isaac_disaster.action.range_base_multiplier", "+100"),
                Component.translatable("item.isaac_disaster.action.bullet_speed_base_multiplier", "+60"),
                StatManager.RANGE.description(1.5),
                StatManager.DAMAGE.description(1.5),
                StatManager.LUCK.description(-1)
        );
    }

}

package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierEntry;
import net.luojiuoscar.isaac_disaster.system.stat_multiplier.MultiplierRules;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Haemolacria extends PassiveAbility {
    public static final class Multipliers {
        public static final MultiplierEntry DAMAGE = StatManager.createMultiplierEntry(
                "haemolacria", "damage", Attributes.ATTACK_DAMAGE, 0.5,
                AttributeModifier.Operation.MULTIPLY_BASE);
        public static final MultiplierEntry RANGE = StatManager.createMultiplierEntry(
                "haemolacria", "range", ModAttributes.BULLET_RANGE.get(), -0.2,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public Haemolacria(int id, int level) {
        super(id, level);
    }

    public static final class Rules {
        public static final MultiplierRules.RuleEntry SUPPRESS_CRICKETS_BODY_RANGE = new MultiplierRules.RuleEntry(
                ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "multiplier_rule/haemolacria_suppresses_crickets_body_range"),
                Multipliers.RANGE.sourceId(), CricketsBody.Multipliers.RANGE.id());
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
    }

    @Override
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, 1);
        StatManager.addAttackType(player, ModAttackTypes.HAEMOLACRIA.getId(), 1);
        StatManager.addMultiplier(player, Multipliers.DAMAGE, 1);
        StatManager.addMultiplier(player, Multipliers.RANGE, 1);
        StatManager.addMultiplierRule(player, Rules.SUPPRESS_CRICKETS_BODY_RANGE, 1);
        StatManager.addTriggerModule(player, ModTriggerModules.FIRING_MODIFIER.getId(), 1);
        StatManager.addTriggerModule(player, ModTriggerModules.HAEMOLACRIA.getId(), 1);
        StatManager.addBulletColor(player, ModBulletColors.BLOOD_TEAR.getId(), 1);
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        StatManager.DAMAGE.apply(player, -1);
        StatManager.addAttackType(player, ModAttackTypes.HAEMOLACRIA.getId(), -1);
        StatManager.removeMultiplier(player, Multipliers.DAMAGE.id(), 1);
        StatManager.removeMultiplier(player, Multipliers.RANGE.id(), 1);
        StatManager.removeMultiplierRule(player, Rules.SUPPRESS_CRICKETS_BODY_RANGE.id(), 1);
        StatManager.addTriggerModule(player, ModTriggerModules.FIRING_MODIFIER.getId(), -1);
        StatManager.addTriggerModule(player, ModTriggerModules.HAEMOLACRIA.getId(), -1);
        StatManager.addBulletColor(player, ModBulletColors.BLOOD_TEAR.getId(), -1);
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack, Player player) {
        return List.of(
                StatManager.DAMAGE.description(1),
                Component.translatable("item.isaac_disaster.action.damage_multiplier", "+50"),
                Component.translatable("item.isaac_disaster.action.bullet_scale_multiplier", "+71"),
                Component.translatable("item.isaac_disaster.action.range_multiplier", "-20"),
                Component.translatable("item.isaac_disaster.haemolacria.lore.3"),
                Component.translatable("item.isaac_disaster.haemolacria.lore.4"),
                Component.translatable("item.isaac_disaster.haemolacria.lore.5")
        );
    }
}

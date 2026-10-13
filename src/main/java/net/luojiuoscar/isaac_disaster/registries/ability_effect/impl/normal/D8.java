package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.attribute.ModAttributes;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerStatModifierProvider;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.DoubleSupplier;

public final class D8 implements IAbilityEffect {
    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;
        return player.getCapability(PlayerStatModifierProvider.PLAYER_STAT_MODIFIER).map(modifiers -> {
            Attribute[] attributes = {Attributes.ATTACK_DAMAGE, ModAttributes.FIRE_RATE.get(),
                    ModAttributes.BULLET_RANGE.get(), Attributes.MOVEMENT_SPEED};
            String[] names = {"damage", "fire_rate", "range", "speed"};
            int rolls = Math.max(1, context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue());
            for (int roll = 0; roll < rolls; roll++) {
                // Reading final values records both intermediate Car Battery results for Rock Bottom.
                for (Attribute attribute : attributes) player.getAttributeValue(attribute);
                double[] factors = sampleMultipliers(player.getRandom()::nextDouble);
                var entries = modifiers.getMultiplierEntries();
                for (int i = 0; i < factors.length; i++) {
                    var entry = StatManager.createMultiplierEntry("d8", names[i], attributes[i],
                            factors[i] - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
                    if (entry == null) return false;
                    var previous = entries.snapshot().get(entry.key());
                    if (previous != null) entries.remove(entry.key(), previous.stacks());
                    entries.add(entry, 1);
                }
                StatManager.refreshMultipliers(player);
                for (Attribute attribute : attributes) player.getAttributeValue(attribute);
            }
            return true;
        }).orElse(false);
    }

    static double[] sampleMultipliers(DoubleSupplier random) {
        double bound = Math.log(2);
        for (int attempt = 0; attempt < 64; attempt++) {
            double[] logs = new double[4];
            double mean = 0;
            for (int i = 0; i < logs.length; i++) {
                logs[i] = (random.getAsDouble() * 2 - 1) * bound;
                mean += logs[i] / 4;
            }
            boolean valid = true;
            for (double log : logs) {
                if (Math.abs(log - mean) > bound) valid = false;
            }
            if (!valid) continue;
            for (int i = 0; i < logs.length; i++) logs[i] = Math.exp(logs[i] - mean);
            return logs;
        }
        return new double[]{1, 1, 1, 1};
    }
}

package net.luojiuoscar.isaac_disaster.effect;


import net.luojiuoscar.isaac_disaster.capability.entity.EntityEffectProvider;
import net.luojiuoscar.isaac_disaster.manager.EffectNameManager;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PowerOfBelialEffect extends MobEffect {
    protected PowerOfBelialEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier) {
        return (double)(pAmplifier + 1) * StatManager.getDamageBonus();
    }
}

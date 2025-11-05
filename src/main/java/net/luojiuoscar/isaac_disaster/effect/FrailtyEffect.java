package net.luojiuoscar.isaac_disaster.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FrailtyEffect extends MobEffect {
    private static final UUID FRAILTY_EFFECT_DAMAGE_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:frailty_effect").getBytes());

    protected FrailtyEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void addAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);

        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(FRAILTY_EFFECT_DAMAGE_UUID);
            attackDamage.addTransientModifier(getFrailtyModifier(amplifier));
        }
    }

    @Override
    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);

        AttributeInstance attackDamage = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.removeModifier(FRAILTY_EFFECT_DAMAGE_UUID);
        }
    }

    private static AttributeModifier getFrailtyModifier(int amplifier) {
        return new AttributeModifier(
                FRAILTY_EFFECT_DAMAGE_UUID,
                "frailty",
                -0.2 * (amplifier + 1),
                AttributeModifier.Operation.MULTIPLY_BASE
        );
    }
}

package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.IStackableEffect;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class FragileHeartEffect extends MobEffect implements IStackableEffect {
    private static final UUID FRAGILE_HEART_EFFECT_UUID =
            UUID.nameUUIDFromBytes(("isaac_disaster:fragile_heart_effect").getBytes());

    public FragileHeartEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        if (!(entity instanceof Player)){
            entity.removeEffect(this);
            return;
        }

        double healthBonus = StatManager.MAX_HEALTH.getBonus() * (amplifier + 1);

        AttributeInstance instance = entity.getAttribute(Attributes.MAX_HEALTH);
        if (instance == null) return;
        instance.addPermanentModifier(
                new AttributeModifier(
                        FRAGILE_HEART_EFFECT_UUID,
                        "fragile_heart_bonus",
                        healthBonus,
                        AttributeModifier.Operation.ADDITION
                )
        );
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        AttributeInstance instance = entity.getAttribute(Attributes.MAX_HEALTH);
        if (instance == null) return;
        instance.removeModifier(FRAGILE_HEART_EFFECT_UUID);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    public void stack(LivingEntity entity, int count){
        MobEffect effect = ModEffects.FRAGILE_HEART.get();

        int amplifier = entity.getEffect(effect) == null ? -1 : entity.getEffect(effect).getAmplifier();
        amplifier += count;

        entity.removeEffect(effect);
        if (amplifier < 0) return;

        MobEffectInstance newEffect = new MobEffectInstance(
                effect,
                -1,
                amplifier,
                false,
                false,
                true
        );
        entity.addEffect(newEffect);
    }
}

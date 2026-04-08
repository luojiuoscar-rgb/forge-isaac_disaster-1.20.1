package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.IStackableEffect;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class HolyShieldEffect extends MobEffect implements IStackableEffect {
    public HolyShieldEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    public void stack(LivingEntity entity, int count){
        MobEffect effect = ModEffects.HOLY_SHIELD.get();

        int amplifier = entity.getEffect(effect) == null ? -1 : entity.getEffect(effect).getAmplifier();
        amplifier += count;
        amplifier = Math.min(10, amplifier); // max 10

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

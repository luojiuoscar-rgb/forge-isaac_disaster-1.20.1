package net.luojiuoscar.isaac_disaster.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class DeadlyPoisonEffect extends MobEffect {
    public DeadlyPoisonEffect(MobEffectCategory category, int color) { super(category, color); }

    public static float damageForAmplifier(int amplifier) {
        return 1.0f + 0.5f * Math.max(0, amplifier);
    }

    /** Quantizes a damage snapshot to the nearest half point within potion amplifier limits. */
    public static int amplifierForDamage(double damage) {
        if (!Double.isFinite(damage)) return damage > 0 ? 255 : 0;
        return (int) Math.max(0, Math.min(255, Math.round((damage - 1.0) * 2.0)));
    }

    @Override
    public List<ItemStack> getCurativeItems() { return List.of(new ItemStack(Items.MILK_BUCKET)); }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(entity.damageSources().magic(), damageForAmplifier(amplifier));
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return duration % 20 == 0; }
}

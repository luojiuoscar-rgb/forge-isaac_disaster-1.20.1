package net.luojiuoscar.isaac_disaster.effect.custom;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.registries.visual.ModVisualLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class PetrifiedEffect extends EntityFreezeEffect {
    private static final ResourceLocation SOURCE = ModEffects.PETRIFIED.getId();
    private static final ResourceLocation LAYER = ModVisualLayers.PETRIFIED.getId();

    public PetrifiedEffect(MobEffectCategory category, int color) {
        super(category, color, SOURCE, LAYER);
    }

    public static void reconcileVisualState(LivingEntity entity) {
        EntityFreezeEffect.reconcileVisualState(entity, ModEffects.PETRIFIED.get(), SOURCE, LAYER);
    }
}
